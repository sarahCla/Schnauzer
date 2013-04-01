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
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.helper.ConfigGetHelper;
import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.helper.DB.ISlaveDbHelper;
import com.sarah.Schnauzer.helper.DB.MasterDBHelper;
import com.sarah.Schnauzer.helper.DB.SlaveHelperFactory;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RDBSchnauzer;
import com.sarah.Schnauzer.listener.master.AbstractRDBMaster;
import com.sarah.Schnauzer.listener.master.IMaster;


/**
 * 
 * @author SarahCla
 */
public class SchnauzerRDBMaster extends AbstractRDBMaster implements IMaster {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SchnauzerRDBMaster.class);	
	
	public SchnauzerRDBMaster(DBConnectorConfig master, DBConnectorConfig slave) {
		super(master, slave);
	}
	
	private boolean setUnsignedLong(RDBSchnauzer table) {
		ISlaveDbHelper sdbhelper = new SlaveHelperFactory(this.slaveDb);
		try
		{
			String name = table.getSlaveTableName();
			ResultSet rs = sdbhelper.getTableFields(name);
			while(rs.next()) {
				if (this.slaveDb.isMySQL() && rs.getString("data_type").equalsIgnoreCase("bigint") && rs.getString("numeric_precision").equals("20")) 
					table.setUnsignedLong(rs.getString("column_name"));
				if (rs.getString("isText").equalsIgnoreCase("1")) 
					table.setSlaveTextField(rs.getString("column_name"));
			}
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("获取Slave表结构时出错" + e.getMessage());
		}
		return false;
	}
	
	private void setCharacterSet(RepField field, ResultSet rs) throws SQLException {
		String characterset = rs.getString("character_set_name");
		if (characterset!=null && !characterset.equals("")) field.characterset = characterset;
	}
	
	private void setFieldUnsignedLong(RepField field, ResultSet rs) throws SQLException {
		if (rs.getString("data_type").equalsIgnoreCase("bigint") && rs.getString("numeric_precision").equals("20")) 
			field.isUnsignedLong = true;
	}
	
	@Override
	public boolean registgerTableReplicator(DBConnectorConfig master) {
		try
		{
			this.tables.clear();
			ConfigGetHelper conf = new ConfigGetHelper();
			conf.getRDBRepTables(this.tables);
			MasterDBHelper mdbhelper = new MasterDBHelper(master);
			boolean pass = true;
			boolean haveCol = false;
			for (int i=0; i<this.tables.size(); i++)
			{
				RDBSchnauzer table = (RDBSchnauzer)this.tables.get(i);
				//fields get from config.xml
				List<RepField> confFields = table.getConfFields();
				//get fields from master table
				ResultSet rs = mdbhelper.getTableFields(table.getMasterTableName());
				haveCol = false;
				boolean isDiff = table.isHeterogenous();
				while(rs.next())
				{
					haveCol = true;
					RepField field = new RepField();
					String mFieldName = rs.getString("column_name");
					if (isDiff) 
					{
						RepField f = table.getConfField(mFieldName);
						field.copy(f);	
					} else {
						field.masterfield = mFieldName;
						field.slavefield = field.masterfield;
					}
					setFieldUnsignedLong(field, rs);
					setCharacterSet(field, rs);
					table.addFullField(field);
					if (table.checkfield.equalsIgnoreCase(mFieldName)) 
						table.checkFieldIndex = table.getFieldCount()-1;
				}
				if (!haveCol) {
					LOGGER.error("表" + table.getMasterTableName() + "在master数据库" + masterDb.dbname + "中不存在");
					System.exit(-1);
				}
				String defStr = "";
				for (int j=0; j<confFields.size(); j++)
				{
					RepField f = confFields.get(j); 
					if (!f.isNew) continue;
					RepField newfield = new RepField();
					newfield.copy(f);
					table.addFullField(newfield);
					if (newfield.isCuidDefValue) {
						table.cuidDefault = true;
						defStr += ",";
					}
					else
						defStr += "," + f.defvalue;
				}
				table.defValueStr = defStr;
				setUnsignedLong(table);
				table.setFieldNameTag(this.slaveDb.fieldTag[0], this.slaveDb.fieldTag[1]);
				LOGGER.info("{}", table.getFullFields());
				pass = pass && table.setIndex(); 
			}
			if (!pass) 
			{
				LOGGER.error("获取待复制表配置失败");
				System.exit(-1);
			}
		} catch (Exception e) {
			LOGGER.error("获取待复制表配置失败" + e.toString());
			return false;
		}
		return true;
	}

}