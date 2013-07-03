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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.Tags;
import com.sarah.Schnauzer.helper.DB.AbstractDbHelper;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.SlaveStatus;
import com.sarah.tools.localinfo.LocalInfoGetter;

/**
 * 
 * @author SarahCla
 */
public class MySQLSlaveDbHelper extends MySQLDbHelper implements ISlaveDbHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MySQLSlaveDbHelper.class);
	
	public MySQLSlaveDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}

	private void repStatusTableUpdate() {
		ResultSet rt = getRS("show columns from " + Tags.repTable + " like '" + Tags.TableMapPos + "'");
		try {
			rt.next();
			if (rt.getRow()>=1) {
				rt.close();
				return;
			}
			rt.close();
			String retInfo = "";
			executeSql("alter table " + Tags.repTable + " change column masterdb " + Tags.TableMapPos + " varchar(100); ", retInfo);
			executeSql("alter table " + Tags.repTable + " change column ip " + Tags.repTableComputerName + " varchar(200); ", retInfo);
			executeSql("alter table " + Tags.repTable + " change column port " + Tags.repTablePID + " int; ", retInfo);
		}catch(SQLException e) {
			ErrorHelper.errExit(String.format(Infos.NoMasterRecord, conConfig.masterID, Tags.repTable));
		}
					
	}
	
	@Override
	public SlaveStatus getSlaveStatus() throws Exception{
		repStatusTableUpdate();
		SlaveStatus result = null;
		ResultSet rt = getRS("select * from  " + Tags.repTable + " where masterID=" + this.conConfig.masterID); 
		try {
			rt.next();
			if (rt.getRow()>=1) {
				result = new SlaveStatus(rt.getString(Tags.binlog), rt.getInt(Tags.TableMapPos), conConfig.masterID);
			} 
			String retInfo = "";
			String sql =
					" update " + Tags.repTable + 
					" Set " + Tags.repTableComputerName + "='" + LocalInfoGetter.getComputerName() + "', " + 
							  Tags.repTablePID + "='" + LocalInfoGetter.getPID() + 
				  "' Where masterID=" + this.conConfig.masterID ;
			
			this.executeSql(sql, retInfo);
			LOGGER.info(sql);
		}catch(SQLException e) {
			ErrorHelper.errExit(String.format(Infos.NoMasterRecord, conConfig.masterID, Tags.repTable));
		}
		return result;
	}

	@Override
	public ResultSet getTableFields(String TableName) {
		String sql = "select case when column_type like '%char%' then 1 else 0 end as isText, columns.* from information_schema.columns where table_name='" 
				+ TableName + "' and table_schema='" + this.conConfig.dbname 
				+ "' order by ordinal_position";
		return getRS(sql);
	}

}