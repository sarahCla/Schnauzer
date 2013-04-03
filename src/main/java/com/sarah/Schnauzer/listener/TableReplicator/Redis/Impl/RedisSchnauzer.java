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
package com.sarah.Schnauzer.listener.TableReplicator.Redis.Impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.common.glossary.Column;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.listener.ColumnTypeHelper;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.*;
import com.sarah.tools.type.StrHelp;

/**
 * 
 * @author SarahCla
 */
public class RedisSchnauzer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSchnauzer.class);	
	
	public RedisRepTable table = new RedisRepTable();
	public List<RepField> masterfields = new CopyOnWriteArrayList<RepField>();

	private List<CheckField> ckfields = new CopyOnWriteArrayList<CheckField>();
	public ValueField vlfield;
	public MemberField memfield;
	public ScoreField scorefield;

	public RepField getMasterField(int index) {
		if (index<0) return null;
		return masterfields.get(index);
	}
	
	public String getKey(List<Column> columns) {
		String v = "";
		List<BaseField> fields = table.getKeyFields();
		for(int i=0; i<fields.size(); i++) {
			 v = v + ":" + columns.get(fields.get(i).fieldindex).getValue().toString();
		}
		return table.SlaveKey + v;
	}
	
	public RedisStructure getType() {
		return table.type;
	}
	public boolean needReplicate(List<Column> columns, ColumnTypeHelper helper) {
		if (ckfields.size()<=0)	return true;
		try
		{
			for(int i=0; i<ckfields.size(); i++) {
				CheckField field = ckfields.get(i);
				int index = field.fieldindex;
				String value = helper.getColStr(index, columns.get(index), (byte)1, this. masterfields.get(index));
				LOGGER.info(Infos.CheckValue + "=" + value);
				if (!field.pass(value)) return false;
			}
		} catch(Exception e) {
			
		}
		return true;
	}
	
	public void addMasterField(RepField field) {
		masterfields.add(field);
		int index = masterfields.size()-1;
		
		for (int i=0; i<ckfields.size(); i++) {
			if (StrHelp.TEqual(ckfields.get(i).masterfield, field.masterfield)) {
				ckfields.get(i).fieldindex = index;
				break;
			}
		}
		
		List<BaseField> keyfields = table.getKeyFields();
		for (int i=0; i<keyfields.size(); i++) {
			if (StrHelp.TEqual(keyfields.get(i).masterfield, field.masterfield)) {
				keyfields.get(i).fieldindex = index;
				break;
			}
		}
		
		if (StrHelp.TEqual(vlfield.masterfield, field.masterfield)) 
			vlfield.fieldindex = index;
		
		if (StrHelp.TEqual(memfield.masterfield, field.masterfield)) 
			memfield.fieldindex = index;
		
		if (StrHelp.TEqual(scorefield.masterfield, field.masterfield)) 
			scorefield.fieldindex = index;
	}
	
	public void setMasterTable(String v) {
		this.table.MasterTable = v.trim(); 
	}
	
	public void setSlaveKey(String v) {
		this.table.SlaveKey = v.trim();
	}
	
	public void setDataType(String v) {
		if (StrHelp.TEqual(v, RedisStructure.List.toString())) {
			this.table.type = RedisStructure.List;
		} else if (StrHelp.TEqual(v, RedisStructure.Set.toString())) {
			this.table.type = RedisStructure.Set;
		} else if (StrHelp.TEqual(v, RedisStructure.SortedSet.toString())) {
			this.table.type = RedisStructure.SortedSet;
		} else if (StrHelp.TEqual(v, RedisStructure.String.toString())) {
			this.table.type = RedisStructure.String;
		} else {
			ErrorHelper.errExit(Infos.Illegal + Infos.RedisDataType + ":" + v);
		}
	}
	
	public String getMasterTableName() {
		return this.table.MasterTable;
	}
	
	public void setKeyFields(String v) {
		this.table.setKeyFiels(v);
	}
	
	public List<CheckField> getCheckFields() {
		return ckfields;
	}
	
	public int getValueIndex() {
		if (vlfield==null) return -1;
		return vlfield.fieldindex;
	}
	
	public int getMemberIndex() {
		if (memfield==null) return -1;
		return memfield.fieldindex;
	}
	
	public int getScoreIndex() {
		if (scorefield==null) return -1;
		return scorefield.fieldindex;
	}
	
}
