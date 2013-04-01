package com.sarah.Schnauzer.helper.DB.MySQL;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.AbstractDbHelper;

/**
 * 
 * @author SarahCla
 */
public class MySQLDbHelper extends AbstractDbHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MySQLDbHelper.class);
	

	
	public MySQLDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}

	public MySQLDbHelper(String host, int port, String user, String pwd, String dbname, String instanceName) {
		super(host, port, user, pwd, dbname, "", "");
	}


	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo) {
		return excuteSqlByTransaction(sqlStr, errInfo, true);
 	}

	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo, boolean checkRowCount) {
		
        if(null == sqlStr || sqlStr.length == 0)  
        	return false;
        boolean flag = true;
		this.doOpen();
        Statement stmt = null; 
        
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate("SET AUTOCOMMIT=0;");
            //stmt.executeUpdate("SET session character_set_client = utf8;");            
            //stmt.executeUpdate("SET session character_set_connection = gbk;");
            stmt.executeUpdate("START TRANSACTION;");
            String[] sql = new String[sqlStr.length];
            int index = 0;
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
        finally
        {     
            try
            {
                stmt.executeUpdate("SET AUTOCOMMIT=1;");  
            }
            catch(Exception ex){}
        }
        return flag;
	}

}