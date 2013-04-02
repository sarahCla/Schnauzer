/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sarah.Schnauzer.listener.master.Impl;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.glossary.column.BitColumn;
import com.sarah.Schnauzer.helper.ConfigGetHelper;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.WarmingMailHelper;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.MasterDBHelper;
import com.sarah.Schnauzer.helper.DB.Redis.RedisSlaveDBHelper;
import com.sarah.Schnauzer.listener.ColumnTypeHelper;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RDBSchnauzer;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.BaseField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Impl.RedisSchnauzer;
import com.sarah.Schnauzer.listener.master.IMaster;
import com.sarah.tools.cuid.CUID;
import com.sarah.tools.type.StrHelp;

/**
 * 
 * @author SarahCla
 */
public class SchnauzerRedisMaster  implements IMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchnauzerRedisMaster.class);	
	
	protected DBConnectorConfig slaveDb;	
	protected DBConnectorConfig masterDb;
	
	private RedisSlaveDBHelper dbhelper;
	private WarmingMailHelper mailsender;	

	private List<RedisSchnauzer> tables = new CopyOnWriteArrayList<RedisSchnauzer>();
	
	@Override
	public boolean registgerTableReplicator(DBConnectorConfig master) {
		try
		{
			tables.clear();
			ConfigGetHelper conf = new ConfigGetHelper();
			conf.getRedisTables(tables);
			MasterDBHelper mdbhelper = new MasterDBHelper(master);
			for (int i=0; i<this.tables.size(); i++)
			{
				RedisSchnauzer table = (RedisSchnauzer)this.tables.get(i);
				ResultSet rs = mdbhelper.getTableFields(table.getMasterTableName());
				int index = 0;
				Boolean haveCol = false;
				while(rs.next())
				{
					haveCol = true;
					RepField field = new RepField(rs.getString("column_name"), "", "");
					String characterset = rs.getString("character_set_name");
					if (characterset!=null && !characterset.equals("")) field.characterset = characterset;
					table.addMasterField(field);
				}	
				if (!haveCol) {
					ErrorHelper.errExit(String.format(Infos.TableNotExist, table.getMasterTableName(), masterDb.dbname));
				}
			}
		} catch (Exception e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + e.toString());
			return false;
		}
		return true;
	}
	

	private String[] handleWrite(ColumnTypeHelper helper, WriteRowsEvent event, RedisSchnauzer table) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		BitColumn bCol = event.getUsedColumns().clone();
		String[] sql = new String[1]; 
		String sqlValue = "";			
		LOGGER.info(rows.size() + Infos.Row);
		for (int j=0; j<rows.size(); j++)
		{
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!table.needReplicate(columns, helper)) continue;
			switch(table.getType()) {
			case Set :
				dbhelper.sadd(table.getKey(columns), columns.get(table.getValueIndex()).toString());
				dbhelper.setBinlogKey(helper.binlogFileName, Long.toString(helper.position), helper.tableName);
				break;
			case SortedSet:
				break;
			case List:
				break;
			}
		}
		if (sqlValue.isEmpty()) {
			sql[0] = "";
			return sql;
		}
		sql[0] += sqlValue;
		return sql;
	}
	
	@Override
	public boolean doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		boolean doOne = false;
		String[] sqls = null;
		int index = 0;
		for (int i=0; i<tables.size(); i++)
		{
			RedisSchnauzer table =  tables.get(i);
			if (StrHelp.notEqual(helper.databaseName, masterDb.dbname)) continue;
			if (StrHelp.notEqual(helper.tableName, table.getMasterTableName())) continue;
			String[] rowsqls = null;
			try {
				rowsqls = getInsertSql(helper, event, rep);
			} catch(Exception e) {
				mailAndLog(Infos.Create + Infos.DoInsert + Infos.Failed, e.getMessage());
				return false;
			}
			if (sqls==null) {
				sqls = new String[tables.size()*event.getRows().size()*4 +1];
				sqls[0] = getStatusUpdateSql(helper);
			}
			for (int r=0; r<rowsqls.length; r++) {
				if ((rowsqls[r]==null) || (rowsqls[r].isEmpty())) continue;
				sqls[++index] = rowsqls[r];
				doOne = true;
			}
		}
		
		if (!doOne || !logSql(sqls)) {
			autoSetPosition(helper);
			return true;
		}
		
		String[] errInfo = new String[1];			
		if (dbhelper.excuteSqlByTransaction(sqls, errInfo))
		{
			this.slaveDb.binlog = this.getBinLogName(helper);
			this.slaveDb.pos = helper.position;
			LOGGER.info(Infos.DoInsert + Infos.OK);
			return true;
		}

		String sInfo = getWarning(sqls, errInfo);
		mailAndLog(Infos.DoInsert + Infos.Failed, sInfo);
		rollBackToTableEvent(helper);
		return false;
	}

	@Override
	public boolean doUpdate(ColumnTypeHelper helper, UpdateRowsEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doDelete(ColumnTypeHelper helper, DeleteRowsEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
