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
package com.sarah.Schnauzer.helper.DB;

import java.sql.*;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLDbHelper;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLSlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.Redis.RedisSlaveDBHelper;
import com.sarah.Schnauzer.helper.DB.SqlServer.SqlServerSlaveDbHelper;


/**
 * 
 * @author SarahCla
 */
public class SlaveHelperFactory implements ISlaveDbHelper {
	private DBConnectorConfig dbconfig;
	private ISlaveDbHelper helper = null;
	
	public MySQLDbHelper getMySQLHelper() {
		if (dbconfig.isMySQL())
			return (MySQLSlaveDbHelper)this.helper;
		else
			return null;
	}
	
	public SlaveHelperFactory(DBConnectorConfig dbconfig) {
		this.dbconfig = dbconfig;
		this.helper = createHelper();
	}
	
	private ISlaveDbHelper createHelper() {
		if (dbconfig.isMySQL()) {
			this.helper = new MySQLSlaveDbHelper(dbconfig);
		} else if (dbconfig.isSQLServer()) {
			this.helper = new SqlServerSlaveDbHelper(dbconfig);
		} else if (dbconfig.isRedis()) {
			this.helper = new RedisSlaveDBHelper(dbconfig);
		}
		return this.helper;
	}

	@Override
	public SlaveStatus getSlaveStatus() throws Exception {
		return this.helper.getSlaveStatus();
	}

	@Override
	public boolean doOpen() {
		return this.helper.doOpen();
	}

	@Override
	public ResultSet getRS(String sql) {
		return this.helper.getRS(sql);
	}

	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo) {
		return this.helper.excuteSqlByTransaction(sqlStr, errInfo);
	}

	@Override
	public ResultSet getTableFields(String TableName) {
		return this.helper.getTableFields(TableName);
	}

	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo, boolean checkRowCount) {
		return this.helper.excuteSqlByTransaction(sqlStr, errInfo, checkRowCount);
	}

	@Override
	public boolean executeSql(String sql, String retInfo) {
		return this.helper.executeSql(sql, retInfo);
	}
}