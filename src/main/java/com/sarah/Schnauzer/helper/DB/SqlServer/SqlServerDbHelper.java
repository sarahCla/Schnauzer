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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.AbstractDbHelper;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLDbHelper;

/**
 * 
 * @author SarahCla
 */
public class SqlServerDbHelper extends AbstractDbHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MySQLDbHelper.class);
	
	public SqlServerDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
		this.conConfig.driver = dbConfig.driver; 
	}

	public SqlServerDbHelper(String host, int port, String user, String pwd, String dbname) {
		super(host, port, user, pwd, dbname, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
	}


	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo) {
		return excuteSqlByTransaction(sqlStr, errInfo, true);
 	}

	
	private boolean executeByTran(String[] sqlStr, String[] errInfo, boolean checkRowCount) {
        if(null == sqlStr || sqlStr.length == 0) return false;
        boolean flag = true;
        LOGGER.info("doOpen...");
		this.doOpen();
		LOGGER.info("begin tran.... ");
        Statement stmt = null; 
        String[] sql = new String[sqlStr.length];
        int index = 0;
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate("begin tran");
            for(int i=0; i<sqlStr.length; i++)
            {
            	if ((sqlStr[i]==null) || (sqlStr[i].isEmpty())) continue;
                stmt.addBatch(sqlStr[i]);
                sql[index++] = sqlStr[i]; 
            }
            int[] res = stmt.executeBatch();
            if (checkRowCount) {
	            for(int i=0; i<res.length; i++)
	            {
	                if ((res[i]<=0) && (res[i]!=-2))
	                {
	                	errInfo[0] = "下面的语句没有找到对应记录: " + sql[i];  
	                	LOGGER.error(errInfo[0]);
	                    stmt.executeUpdate("ROLLBACK;");  
	                    return false;
	                }
	            }
            }
            if (flag)       
            	stmt.executeUpdate("COMMIT;"); 
        }
        catch(Exception ex)
        {
            flag = false;
            try
            {               
                stmt.executeUpdate("ROLLBACK;");                 
            } 
            catch(Exception ex1) 
            {
            	LOGGER.error(ex1.toString());
            }
            LOGGER.error("excuteSqlByTransaction Failed : {}",ex);
            errInfo[0] = ex.toString();
        }
        LOGGER.info("....commit");
        return flag;
	}
	
	private boolean executeByTran2000(String[] sqlStr, String[] errInfo, boolean checkRowCount) {
        if(null == sqlStr || sqlStr.length == 0) return false;
        boolean flag = true;
        LOGGER.info("doOpen...");
		this.doOpen();
		LOGGER.info("opened... ");
        Statement stmt = null; 
        String[] sql = new String[sqlStr.length];
        int index = 0;
        try
        {
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            for(int i=0; i<sqlStr.length; i++)
            {
            	if ((sqlStr[i]==null) || (sqlStr[i].isEmpty())) continue;
                stmt.addBatch(sqlStr[i]);
                sql[index++] = sqlStr[i]; 
            }
            int[] res = stmt.executeBatch();
            LOGGER.info("executeBatch ok ");
            if (checkRowCount) {
	            for(int i=0; i<res.length; i++)
	            {
	                if ((res[i]<=0) && (res[i]!=-2))
	                {
	                	errInfo[0] = "下面的语句没有找到对应记录: " + sql[i];  
	                	LOGGER.error(errInfo[0]);
	                    return false;
	                }
	            }
            }
            conn.commit();
            LOGGER.info("commited...");
        }
        catch(Exception ex)
        {
            flag = false;
            LOGGER.error("excuteSqlByTransaction Failed : {}",ex);
            errInfo[0] = ex.toString();
        }
        LOGGER.info("executeByTran2000  end");
        return flag;
	}	
	
	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo, boolean checkRowCount) {
		if (conConfig.isSQLServer2000())
			return executeByTran2000(sqlStr, errInfo, checkRowCount);
		else
			return executeByTran(sqlStr, errInfo, checkRowCount);
	}

}