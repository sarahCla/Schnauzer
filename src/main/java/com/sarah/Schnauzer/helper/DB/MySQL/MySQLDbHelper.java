package com.sarah.Schnauzer.helper.DB.MySQL;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.DB.AbstractDbHelper;

/**
 * 
 * @author SarahCla
 */
public class MySQLDbHelper extends AbstractDbHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MySQLDbHelper.class);
	
	
	public Boolean isClosed() throws SQLException {
		return conn.isClosed() || (!conn.isValid(1000));
	}
	
	public void reOpen() throws SQLException {
		conn.close();
		doOpen();
	}
	
	public MySQLDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}

	public MySQLDbHelper(String host, int port, String user, String pwd, String dbname) {
		super(host, port, user, pwd, dbname, "");
	}


	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo) {
		return excuteSqlByTransaction(sqlStr, errInfo, true);
 	}

	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo, boolean checkRowCount) {
		
        if(null == sqlStr || sqlStr.length == 0) return false;
        boolean flag = true;
		this.doOpen();
        try
        {
        	Statement stmt = conn.createStatement();
        	conn.setAutoCommit(false);
        	int nCount = 0;
            if (checkRowCount) {
	            int ret = 0;
	            for(int i=0; i<sqlStr.length; i++)
	            {
	            	if ((sqlStr[i]==null) || (sqlStr[i].isEmpty())) continue;
	                ret = stmt.executeUpdate(sqlStr[i]);
	                if ((ret<=0) && (ret!=-2)) {
	                	errInfo[0] = Infos.RecordNotFound + ":" + sqlStr[i];
	                	conn.rollback();
	                	return false;
	                }
	                 
	            }
            } else {            	
	            for(int i=0; i<sqlStr.length; i++)
	            {
	            	if ((sqlStr[i]==null) || (sqlStr[i].isEmpty())) continue;
	            	if (i<2) LOGGER.info(sqlStr[i]);
	                stmt.execute(sqlStr[i]);
	                nCount++;
	            }
            }
            conn.commit();
            LOGGER.info("执行语句：nCount=" + nCount);
        }
        catch(Exception ex)
        {
            LOGGER.error("excuteSqlByTransaction Failed : {}",ex);
            errInfo[0] = ex.toString();
            return false;
        }
        return flag;
	}

	/*
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
	                	errInfo[0] = Infos.RecordNotFound + ":" + sql[i];  
	                    //stmt.executeUpdate("ROLLBACK;");  
	                    return false;
	                }
	            }
            }
            if (flag) stmt.executeUpdate("COMMIT;"); 
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
	 */
}