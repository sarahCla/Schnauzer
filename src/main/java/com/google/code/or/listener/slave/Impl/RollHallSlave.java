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
package com.google.code.or.listener.slave.Impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.binlog.BinlogEventParser;
import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.BinlogEventListener;
import com.google.code.or.binlog.BinlogParserListener;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.glossary.column.Int24Column;
import com.google.code.or.common.glossary.column.LongColumn;
import com.google.code.or.common.glossary.column.StringColumn;
import com.google.code.or.common.util.MySQLConstants;
import com.google.code.or.listener.ColumnTypeHelper;
import com.google.code.or.listener.TableReplicator.ITableReplicator;
import com.google.code.or.listener.TableReplicator.Impl.ptype_Rep;
import com.google.code.or.listener.slave.AbstractSlave;
import com.google.code.or.listener.slave.ISlave;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;

/**
 * 
 * @author SarahCla
 */
public class RollHallSlave extends AbstractSlave implements ISlave {
	
	@Override
	public String getHost() {
		return "trdubuntuserver";
	}

	@Override
	public int getPort() {
		return 3306;
	}

	@Override
	public String getUser() {
		return "root";
	}

	@Override
	public String getPassword() {
		return "dev";
	}

	@Override
	public void registgerTableReplicator() {
		this.tables.add(new ptype_Rep());
	}
}