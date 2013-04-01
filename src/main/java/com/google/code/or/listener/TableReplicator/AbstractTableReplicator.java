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
package com.google.code.or.listener.TableReplicator;


import com.google.code.or.common.glossary.column.BitColumn;
import com.google.code.or.listener.TableReplicator.ITableReplicator;

/**
 * 
 * @author SarahCla
 */
public abstract class AbstractTableReplicator implements ITableReplicator {
	
	@Override
	public String getInsertFields(BitColumn bCol) {
		String sql = "";
		String[] names = this.getColumnNames();
		for(int i=0; i<bCol.getLength(); i++)
		{
			if (!bCol.get(i)) continue;
			if (!sql.equalsIgnoreCase("")) sql += ",";
			sql += names[i];
		}
		return "insert into " + this.getTableName() + " (" + sql + " ) values ";
	}

	@Override
	public String getDelete() {
		// TODO Auto-generated method stub
		return "delete from " + this.getTableName() + " where " + this.getIDName() + "=";
	}

	@Override
	public int getColumnsCount() {
		String[] cols = this.getColumnNames();
		return cols.length;
	}
	
}