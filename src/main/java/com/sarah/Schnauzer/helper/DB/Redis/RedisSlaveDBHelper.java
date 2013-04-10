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
package com.sarah.Schnauzer.helper.DB.Redis;

import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.Tags;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.SlaveStatus;

/**
 * 
 * @author SarahCla
 */
public class RedisSlaveDBHelper implements ISlaveDbHelper{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSlaveDBHelper.class);
	
	protected Jedis conn;
	protected DBConnectorConfig conConfig;
	
	private int sysdb = 0;
	private int datadb = 1;
	
	public void flushDataDB() {
		try
		{
			conn = null;
			conn = new Jedis(conConfig.host, conConfig.port);
			conn.select(datadb);
			conn.flushDB();
		} catch(Exception e) {
			ErrorHelper.errExit(Infos.SetPos + Infos.Failed + ":" + e.getMessage());
		}
	}
	
	public RedisSlaveDBHelper(DBConnectorConfig dbConfig) {
		this.conConfig = dbConfig;
	}
	
	public String getBinlogKeyStr() {
		return Tags.softname + ":" + Tags.binlog + ":" + conConfig.masterID;
	}
	
	public String getPosKeyStr() {
		return Tags.softname + ":" + Tags.pos + ":" + conConfig.masterID;
	}
	
	public String getPreTableKeyStr() {
		return Tags.softname + ":PreTableName:" + conConfig.masterID;
	}
	
	public void setBinlogKey(String binlog, String pos, String table) {
		try
		{
			conn = null;
			conn = new Jedis(conConfig.host, conConfig.port);
			conn.select(sysdb);
			String key = getBinlogKeyStr();
			conn.set(key, binlog);
			LOGGER.info("redis.set " + key + " " + binlog);
			key = getPosKeyStr();
			conn.set(key, pos);
			LOGGER.info("redis.set " + key + " " + pos);
			key = getPreTableKeyStr();
			conn.set(key, table);
			LOGGER.info("redis.set " + key + " " + table);
		} catch(Exception e) {
			ErrorHelper.errExit(Infos.SetPos + Infos.Failed + ":" + e.getMessage());
		}
	}
	
	public void zincrby(String key, Double score, String member) {
		try
		{
			conn.select(datadb);
			conn.zincrby (key, score, member);
			//LOGGER.info("zincrby " + key + " " + member + " " + score);
		} catch(Exception e) {
			ErrorHelper.errExit("zincrby(" + key + "," + score + "," + member + ")" + Infos.Failed );
		}
	}
	
	public void sadd(String key, String value) {
		try
		{
			conn.select(datadb);
			conn.sadd(key, value);
			LOGGER.info("sadd " + key + " " + value);
		} catch(Exception e) {
			ErrorHelper.errExit("sadd(" + key + "," + value + ")" + Infos.Failed );
		}
	}
	

	@Override
	public boolean doOpen() {
		boolean isOpened = false;
		try {
			String binlog = conn.get(getBinlogKeyStr());
			isOpened = true;
		} catch(Exception e) {
			isOpened = false;
		}
		if (isOpened) return true;
		try {
			conn = null;
			conn = new Jedis(conConfig.host, conConfig.port);
		}  catch(Exception e) {   
			LOGGER.error("Redis" + Infos.Connect + Infos.Failed + "[" + conConfig.host + ":" + conConfig.port + "]" + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public ResultSet getRS(String sql) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo,
			boolean checkRowCount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean excuteSqlByTransaction(String[] sqlStr, String[] errInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean executeSql(String sql, String retInfo) {
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public SlaveStatus getSlaveStatus() {
		SlaveStatus result = null;
		try
		{
			conn = null;
			conn = new Jedis(conConfig.host, conConfig.port);
			conn.select(sysdb);
			String key = getBinlogKeyStr();
			String binlog = conn.get(key);
			String posKey = getPosKeyStr();
			int pos = Integer.parseInt(conn.get(posKey));
			result = new SlaveStatus(binlog, pos, conConfig.masterID);
		} catch(Exception e) {
			return null;
		}
		return result;
	}

	@Override
	public ResultSet getTableFields(String TableName) {
		// TODO Auto-generated method stub
		return null;
	}

}
