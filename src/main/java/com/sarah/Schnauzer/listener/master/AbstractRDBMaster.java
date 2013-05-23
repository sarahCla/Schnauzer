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
package com.sarah.Schnauzer.listener.master;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.glossary.column.BitColumn;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.sarah.Schnauzer.heartbeat.HeartBeatInfo;
import com.sarah.Schnauzer.heartbeat.HeartBeatSender;
import com.sarah.Schnauzer.helper.ConfigGetHelper;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.Tags;
import com.sarah.Schnauzer.helper.WarmingMailHelper;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.SlaveHelperFactory;
import com.sarah.Schnauzer.listener.ColumnTypeHelper;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RDBSchnauzer;
import com.sarah.tools.cuid.CUID;
import com.sarah.tools.localinfo.LocalInfo;
import com.sarah.tools.localinfo.LocalInfoGetter;

/**
 * 
 * @author SarahCla
 */
public abstract class AbstractRDBMaster implements IMaster {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRDBMaster.class);	
	
	protected DBConnectorConfig slaveDb;	
	protected DBConnectorConfig masterDb;
	
	private ISlaveDbHelper dbhelper;
	private WarmingMailHelper mailsender;	
	
	private long lSysTime = 0;
	
	
	protected List<ITableReplicator> tables = new CopyOnWriteArrayList<ITableReplicator>();
	
	public AbstractRDBMaster(DBConnectorConfig master, DBConnectorConfig slave)
	{
		this.slaveDb = slave;
		this.masterDb = master;
		this.dbhelper = new SlaveHelperFactory(slave); 
		this.mailsender = new WarmingMailHelper("Config.xml");
		registgerTableReplicator(master);
	}
	
	private String getBinLogName(ColumnTypeHelper helper) {
		if (helper.binlogFileName==null)
			return slaveDb.binlog;
		else
			return helper.binlogFileName.toString();
	}
	
	private String getWarning(String[] sqls, String[] errInfo) {
		String msg = Infos.FailedEmailTitle + "<br>";
		for (int i=1; i<sqls.length; i++) {
			if ((sqls[i]==null)||(sqls[i].isEmpty())) continue;
			msg += sqls[i] + "<br>";
		}
		msg += Infos.ErrorType + "<br>" + errInfo[0] + "<br>" + "Version " + Tags.version; 
		msg += "<br>";
		msg += Infos.Config + "<br>Slave" + Infos.Type + ":" + this.slaveDb.getType() +
				"<br>Slave: " + this.slaveDb.host +
				"<br>Slave database: " + this.slaveDb.dbname +
				"<br>Master: " + this.masterDb.host +
				"<br>Master database: " + this.masterDb.dbname + "<br>";
		msg += Infos.CurPos + "<br>";
		msg += "BinlogFile : " + this.slaveDb.binlog + "<br>";
		msg += "Position : " + this.slaveDb.pos + "<br>";
		return  msg;
	}
	
	private String getStatusUpdateSql(ColumnTypeHelper helper) {
		return "update RepStatus set pos=" + helper.position + " , binlog='" + this.getBinLogName(helper) + "' Where masterID=" + slaveDb.masterID;
	}
	
	private String getAutoSetPosSql(ColumnTypeHelper helper) {
		return "update RepStatus set pos=" + helper.tableMapPos + " , binlog='" + this.getBinLogName(helper) + "' where masterID=" + slaveDb.masterID;
	}
	
	private void autoSetPosition(ColumnTypeHelper helper) {
		if ((System.currentTimeMillis()-lSysTime)<30000) return;
		String[] sqls = new String[1];
		sqls[0] = getAutoSetPosSql(helper);
		String[] errInfo = new String[1];
		if (dbhelper.excuteSqlByTransaction(sqls, errInfo))
		{
			this.slaveDb.binlog = this.getBinLogName(helper);
			this.slaveDb.pos = helper.position;
			LOGGER.info(Infos.AutoSetPos + Infos.OK + ":" + sqls[0]);
		} else
			LOGGER.error(Infos.AutoSetPos + Infos.Failed + ":" + sqls[0]);
		lSysTime = System.currentTimeMillis();
	}
	
	private String[] getCommInsertSql(ColumnTypeHelper helper, WriteRowsEvent event, RDBSchnauzer rep) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		BitColumn bCol = event.getUsedColumns().clone();
		String[] sql = new String[1]; 
		sql[0] = rep.getInsertFields(bCol);
		rep.setUsedColumn(bCol);
		byte[] unsigntags = rep.getUnsignedTags();
		String sqlValue = "";			
		LOGGER.info(rows.size() + Infos.Row);
		for (int j=0; j<rows.size(); j++)
		{
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			if (!sqlValue.isEmpty()) sqlValue += ",";
			if (rep.haveCUIDDefault())
				sqlValue += "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + CUID.getCuid(this.slaveDb.info) + ")";
			else
				sqlValue += "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + ")";
		}
		if (sqlValue.isEmpty()) {
			sql[0] = "";
			return sql;
		}
		sql[0] += sqlValue;
		return sql;
	}
	
	private String getWhereStr(ITableReplicator rep, List<Column> columns, ColumnTypeHelper helper) throws UnsupportedEncodingException {
		String sql = "";
		int[] indexs = rep.getKeyFieldIndexs();
		byte[] unsigntags = rep.getUnsignedTags();
		int pos = 0;
		for (int i=0; i<indexs.length; i++) {
			pos = indexs[i];
			if (!sql.isEmpty()) sql += " and ";
			sql += rep.getIDName(i) + "=" + helper.getColStr(pos, columns.get(pos), unsigntags[pos], rep.getFullFields().get(pos));
		}
		return sql;
	}

	private String[] getInsUpSqlSql(ColumnTypeHelper helper, WriteRowsEvent event, RDBSchnauzer rep) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		BitColumn bCol = event.getUsedColumns().clone();
		String InsertSql = rep.getInsertFields(bCol);
		String sIfExistPre = " set nocount off  if not exists(select 1 from " + rep.getSlaveTableName() + " where ";
		String updatePre = "   UPDATE " + rep.getSlaveTableName() + " SET "; 
		String[] sql = new String[rows.size()*4];
		rep.setUsedColumn(bCol);
		byte[] unsigntags = rep.getUnsignedTags();
		String rowSql = "";			
		LOGGER.info(rows.size() + Infos.Row);
		int index = 0;
		for (int j=0; j<rows.size(); j++)
		{
			rowSql = "";
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			rowSql = sIfExistPre + getWhereStr(rep, columns, helper) + ")  ";
			if (rep.haveCUIDDefault()) {
				if (rep.isCUIDDefaultText())
					rowSql += InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + "'" + CUID.getCuid(this.slaveDb.info) + "')";
				else
					rowSql += InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + CUID.getCuid(this.slaveDb.info) + ")";
			}
			else
				rowSql += InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + ")";
			rowSql += " else ";
			rowSql += updatePre + helper.getUpdataStr(columns, rep.getColumnNames(), unsigntags, rep.getFullFields()) +
					" where " + getWhereStr(rep, columns, helper) + "   ";
			sql[index++] = rowSql;
		} 
		return sql;
	}

	private String[] getSql2000Sql(ColumnTypeHelper helper, WriteRowsEvent event, RDBSchnauzer rep) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		BitColumn bCol = event.getUsedColumns().clone();
		String InsertSql = rep.getInsertFields(bCol);
		String[] sql = new String[rows.size()*4];
		rep.setUsedColumn(bCol);
		byte[] unsigntags = rep.getUnsignedTags();
		String rowSql = "";			
		LOGGER.info(rows.size() + Infos.Row);
		int index = 0;
		for (int j=0; j<rows.size(); j++)
		{
			rowSql = "";
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			if (rep.haveCUIDDefault()) {
				if (rep.isCUIDDefaultText())
					rowSql += InsertSql +  " (" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + "'" + CUID.getCuid(this.slaveDb.info) + "')";
				else
					rowSql += InsertSql +  " (" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + CUID.getCuid(this.slaveDb.info) + ")";
			}
			else
				rowSql += InsertSql +  "  (" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + ")";
			sql[index++] = rowSql;
		} 
		return sql;
	}
	
	private String[] getInsertUpdateSql(ColumnTypeHelper helper, WriteRowsEvent event, RDBSchnauzer rep) throws UnsupportedEncodingException {
		List<Row> rows = event.getRows();
		BitColumn bCol = event.getUsedColumns().clone();
		String InsertSql = rep.getInsertFields(bCol);
		String[] sql = new String[rows.size()];
		rep.setUsedColumn(bCol);
		byte[] unsigntags = rep.getUnsignedTags();
		String rowSql = "";			
		LOGGER.info(rows.size() + Infos.Row);
		int index = 0;
		for (int j=0; j<rows.size(); j++)
		{
			rowSql = "";
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			if (rep.haveCUIDDefault())
				rowSql = InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + CUID.getCuid(this.slaveDb.info) + ")";
			else
				rowSql = InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + ")";
			rowSql += "  ON duplicate KEY UPDATE " + helper.getUpdataStr(columns, rep.getColumnNames(), unsigntags, rep.getFullFields());
			sql[index++] = rowSql;
		}
		return sql;
	}
	
	
	private String[] getInsertSql(ColumnTypeHelper helper, WriteRowsEvent event, ITableReplicator rep) throws UnsupportedEncodingException {
		RDBSchnauzer table = (RDBSchnauzer)rep;
		if (rep.isMergeTable() && slaveDb.isMySQL()) {
			return getInsertUpdateSql(helper, event, table);
		} else if (rep.isMergeTable() && slaveDb.isSQLServer()) {
			return getInsUpSqlSql(helper, event, table);
		} else if (this.slaveDb.isSQLServer2000()) {
			return getSql2000Sql(helper, event, table);			
		} else {
			return getCommInsertSql(helper, event, table); 
		}
	}
	
	private boolean logSql(String[] sqls) {
		boolean ret = false;
		if (sqls==null) return ret;
		for (int i=0; i<sqls.length; i++) {
			if ((sqls[i]==null) || (sqls[i].isEmpty())) continue;
			LOGGER.info(sqls[i]);
			ret = true;
		}
		return ret;
	}
	
	private void rollBackToTableEvent(ColumnTypeHelper helper) {
		String retInfo = "";
		dbhelper.executeSql("update " + Tags.repTable + " set pos=" + helper.tableMapPos + " , binlog='" + this.getBinLogName(helper) + "' where masterID=" + slaveDb.masterID, retInfo);
	}
	
	private void mailAndLog(String title, String context) {
		mailsender.send(Infos.RepFailed+ slaveDb.host + title, context );
		context = context.replaceAll("<br>", System.getProperty("line.separator"));
		LOGGER.error(title + context);
	}
	
	private boolean isEqualS(String s1, String s2) {
		return s1.equalsIgnoreCase(s2);
	}
	
	private boolean notEqualS(String s1, String s2) {
		return (!s1.equalsIgnoreCase(s2));
	}
	
	
	@Override
	public boolean doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		boolean doOne = false;
		String[] sqls = null;
		int index = 0;
		for (int i=0; i<tables.size(); i++)
		{
			ITableReplicator rep =  tables.get(i);
			if (notEqualS(helper.databaseName, masterDb.dbname)) continue;
			if (notEqualS(helper.tableName, rep.getMasterTableName())) continue;
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
		String preSql = "";
		String aftSql = "";
		boolean doOne = false;
		String[] sqls = null;
		int sqlsIndex = 0;
		for (int i=0; i<tables.size(); i++)
		{
			RDBSchnauzer rep =  (RDBSchnauzer)tables.get(i);
			if (notEqualS(helper.databaseName, this.masterDb.dbname)) continue;
			if (notEqualS(helper.tableName, rep.getMasterTableName())) continue;
			List<Pair<Row>> rows =  event.getRows();
			String[] tableSqls = new String[rows.size()+1];
			int pos=1;
			byte[] unsigntags = rep.getUnsignedTags();
			for (int j=0; j<rows.size(); j++)
			{
				String sql = "Update " + rep.getSlaveTableName() + "  set ";
	            Pair<Row> pRow = rows.get(j);  
	            Row row = pRow.getAfter();
	            Row preRow = pRow.getBefore();
	            
    			List<Column> columns = row.getColumns();
    			List<Column> pcolumns = preRow.getColumns(); 
    			if (!rep.needReplicate(columns)) continue;
    			try {
        			preSql = helper.getUpdataStr(pcolumns, rep.getColumnNames(), unsigntags, rep.getFullFields());
        			aftSql = helper.getUpdataStr(columns, rep.getColumnNames(), unsigntags, rep.getFullFields());
    			} catch(Exception e) {
    				mailAndLog(Infos.Create + Infos.DoUpdate + Infos.Failed, e.getMessage());
    				return false;
    			}
    			
    			if (isEqualS(preSql, aftSql)) continue;
    			sql += aftSql;
    			try {
    				sql += " where " + getWhereStr(rep, columns, helper);
    			} catch(Exception e) {
    				mailAndLog(Infos.Create + Infos.DoUpdate + Infos.Failed, e.getMessage());
    				return false;
    			}
    				
    			tableSqls[pos++] = sql;
    			doOne = true;
			}
			if (sqls==null) {
				 sqls = new String[tables.size()*rows.size()+1];
				 for (int k=0; k<sqls.length; k++) sqls[k]="";
				 sqls[0] = getStatusUpdateSql(helper);     				 
			}
			int sIndex = 0;
			for (int j=0; j<tableSqls.length; j++) {
				sIndex = (sqlsIndex+1);
				if ((tableSqls[j]==null) || (tableSqls[j].isEmpty())) continue;
				if ((sqls[sIndex]!=null) && (!sqls[sIndex].isEmpty())) sqls[i+1] += ";";
				sqls[++sqlsIndex] += tableSqls[j]; 
			}
		}
		
		String[] errInfo = new String[1];
		
		if (!doOne || !logSql(sqls)) {
			autoSetPosition(helper);
			return true;
		}
		
		if (dbhelper.excuteSqlByTransaction(sqls, errInfo))
		{
			this.slaveDb.binlog = this.getBinLogName(helper);
			this.slaveDb.pos = helper.position;
			LOGGER.info(Infos.DoUpdate + Infos.OK);
			return true;
		}
		
		String sInfo = getWarning(sqls, errInfo);
		mailAndLog(Infos.DoUpdate + Infos.Failed, sInfo);
		this.slaveDb.errorMsg = "Update command faild";
		HeartBeatInfo hinfo = new HeartBeatInfo();
		ConfigGetHelper conf = new ConfigGetHelper();
		conf.getHeartBeatSet(hinfo);			
		if (hinfo.port>0 && !hinfo.host.isEmpty()) {
			LOGGER.info(Infos.SendErrorInfo);
			LocalInfo info = LocalInfoGetter.getLocalInfo();
			hinfo.SerialNo = info.getSNStr();
			HeartBeatSender  beatSender = new HeartBeatSender(this.masterDb, this.slaveDb, hinfo);
			beatSender.sendError();
		}
		rollBackToTableEvent(helper);
		return false;
	}

	@Override
	public boolean doDelete(ColumnTypeHelper helper, DeleteRowsEvent event) {
		boolean need = false;
		String[] sqls = null;
		int index = 0;
		for (int i=0; i<tables.size(); i++)
		{
			RDBSchnauzer rep =  (RDBSchnauzer)tables.get(i);
			if (notEqualS(helper.databaseName, masterDb.dbname)) continue;
			if (notEqualS(helper.tableName, rep.getMasterTableName())) continue;
			List<Row> rows = event.getRows();
			for (int j=0; j<rows.size(); j++)
			{
    			Row row = rows.get(j);
    			List<Column> columns = row.getColumns();
    			if (sqls==null) {
    				sqls = new String[rows.size()*tables.size()+1];
    				sqls[0] = getStatusUpdateSql(helper);
    			}
    			need = true;
    			try {
	   				sqls[++index] = rep.getDelete() + getWhereStr(rep, columns, helper);
				} catch(Exception e) {
					mailAndLog(Infos.Create + Infos.DoDelete + Infos.Failed, e.getMessage());
					return false;
				}
			}
		}
		
		String[] errInfo = new String[1];
		
		if (!need || !logSql(sqls)) {
			autoSetPosition(helper);
			return true;
		}
		
		if (dbhelper.excuteSqlByTransaction(sqls, errInfo))
		{
			slaveDb.binlog = this.getBinLogName(helper);
			slaveDb.pos = helper.position;
			LOGGER.info(Infos.DoDelete + Infos.OK);
			return true;
		}

		String sInfo = getWarning(sqls, errInfo);
		mailAndLog(Infos.DoDelete + Infos.Failed, sInfo);
		rollBackToTableEvent(helper);
		return false;
	}
	
}