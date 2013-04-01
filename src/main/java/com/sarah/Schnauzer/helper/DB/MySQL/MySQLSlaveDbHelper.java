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
package com.sarah.Schnauzer.helper.DB.MySQL;

import java.sql.*;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;

/**
 * 
 * @author SarahCla
 */
public class MySQLSlaveDbHelper extends MySQLDbHelper implements ISlaveDbHelper {
	
	public MySQLSlaveDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}

	@Override
	public ResultSet getSlaveStatus() {
		return getRS("select * from  RepStatus where masterID=" + this.conConfig.masterID);
	}

	@Override
	public ResultSet getTableFields(String TableName) {
		String sql = "select case when column_type like '%char%' then 1 else 0 end as isText, columns.* from information_schema.columns where table_name='" 
				+ TableName + "' and table_schema='" + this.conConfig.dbname 
				+ "' order by ordinal_position";
		return getRS(sql);
	}

}