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

import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.*;
import com.sarah.tools.type.StrHelp;

/**
 * 
 * @author SarahCla
 */
public class RedisSchnauzer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisSchnauzer.class);	
	
	public RedisRepTable table = new RedisRepTable();
	
	private RedisStructure type = null;
	private List<BaseField> masterfields = new CopyOnWriteArrayList<BaseField>();
	
	private List<CheckField> ckfields = new CopyOnWriteArrayList<CheckField>();
	private List<ValueField> vlfields = new CopyOnWriteArrayList<ValueField>();
	private List<MemberField> memfields = new CopyOnWriteArrayList<MemberField>();
	private List<ScoreField> scorefields = new CopyOnWriteArrayList<ScoreField>();

	
	public void addMasterField(BaseField field) {
		masterfields.add(field);
		int index = masterfields.size()-1;
		
		for (int i=0; i<ckfields.size(); i++) {
			if (StrHelp.TEqual(ckfields.get(i).masterfield, field.masterfield)) {
				ckfields.get(i).fieldindex = index;
				break;
			}
		}
		
		for (int i=0; i<vlfields.size(); i++) {
			if (StrHelp.TEqual(vlfields.get(i).masterfield, field.masterfield)) {
				vlfields.get(i).fieldindex = index;
				break;
			}
		}
		
		for (int i=0; i<memfields.size(); i++) {
			if (StrHelp.TEqual(memfields.get(i).masterfield, field.masterfield)) {
				memfields.get(i).fieldindex = index;
				break;
			}
		}
		
		for (int i=0; i<scorefields.size(); i++) {
			if (StrHelp.TEqual(scorefields.get(i).masterfield, field.masterfield)) {
				scorefields.get(i).fieldindex = index;
				break;
			}
		}
		
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
	
	public List<ValueField> getValueFields() {
		return vlfields;
	}
	
	public List<MemberField> getMemberFields() {
		return memfields;
	}
	
	public List<ScoreField> getScoreFields() {
		return scorefields;
	}
	
	
}
