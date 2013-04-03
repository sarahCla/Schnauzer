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
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.glossary.column.BitColumn;
import com.sarah.Schnauzer.heartbeat.HeartBeatInfo;
import com.sarah.Schnauzer.heartbeat.HeartBeatSender;
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
import com.sarah.tools.localinfo.LocalInfo;
import com.sarah.tools.localinfo.LocalInfoGetter;
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
	
	

	private void innerWrite(ColumnTypeHelper helper, WriteRowsEvent event, RedisSchnauzer table) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		LOGGER.info(rows.size() + Infos.Row);
		int scoreIndex = table.getMemberIndex();
		int memberIndex = table.getScoreIndex();
		int valueIndex = table.getValueIndex();
		Double score = 0.0;
		String member = "";
		String value = "";
		for (int j=0; j<rows.size(); j++)
		{
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!table.needReplicate(columns, helper)) continue;
			switch(table.getType()) {
			case Set :
				value = helper.getColStr(valueIndex, columns.get(valueIndex), (byte)1, table.masterfields.get(valueIndex));
				dbhelper.sadd(table.getKey(columns), value);
				dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
				break;
			case SortedSet:
				score = Double.parseDouble(helper.getColStr(scoreIndex, columns.get(scoreIndex), (byte)1, table.masterfields.get(scoreIndex)));
				member = helper.getColStr(memberIndex, columns.get(memberIndex), (byte)1, table.masterfields.get(memberIndex));
				dbhelper.zincrby(table.getKey(columns), score, member);
				dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
				break;
			case List:
				ErrorHelper.errExit("innerWrite====" + Infos.Illegal + "RedisStructure:" + table.getType().toString());
				break;
			default : 
				ErrorHelper.errExit("innerWrite====" + Infos.Illegal + "RedisStructure:" + table.getType().toString());
				break;
			}
		}
	}
	
	@Override
	public boolean doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		boolean doOne = false;
		for (int i=0; i<tables.size(); i++)
		{
			RedisSchnauzer table =  tables.get(i);
			if (StrHelp.notEqual(helper.databaseName, masterDb.dbname)) continue;
			if (StrHelp.notEqual(helper.tableName, table.getMasterTableName())) continue;
			doOne = true;
			try {
				innerWrite(helper, event, table);
			} catch(Exception e) {
				ErrorHelper.errExit(Infos.DoInsert + Infos.Failed + e.getMessage());
			}
		}
		
		if (!doOne) 
			dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
		return true;
	}

	private void innerUpdate(ColumnTypeHelper helper, UpdateRowsEvent event, RedisSchnauzer table) throws UnsupportedEncodingException {
		List<Pair<Row>> rows =  event.getRows();
		LOGGER.info(rows.size() + Infos.Row);
		int scoreIndex = table.getMemberIndex();
		int memberIndex = table.getScoreIndex();
		int valueIndex = table.getValueIndex();
		Double score = 0.0;
		Double preScore = 0.0;
		String member = "";
		String value = "";
		for (int j=0; j<rows.size(); j++)
		{
            Pair<Row> pRow = rows.get(j);  
            Row row = pRow.getAfter();
            Row preRow = pRow.getBefore();
            
			List<Column> columns = row.getColumns();
			List<Column> pcolumns = preRow.getColumns(); 
			
			if (!table.needReplicate(columns, helper)) continue;
			
			switch(table.getType()) {
			case Set :
				value = helper.getColStr(valueIndex, columns.get(valueIndex), (byte)1, table.masterfields.get(valueIndex));
				dbhelper.sadd(table.getKey(columns), value);
				dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
				break;
			case SortedSet:
				score = Double.parseDouble(helper.getColStr(scoreIndex, columns.get(scoreIndex), (byte)1, table.masterfields.get(scoreIndex)));
				preScore = Double.parseDouble(helper.getColStr(scoreIndex, pcolumns.get(scoreIndex), (byte)1, table.masterfields.get(scoreIndex)));
				member = helper.getColStr(memberIndex, columns.get(memberIndex), (byte)1, table.masterfields.get(memberIndex));
				dbhelper.zincrby(table.getKey(columns), -1* preScore, member);
				dbhelper.zincrby(table.getKey(columns), score, member);
				dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
				break;
			case List:
				ErrorHelper.errExit("innerUpdate====" + Infos.Illegal + "RedisStructure:" + table.getType().toString());
				break;
			default : 
				ErrorHelper.errExit("innerUpdate====" + Infos.Illegal + "RedisStructure:" + table.getType().toString());
				break;
			}
		}
	}
	
	@Override
	public boolean doUpdate(ColumnTypeHelper helper, UpdateRowsEvent event) {
		boolean doOne = false;
		for (int i=0; i<tables.size(); i++)
		{
			RedisSchnauzer table =  (RedisSchnauzer)tables.get(i);
			if (StrHelp.notEqual(helper.databaseName, this.masterDb.dbname)) continue;
			if (StrHelp.notEqual(helper.tableName, table.getMasterTableName())) continue;
			doOne = true;
			try {
				innerUpdate(helper, event, table);
			} catch(Exception e) {
				ErrorHelper.errExit(Infos.DoUpdate + Infos.Failed + e.getMessage());
			}
		}
		
		if (!doOne) 
			dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
		return true;
	}
	
	private void innerDelete(ColumnTypeHelper helper, DeleteRowsEvent event, RedisSchnauzer table) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		LOGGER.info(rows.size() + Infos.Row);
		int scoreIndex = table.getMemberIndex();
		int memberIndex = table.getScoreIndex();
		int valueIndex = table.getValueIndex();
		Double score = 0.0;
		String member = "";
		String value = "";
		for (int j=0; j<rows.size(); j++)
		{
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!table.needReplicate(columns, helper)) continue;
			switch(table.getType()) {
			case Set :
				value = helper.getColStr(valueIndex, columns.get(valueIndex), (byte)1, table.masterfields.get(valueIndex));
				dbhelper.sadd(table.getKey(columns), value);
				dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
				break;
			case SortedSet:
				score = Double.parseDouble(helper.getColStr(scoreIndex, columns.get(scoreIndex), (byte)1, table.masterfields.get(scoreIndex)));
				member = helper.getColStr(memberIndex, columns.get(memberIndex), (byte)1, table.masterfields.get(memberIndex));
				dbhelper.zincrby(table.getKey(columns), -1*score, member);
				dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
				break;
			case List:
				ErrorHelper.errExit("innerDelete====" + Infos.Illegal + "RedisStructure:" + table.getType().toString());
				break;
			default : 
				ErrorHelper.errExit("innerDelete====" + Infos.Illegal + "RedisStructure:" + table.getType().toString());
				break;
			}
		}
	}
	

	@Override
	public boolean doDelete(ColumnTypeHelper helper, DeleteRowsEvent event) {
		boolean doOne = false;
		for (int i=0; i<tables.size(); i++)
		{
			RedisSchnauzer table =  (RedisSchnauzer)tables.get(i);
			if (StrHelp.notEqual(helper.databaseName, this.masterDb.dbname)) continue;
			if (StrHelp.notEqual(helper.tableName, table.getMasterTableName())) continue;
			doOne = true;
			try {
				innerDelete(helper, event, table);
			} catch(Exception e) {
				ErrorHelper.errExit(Infos.DoDelete + Infos.Failed + e.getMessage());
			}
		}
		
		if (!doOne) 
			dbhelper.setBinlogKey(helper.binlogFileName.toString(), Long.toString(helper.position), helper.tableName);
		return true;
	}

}
