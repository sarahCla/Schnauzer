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
package com.sarah.Schnauzer.listener.master.Impl;

import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.binlog.impl.event.DeleteRowsEvent;
import com.google.code.or.binlog.impl.event.UpdateRowsEvent;
import com.google.code.or.binlog.impl.event.WriteRowsEvent;
import com.sarah.Schnauzer.helper.ConfigGetHelper;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.WarmingMailHelper;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.MasterDBHelper;
import com.sarah.Schnauzer.helper.DB.Redis.RedisSlaveDBHelper;
import com.sarah.Schnauzer.listener.ColumnTypeHelper;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.BaseField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Impl.RedisSchnauzer;
import com.sarah.Schnauzer.listener.master.IMaster;

/**
 * 
 * @author SarahCla
 */
public class SchnauzerRedisMaster  implements IMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchnauzerRedisMaster.class);	
	
	protected DBConnectorConfig slaveDb;	
	protected DBConnectorConfig masterDb;
	
	private RedisSlaveDBHelper dbhelper;
	private WarmingMailHelper mailsender;	

	private List<RedisSchnauzer> tables = new CopyOnWriteArrayList<RedisSchnauzer>();
	
	@Override
	public boolean registgerTableReplicator(DBConnectorConfig master) {
		try
		{
			tables.clear();
			ConfigGetHelper conf = new ConfigGetHelper();
			conf.getRedisTables(tables);
			MasterDBHelper mdbhelper = new MasterDBHelper(master);
			for (int i=0; i<this.tables.size(); i++)
			{
				RedisSchnauzer table = (RedisSchnauzer)this.tables.get(i);
				ResultSet rs = mdbhelper.getTableFields(table.getMasterTableName());
				int index = 0;
				Boolean haveCol = false;
				while(rs.next())
				{
					haveCol = true;
					BaseField field = new BaseField(rs.getString("column_name"));
					table.addMasterField(field);
					
					
				}	
				if (!haveCol) {
					ErrorHelper.errExit(String.format(Infos.TableNotExist, table.getMasterTableName(), masterDb.dbname));
				}
			}
		} catch (Exception e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + e.toString());
			return false;
		}
		return true;
	}

	@Override
	public boolean doWrite(ColumnTypeHelper helper, WriteRowsEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doUpdate(ColumnTypeHelper helper, UpdateRowsEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doDelete(ColumnTypeHelper helper, DeleteRowsEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
