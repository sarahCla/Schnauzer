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
package com.sarah.Schnauzer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.BinlogEventListener;
import com.google.code.or.common.util.MySQLConstants;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.RotateEvent;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.listener.master.IMaster;
import com.sarah.Schnauzer.listener.master.Impl.SchnauzerRDBMaster;
import com.sarah.Schnauzer.listener.master.Impl.SchnauzerRedisMaster;
import com.sarah.Schnauzer.listener.master.Impl.SchnauzerSASMaster;

/**
 * 
 * @author SarahCla
 */
public class ClientTableListener implements BinlogEventListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTableListener.class);
	private ColumnTypeHelper helper = new ColumnTypeHelper();
	private IMaster slave;
	
	
	public ClientTableListener(DBConnectorConfig masterdb, DBConnectorConfig slavedb, String args[]) 
	{
		if (slavedb.isRedis()) 
			this.slave = new SchnauzerRedisMaster(masterdb, slavedb, args);
		else if (masterdb.SchnauzerClass.equalsIgnoreCase("SchnauzerSASMaster"))
		{
			this.slave = new SchnauzerSASMaster(masterdb, slavedb);
		} else {
			this.slave = new SchnauzerRDBMaster(masterdb, slavedb);
		}
		
		helper.dateFormatStr = masterdb.DateFormat;
	}
	
	private void setHelper(ColumnTypeHelper helper, TableMapEvent event)
	{
		helper.colTypes = event.getColumnTypes();
		helper.tableName = event.getTableName().toString();
		helper.databaseName = event.getDatabaseName().toString();
		helper.position = event.getHeader().getNextPosition();
		helper.tableMapPos = event.getHeader().getPosition();
	}

	private void setHelper(ColumnTypeHelper helper, WriteRowsEvent event)
	{
		helper.position = event.getHeader().getPosition();
	}
	
	private void setHelper(ColumnTypeHelper helper, DeleteRowsEvent event)
	{
		helper.position = event.getHeader().getPosition();
	}

	private void setHelper(ColumnTypeHelper helper, UpdateRowsEvent event)
	{
		helper.position = event.getHeader().getPosition();
	}
	
	private void setHelper(ColumnTypeHelper helper, RotateEvent event)
	{
		helper.position = event.getBinlogPosition();
		helper.binlogFileName = event.getBinlogFileName();
	}
	
	
	
	@Override
	public void onEvents(BinlogEventV4 event) {
		int type = event.getHeader().getEventType(); 
		switch(type) {
		case MySQLConstants.TABLE_MAP_EVENT:
			setHelper(helper, (TableMapEvent)event);
			break;
		case MySQLConstants.WRITE_ROWS_EVENT:
			setHelper(helper, (WriteRowsEvent)event);
			if (!slave.doWrite(helper, (WriteRowsEvent)event)) {
				LOGGER.info(Infos.Stopped);
				System.exit(-1);
			}
			break;
		case MySQLConstants.DELETE_ROWS_EVENT:
			setHelper(helper, (DeleteRowsEvent)event);
    		slave.doDelete(helper, (DeleteRowsEvent)event);
			break;
		case MySQLConstants.UPDATE_ROWS_EVENT:
			setHelper(helper, (UpdateRowsEvent)event);
    		if (!slave.doUpdate(helper,  (UpdateRowsEvent)event)) {
    			LOGGER.info(Infos.Stopped);
    			System.exit(-1);
    		}
			break;
		case MySQLConstants.ROTATE_EVENT:
			setHelper(helper, (RotateEvent)event);
			break;
		}
	}
	

}
