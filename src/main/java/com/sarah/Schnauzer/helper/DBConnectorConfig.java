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
package com.sarah.Schnauzer.helper;

import com.sarah.tools.localinfo.LocalInfo;
import com.sarah.tools.type.*;

/**
 * 
 * @author SarahCla
 */
public class DBConnectorConfig {
	public String host = "";
	public int port = 0;
	public String user = "";
	public String pwd = "";
	public String dbname = "";
	private String type = "";
	public String driver = "";
	public String DateFormat = "";
	public int waittime = 28800;
	
	public long pos = 0;
	public String binlog = "";
	public int serverid = 0;
	public String[] fieldTag = new String[2];

	public String errorMsg = "";
	
	public int masterID = 1;
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
		this.driver = "";
		this.fieldTag[0] = "";
		this.fieldTag[1] = "";
		
		if (StrHelp.equal(type, "MySql")) {
			this.driver = "";
			this.fieldTag[0] = "`";
			this.fieldTag[1] = "`";
		}
		else if (type.equalsIgnoreCase("SqlServer")) {
			this.driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			this.fieldTag[0] = "[";
			this.fieldTag[1] = "]";
		}
		else if (type.equalsIgnoreCase("SqlServer2000")) {
			this.driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			this.fieldTag[0] = "[";
			this.fieldTag[1] = "]";
		}
		else if (StrHelp.equal(type, "Redis")) {
		}
	}
	
	public boolean isMySQL() {
		return this.type.equalsIgnoreCase("mysql");
	}

	public boolean isRedis() {
		return this.type.equalsIgnoreCase("Redis");
	}
	
	public boolean isSQLServer() {
		return this.type.equalsIgnoreCase("sqlserver") || this.type.equalsIgnoreCase("sqlserver2000");
	}
	
	public boolean isSQLServer2000() {
		return this.type.equalsIgnoreCase("sqlserver2000");
	}
	
	public LocalInfo info;
	
	public DBConnectorConfig() {
		
	}
	
	public DBConnectorConfig(LocalInfo info) {
		this.info = info;
	}
	
	
	public DBConnectorConfig(String host, int port, String user, String pwd, String dbname, String type) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
		this.dbname = dbname;
		this.type = type;
		if (type.equalsIgnoreCase("MySql")) {
			this.driver = "";
			this.fieldTag[0] = "`";
			this.fieldTag[1] = "`";
		}
		else if (type.equalsIgnoreCase("SqlServer")) {
			this.driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			this.fieldTag[0] = "[";
			this.fieldTag[1] = "]";
		}
		else if (type.equalsIgnoreCase("SqlServer2000")) {
			this.driver = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			this.fieldTag[0] = "[";
			this.fieldTag[1] = "]";
		}
			
	}
	
	public String getURL() {
		if (this.type.equalsIgnoreCase("SqlServer"))
		{
			return "jdbc:sqlserver://" + this.host + ":" + this.port + ";DatabaseName=" + this.dbname;
		}
		else if (this.type.equalsIgnoreCase("MySql")) {
			return "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.dbname + "?useUnicode=true&amp;autoReconnect=true";
		}
		else if (this.type.equalsIgnoreCase("SqlServer2000"))
		{
			return "jdbc:microsoft:sqlserver://" + this.host + ":" + this.port + ";DatabaseName=" + this.dbname;
		}
		return "";
	}
	
}