//SlaveHelperFactory.java
package com.sarah.Schnauzer.helper.DB;

import java.sql.*;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLSlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.SqlServer.SqlServerSlaveDbHelper;


/**
 * 
 * @author SarahCla
 */
public class SlaveHelperFactory implements ISlaveDbHelper {
	private DBConnectorConfig dbconfig;
	private ISlaveDbHelper helper = null;
	
	public SlaveHelperFactory(DBConnectorConfig dbconfig) {
		this.dbconfig = dbconfig;
		this.helper = createHelper();
	}
	
	private ISlaveDbHelper createHelper() {
		if (this.dbconfig.isMySQL()) {
			this.helper = new MySQLSlaveDbHelper(dbconfig);
		} else if (this.dbconfig.isSQLServer()) {
			this.helper = new SqlServerSlaveDbHelper(dbconfig);
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