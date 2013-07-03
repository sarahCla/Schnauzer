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
package com.sarah.Schnauzer.helper.DB.SqlServer;

import java.sql.*;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.Tags;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.SlaveStatus;
import com.sarah.tools.localinfo.LocalInfoGetter;

/**
 * 
 * @author SarahCla
 */
public class SqlServerSlaveDbHelper extends SqlServerDbHelper implements ISlaveDbHelper {

	public SqlServerSlaveDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}
	
	private void repStatusUpdate() {
		String retInfo = "";
		executeSql("if not exists(select 1 from syscolumns where id=object_id('" + Tags.repTable + "') and name='" + Tags.TableMapPos + "') " +
					" begin " +
					"    EXEC sp_rename '" + Tags.repTable + ".[masterdb]', '" + Tags.TableMapPos + "', 'COLUMN'; " +
					"    EXEC sp_rename '" + Tags.repTable + ".[ip]', '" + Tags.repTableComputerName + "', 'COLUMN'; " +
					"    EXEC sp_rename '" + Tags.repTable + ".[port]', '" + Tags.repTablePID + "', 'COLUMN'; " +
					" end", retInfo);	
	}
	

	@Override
	public SlaveStatus getSlaveStatus() {
		repStatusUpdate();
		SlaveStatus result = null;
		ResultSet rt = getRS("select * from  " + Tags.repTable + " where masterID=" + this.conConfig.masterID); 
		try {
			rt.next();
			if (rt.getRow()>=1) {
				result = new SlaveStatus(rt.getString(Tags.binlog), rt.getInt(Tags.TableMapPos), conConfig.masterID);
			} 
			String retInfo = "";
			this.executeSql(" update " + Tags.repTable + 
					        " Set " + Tags.repTableComputerName + "='" + LocalInfoGetter.getComputerName() + "', " + 
					        		  Tags.repTablePID + "='" + LocalInfoGetter.getPID() + 
					        "' Where masterID=" + this.conConfig.masterID , retInfo);
			
		}catch(SQLException e) {
			return null;
		}
		return result;
	}

	@Override
	public ResultSet getTableFields(String TableName) {
		String sql = "SELECT syscolumns.name  as column_name ,systypes.name,syscolumns.isnullable, syscolumns.length, systypes.xtype, systypes.xusertype  " +
				"  ,case when systypes.name like '%char%' then 1 else 0 end as isText " +
				"FROM syscolumns, systypes  " +
				"WHERE syscolumns.xusertype = systypes.xusertype  AND syscolumns.id = object_id('" + TableName + "')"; 
		return getRS(sql);
	}

}