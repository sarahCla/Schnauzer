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
package com.google.code.or.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.BinlogEventListener;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.glossary.column.LongColumn;
import com.google.code.or.common.glossary.column.StringColumn;
import com.google.code.or.common.util.MySQLConstants;
import com.google.code.or.listener.slave.ISlave;
import com.google.code.or.listener.slave.Impl.RollHallSlave;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;

/**
 * 
 * @author SarahCla
 */
public class ClientTableListener implements BinlogEventListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTableListener.class);
	private ColumnTypeHelper helper = new ColumnTypeHelper();
	private ISlave slave = new RollHallSlave();
	
	
	
	private void setHelper(ColumnTypeHelper helper, TableMapEvent event)
	{
		helper.colTypes = event.getColumnTypes();
		helper.tableName = event.getTableName().toString();
		helper.databaseName = event.getDatabaseName().toString();
		//helper.colNames = event.
	}
	
	
	
	@Override
	public void onEvents(BinlogEventV4 event) {
		int type = event.getHeader().getEventType(); 
    	if (type==MySQLConstants.TABLE_MAP_EVENT) {
    		setHelper(helper, (TableMapEvent)event);
    		LOGGER.info("{}", event);
    	} else if (type==MySQLConstants.WRITE_ROWS_EVENT) {
    		slave.doWrite(helper, (WriteRowsEvent)event);
    	} else if (type==MySQLConstants.DELETE_ROWS_EVENT) {
    		slave.doDelete(helper, (DeleteRowsEvent)event);
    	} else if (type==MySQLConstants.UPDATE_ROWS_EVENT) {
    		slave.doUpdate(helper,  (UpdateRowsEvent)event);
    	}
	}
	

}
