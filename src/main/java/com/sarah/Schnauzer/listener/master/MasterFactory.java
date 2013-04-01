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

import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.sarah.Schnauzer.listener.ColumnTypeHelper;


/**
 * 
 * @author SarahCla
 */
public class MasterFactory implements IMasterFactory {

	private IMaster[] masters;
	
	@Override
	public IMaster[] getMasters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doOnEvents(BinlogEventV4 event) {
		for(int i=0; i<this.masters.length; i++) {
			
		}
	}

	public void doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		for(int i=0; i<this.masters.length; i++) {
			
		}
	}

	public void doUpdate(ColumnTypeHelper helper, UpdateRowsEvent event) {
		for(int i=0; i<this.masters.length; i++) {
			
		}
	}

	public void doDelete(ColumnTypeHelper helper, DeleteRowsEvent event) {
		for(int i=0; i<this.masters.length; i++) {
			
		}
	}
}