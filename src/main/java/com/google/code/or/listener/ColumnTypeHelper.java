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
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.column.BitColumn;
import com.google.code.or.common.util.MySQLConstants;

/**
 * 
 * @author SarahCla
 */
public class ColumnTypeHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTableListener.class);

	public String databaseName = "";	
	public String tableName = "";
	public byte[] colTypes;
	
	public String getValueStr(List<Column> columns, BitColumn bCol) 
	{
		String sql = "";
		for (int i=0; i<columns.size(); i++)
		{
			if (!bCol.get(i)) continue;
			if (!sql.equalsIgnoreCase("")) sql += ", ";
			sql += getColStr(i, columns.get(i));
		}
		return sql;
	}
	
	public String getUpdataStr(List<Column> columns, String[] names)
	{
		String sql = "";
		for (int i=0; i<columns.size(); i++)
		{
			//if (!bCol.get(i)) continue;
			if (!sql.equalsIgnoreCase("")) sql += ", ";
			sql += names[i] + "=" + getColStr(i, columns.get(i));
		}
		return sql;
	}
	

	public String getColStr(int index, Column col)
	{
		if (col==null)
		{
			LOGGER.info("col is null====================");
			return "NULL";
		} else if (col.getValue()==null)
		{
			return "NULL";
		}
		String value = col.getValue().toString();
		switch (this.colTypes[index])
		{
			case MySQLConstants.TYPE_TIMESTAMP:
			case MySQLConstants.TYPE_DATE:
			case MySQLConstants.TYPE_TIME:	
			case MySQLConstants.TYPE_DATETIME:
			case MySQLConstants.TYPE_YEAR:		
			case MySQLConstants.TYPE_NEWDATE:		
			case MySQLConstants.TYPE_VARCHAR:
			case (byte)MySQLConstants.TYPE_VAR_STRING:
			case (byte)MySQLConstants.TYPE_STRING:
				value = "'" + col.toString() + "'";
				break;
		}
		return value;
	}
}	