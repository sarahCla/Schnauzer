//MasterDBHelper.java

package com.sarah.Schnauzer.helper.DB;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.Attrs.SASDBAttr;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.*;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLDbHelper;

/**
 * 
 * @author SarahCla
 */
public class MasterDBHelper extends MySQLDbHelper {

	public MasterDBHelper(DBConnectorConfig dbConfig) {
		super(dbConfig);
	}

	public ResultSet getTableFields(String tablename) {
		String sql = "select * from information_schema.columns where table_name='" 
					+ tablename + "' and table_schema='" + this.conConfig.dbname 
					+ "' order by ordinal_position";
		return getRS(sql);
	}
	
	public ResultSet getProfilors(String serverID) {
		String sql = " select " + SASDBAttr.ProfileID + 
				     " from " + SASDBAttr.ServerMaster + "." + SASDBAttr.BindTable + 
				     " where " + SASDBAttr.ServerID + "=" + serverID + 
				     "    and " + SASDBAttr.CanSync + "=1";
		return getRS(sql);
	}
}