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
package com.google.code.or.listener.slave;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.Pair;
import com.google.code.or.common.glossary.Row;
import com.google.code.or.common.glossary.column.BitColumn;
import com.google.code.or.listener.ColumnTypeHelper;
import com.google.code.or.listener.TableReplicator.ITableReplicator;
import com.google.code.or.listener.slave.ISlave;
import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;

/**
 * 
 * @author SarahCla
 */
public abstract class AbstractSlave implements ISlave {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSlave.class);	
	
	protected List<ITableReplicator> tables = new CopyOnWriteArrayList<ITableReplicator>();
	
	public AbstractSlave()
	{
		registgerTableReplicator();
	}
	

	@Override
	public void doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		for (int i=0; i<tables.size(); i++)
		{
			ITableReplicator rep =  tables.get(i);
			if (!helper.databaseName.equalsIgnoreCase(rep.getDBName())) continue;
			if (!helper.tableName.equalsIgnoreCase(rep.getTableName())) continue;			
			List<Row> rows =  event.getRows();
			BitColumn bCol =  event.getUsedColumns();
			String sql = rep.getInsertFields(bCol);
			String sqlValue = "";
			for (int j=0; j<rows.size(); j++)
			{
    			Row row = rows.get(j);    			
    			List<Column> columns = row.getColumns();
    			if (!rep.needReplicate(columns)) continue;
    			if (!sqlValue.equalsIgnoreCase("")) sqlValue += ",";
    			sqlValue += "(" + helper.getValueStr(columns, bCol) + ")";
			}
			sql += sqlValue;
			LOGGER.info("Replicate To: " + getHost() + "--Insert SQL--" +  sql);
		}
	}

	@Override
	public void doUpdate(ColumnTypeHelper helper, UpdateRowsEvent event) {
		for (int i=0; i<tables.size(); i++)
		{
			ITableReplicator rep =  tables.get(i);
			if (!helper.databaseName.equalsIgnoreCase(rep.getDBName())) continue;
			if (!helper.tableName.equalsIgnoreCase(rep.getTableName())) continue;
			List<Pair<Row>> rows =  event.getRows();
			for (int j=0; j<rows.size(); j++)
			{
				String sql = "Update " + rep.getTableName() + "  set ";
	            Pair<Row> pRow = rows.get(j);  
	            Row row = pRow.getAfter();
    			List<Column> columns = row.getColumns();
    			if (!rep.needReplicate(columns)) continue;
    			sql += helper.getUpdataStr(columns, rep.getColumnNames());
    			sql += " where " + rep.getIDName() + "=" + helper.getColStr(rep.getIDColIndex(), columns.get(rep.getIDColIndex()));
				LOGGER.info("Replicate To: " + getHost() + "--Update SQL-- " + sql);
			}
		}
	}

	@Override
	public void doDelete(ColumnTypeHelper helper, DeleteRowsEvent event) {
		for (int i=0; i<tables.size(); i++)
		{
			ITableReplicator rep =  tables.get(i);
			if (!helper.databaseName.equalsIgnoreCase(rep.getDBName())) continue;
			if (!helper.tableName.equalsIgnoreCase(rep.getTableName())) continue;
			List<Row> rows =  event.getRows();
			for (int j=0; j<rows.size(); j++)
			{
				String sql = "";
    			Row row = rows.get(i);
    			List<Column> columns = row.getColumns();
    			sql = rep.getDelete() + columns.get(rep.getIDColIndex()).getValue().toString();
				LOGGER.info("Replicate To: " + getHost() + "--Delete SQL--" + sql);
			}
		}
	}
	
}