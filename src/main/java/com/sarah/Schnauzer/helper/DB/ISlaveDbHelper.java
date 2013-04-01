//ISlaveDbHelper.java
package com.sarah.Schnauzer.helper.DB;

import java.sql.*;


/**
 * 
 * @author SarahCla
 */
public interface ISlaveDbHelper extends IDbHelper {
	public SlaveStatus getSlaveStatus() throws Exception;
	public ResultSet getTableFields(String TableName);

}