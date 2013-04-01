//SqlServerDbHelper.java

package com.sarah.Schnauzer.helper.DB.SqlServer;

import java.sql.*;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;

/**
 * 
 * @author SarahCla
 */
public class SqlServerSlaveDbHelper extends SqlServerDbHelper implements ISlaveDbHelper {

	public SqlServerSlaveDbHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}

	@Override
	public ResultSet getSlaveStatus() {
		return getRS("select * from  RepStatus Where masterID=" + this.conConfig.masterID);
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