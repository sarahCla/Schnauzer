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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sarah.Schnauzer.helper.DBConnectorConfig;

/**
 * 
 * @author SarahCla
 */
public abstract class AbstractDbHelper implements IDbHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbHelper.class);
	
	protected DBConnectorConfig conConfig;
	protected Connection conn;
	public String[] fieldTag = new String[2];
	
	public AbstractDbHelper(String host, int port, String user, String pwd, String dbname, String driver) {
		this.conConfig = new DBConnectorConfig(host, port, user, pwd, dbname, driver);
	}
	
	
	public AbstractDbHelper(DBConnectorConfig dbConfig) {
		this.conConfig = dbConfig;
	}


	@Override
	public boolean doOpen() {
		int waittime = conConfig.waittime;
		if (waittime>10) waittime -= 3;
		try {
			if (!this.conConfig.driver.isEmpty()) 
				Class.forName(this.conConfig.driver);
			if ((conn!=null) && (!conn.isClosed())) {
				if ((conConfig.isSQLServer2000()) || (conn.isValid(waittime))) return true;
				LOGGER.info("由于长时间没有通讯，数据库需要重新连接");
			}
			conn = DriverManager.getConnection(conConfig.getURL(), conConfig.user, conConfig.pwd);
			LOGGER.info("连接数据库：" + this.conConfig.getURL());
		} catch(Exception e) {   
			LOGGER.error("数据库连接失败2[" + this.conConfig.getURL() + "]" + e.getMessage());
			return false;
		}   
		return true;
	}

	@Override
	public boolean executeSql(String sql, String retInfo) {
        if(sql.isEmpty()) return false;
		this.doOpen();
        Statement stmt = null; 
        try
        {
            stmt = conn.createStatement();
            stmt.execute(sql);
        }
        catch(Exception ex) {
            retInfo = ex.toString();
            return false;
        }
        return true;
	}

	@Override
	public ResultSet getRS(String sql) {
		ResultSet rs=null;
		try {
			if (!this.doOpen()) return rs;
			Statement statement;
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
		} catch (Exception e) {
			LOGGER.error("获取数据集错误：" + e.getMessage());
		}
		return rs;
	}
}