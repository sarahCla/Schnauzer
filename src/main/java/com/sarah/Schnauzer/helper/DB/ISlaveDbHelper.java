//ISlaveDbHelper.java
package com.sarah.Schnauzer.helper.DB;

import java.sql.*;


/**
 * 
 * @author SarahCla
 */
public interface ISlaveDbHelper extends IDbHelper {
	public ResultSet getSlaveStatus();

	public ResultSet getTableFields(String TableName);

}