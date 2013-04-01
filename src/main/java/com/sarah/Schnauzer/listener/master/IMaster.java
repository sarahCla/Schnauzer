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

import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.listener.ColumnTypeHelper;

/**
 * 
 * @author SarahCla
 */
public interface IMaster {
	public String getHost();
	public int getPort();
	public String getUser();
	public String getPassword();
	

	public boolean registgerTableReplicator(DBConnectorConfig master);
	
	public boolean doWrite(ColumnTypeHelper helper, WriteRowsEvent event);
	public boolean doUpdate(ColumnTypeHelper helper, UpdateRowsEvent event);
	public boolean doDelete(ColumnTypeHelper helper, DeleteRowsEvent event);
}