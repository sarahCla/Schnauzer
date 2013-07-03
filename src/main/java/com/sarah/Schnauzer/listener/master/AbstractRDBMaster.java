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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import java.util.Timer;

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
	
	private String[] insertSql = new String[2500];
	private int iSqlSize = 1;
	
	private long lSysTime = 0;
	
	private java.util.Timer InsertTimer;
	private Boolean isDoing = false;
	private Boolean isReSizing = false;
	Lock lock = null;  
	
	protected List<ITableReplicator> tables = new CopyOnWriteArrayList<ITableReplicator>();
	
	public AbstractRDBMaster(DBConnectorConfig master, DBConnectorConfig slave)
	{
		this.slaveDb    = slave;
		this.masterDb   = master;
		this.dbhelper   = new SlaveHelperFactory(slave); 
		this.mailsender = new WarmingMailHelper("Config.xml");
		registgerTableReplicator(master);
		
		lock = new ReentrantLock();  
		
		InsertTimer = new Timer(true);
		InsertTimer.schedule(
				new java.util.TimerTask() { 
					public void run() {
						if (isDoing) return;
						String[] errInfo = new String[1];
						if (iSqlSize<2 || System.currentTimeMillis()-lSysTime<1000) return;

						isDoing = true;
						
						lock.lock();
						
						while (isReSizing) {};
						
						isReSizing = true;
						String[] sqls = new String[iSqlSize];
						for (int i=0; i<iSqlSize; i++) {
							sqls[i] = insertSql[i];							
						}
						isReSizing = false;
						
						clearInsertSql();
						lock.unlock();
						
						Boolean ret = dbhelper.excuteSqlByTransaction(sqls, errInfo, false);
						lSysTime = System.currentTimeMillis(); 
						
						if (!ret) System.exit(-1);
						isDoing = false;
					} 
				}, 0, 500);
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
		msg += "TableMapPos : " + this.slaveDb.tablepos + "<br>";
		msg += "Position : " + this.slaveDb.pos + "<br>";
		return  msg;
	}
	
	private String getStatusUpdateSql(ColumnTypeHelper helper) {
		return " update " + Tags.repTable + 
				" set pos=" + helper.position + 
				" , binlog='" + this.getBinLogName(helper) + 
				"', " + Tags.TableMapPos + "='" + helper.tableMapPos + 
				"' Where masterID=" + slaveDb.masterID;
	}
	
	private String getAutoSetPosSql(ColumnTypeHelper helper) {
		return "update " + Tags.repTable + 
				" set pos=" + helper.tableMapPos + 
				" , binlog='" + this.getBinLogName(helper) +
				"', " + Tags.TableMapPos + "='" + helper.tableMapPos +
				"' where masterID=" + slaveDb.masterID;
	}
	
	private String getCuid(RDBSchnauzer rep) {
		String cuid = "";
		if (rep.haveCUIDDefault()) {
			cuid = CUID.getCuid(this.slaveDb.info);
			if (rep.isCUIDDefaultText()) cuid = "'" + cuid + "'";
		}
		return cuid;
	}
	
	private void autoSetPosition(ColumnTypeHelper helper) {

		if ((System.currentTimeMillis()-lSysTime)<0) {
			LOGGER.info("【System Datetime been changed!!!!!】");
		}
		if ((System.currentTimeMillis()-lSysTime)>0 && (System.currentTimeMillis()-lSysTime)<30000) return;
		
		String[] sqls = new String[1];
		sqls[0] = getAutoSetPosSql(helper);
		String[] errInfo = new String[1];
		if (dbhelper.excuteSqlByTransaction(sqls, errInfo, false))
		{
			this.slaveDb.binlog = this.getBinLogName(helper);
			this.slaveDb.pos = helper.position;
			this.slaveDb.tablepos = helper.tableMapPos;
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
		if (Tags.Verbose) LOGGER.info(rows.size() + Infos.Row);
		String cuid = "";
		for (int j=0; j<rows.size(); j++)
		{
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			if (!sqlValue.isEmpty()) sqlValue += ",";
			cuid = getCuid(rep);
			sqlValue += "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + cuid + ")";
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
		if (Tags.Verbose) LOGGER.info(rows.size() + Infos.Row);
		int index = 0;
		for (int j=0; j<rows.size(); j++)
		{
			rowSql = "";
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			rowSql = sIfExistPre + getWhereStr(rep, columns, helper) + ")  ";
			String cuid = getCuid(rep);
			rowSql += InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + cuid +")";
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
		if (Tags.Verbose) LOGGER.info(rows.size() + Infos.Row);
		int index = 0;
		String cuid = "";
		for (int j=0; j<rows.size(); j++)
		{
			rowSql = "";
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			cuid = getCuid(rep);
			rowSql += InsertSql +  "  (" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + cuid + ")";
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
		if (Tags.Verbose) LOGGER.info(rows.size() + Infos.Row);
		int index = 0;
		String cuid = "";
		for (int j=0; j<rows.size(); j++)
		{
			rowSql = "";
			Row row = rows.get(j);    			
			List<Column> columns = row.getColumns();
			if (!rep.needReplicate(columns)) continue;
			cuid = getCuid(rep);
			rowSql = InsertSql +  "(" + helper.getValueStr(columns, bCol, unsigntags, rep.getFullFields()) + rep.getDefStr() + cuid + ")";
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
			if (Tags.Verbose) LOGGER.info(sqls[i]);
			ret = true;
		}
		return ret;
	}
	
	private void rollBackToTableEvent(ColumnTypeHelper helper) {
		String retInfo = "";
		dbhelper.executeSql(" update " + Tags.repTable + 
				            " set pos=" + helper.tableMapPos + 
				            "," + Tags.TableMapPos + "='" + helper.tableMapPos +
				            "', binlog='" + this.getBinLogName(helper) + 
				            "' where masterID=" + slaveDb.masterID, retInfo);
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
	
	private void clearInsertSql() {
		while(isReSizing) {};
		isReSizing = true;
		for (int j=0; j<insertSql.length; j++) insertSql[j] = "";
		iSqlSize = 1;
		isReSizing = false;
	}
	
	private boolean doInsert(String sqls[], String[] errInfo) {
		
		//LOGGER.info("doInsert 1");
		
		lSysTime = System.currentTimeMillis();
		
		while (isReSizing) {};
		
		//LOGGER.info("doInsert 2");
		
		isReSizing = true;
		for (int i=0; i<sqls.length; i++) {
			if ((sqls[i]==null) || (sqls[i].isEmpty())) continue;
			if (i==0)
				insertSql[0] = sqls[0];
			else
				insertSql[iSqlSize++] = sqls[i];
		}
		isReSizing = false;
		
		if (iSqlSize>1000)
		{
			while (isReSizing) {};
			isReSizing = true;
			/*
			StringBuilder b = new StringBuilder(); 
			String[] s_ql = new String[2];
			for (int i=0; i<iSqlSize; i++) {
				if (i<1) {
					s_ql[i] = insertSql[i];
				} else {
					if (i==1)
						b.append(insertSql[i]);
					else
					{
						int l = insertSql[i].indexOf(" values (");
						//String s = insertSql[i].replaceAll("insert into ptype (`typeId`,`parTypeId`,`leveal`,`sonnum`,`soncount`,`CanModify`,`UserCode`,`p_barcode`,`p_bartype`,`barcode`,`FullName`,`Name`,`Standard`,`Type`,`Area`,`Unit1`,`Comment`,`deleted`,`RowIndex`,`id`,`parid`,`profileid`,`supplyInfo`,`preprice`,`preprice2`,`preprice3`,`preprice4`,`recPrice`,`recPrice1`,`recPriceBase`,`isStop`,`namePy`,`minPrice`,`prop1_enabled`,`prop2_enabled`,`prop3_enabled`,`taobao_cid`,`pic_url`,`createtype`,`technicalservicerate`,`feature1`,`feature2`,`feature3`,`modifiedTime` ) values", ",");
						String s = ", " + insertSql[i].substring(l+8);
						b.append(s);
					}
				}
			}
			s_ql[1] = b.toString();
			*/
			String[] s_ql = new String[iSqlSize];
			for (int i=0; i<iSqlSize; i++) {
				s_ql[i] = insertSql[i];
			}
			isReSizing = false;
			
			clearInsertSql();
			
			
			
			Boolean ret = dbhelper.excuteSqlByTransaction(s_ql, errInfo, false);

			lSysTime = System.currentTimeMillis();			
			
			if (!ret) {
				System.exit(-1);
			}
			return ret;
		}
		
		return true;
	}
	
	@Override
	public boolean doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		
		/*
		if (helper.position<=120619669)
			LOGGER.info("begin");
		else if 	(helper.position>=123198439) {
			LOGGER.info("end");
			return true;
		}
		*/
		
		boolean doOne = false;
		String[] sqls = null;
		int index = 0;
		doBeforeWrite(helper, event);
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
		if (doInsert(sqls, errInfo))
		{
			this.slaveDb.binlog = this.getBinLogName(helper);
			this.slaveDb.pos = helper.position;
			this.slaveDb.tablepos = helper.tableMapPos;
			if (Tags.Verbose) LOGGER.info(Infos.DoInsert + Infos.OK);
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
		doBeforeUpdate(helper, event);
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
			this.slaveDb.tablepos = helper.tableMapPos;
			LOGGER.info(Infos.DoUpdate + Infos.OK);
			return true;
		}
		
		String sInfo = getWarning(sqls, errInfo);
		mailAndLog(Infos.DoUpdate + Infos.Failed, sInfo);
		this.slaveDb.errorMsg = "Update command faild";
		HeartBeatInfo hinfo = new HeartBeatInfo();
		ConfigGetHelper conf = new ConfigGetHelper(Integer.toString(this.slaveDb.masterID));
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
		doBeforeDelete(helper, event);
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
			slaveDb.tablepos = helper.tableMapPos;
			LOGGER.info(Infos.DoDelete + Infos.OK);
			return true;
		}

		String sInfo = getWarning(sqls, errInfo);
		mailAndLog(Infos.DoDelete + Infos.Failed, sInfo);
		rollBackToTableEvent(helper);
		return false;
	}
	
}