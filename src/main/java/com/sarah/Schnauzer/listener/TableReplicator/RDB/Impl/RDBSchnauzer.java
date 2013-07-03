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
package com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.column.BitColumn;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.Tags;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;


/**
 * 
 * @author SarahCla
 */
public class RDBSchnauzer implements ITableReplicator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RDBSchnauzer.class);	
	
	private List<RepField> fullfields = new CopyOnWriteArrayList<RepField>();	
	private List<RepField> conffields = new CopyOnWriteArrayList<RepField>();
	
	public int checkFieldIndex = -1; 
	
	public String InsertFields = "";
	
	private String mastertable = "";
	private String slavetable = "";
	private String needrepvalue = "";
	private boolean heterogeneous = false;
	private boolean mergedTable = false;
	private String[] columnnames;
	public boolean cuidDefault = false;
	
	public String defValueStr = "";
	public String checkfield = "";

	public int[] keyFieldIndexs = null;
	private String[] keyfields = null;
	
	public byte[] isUnsignedLongs;
	private Boolean isCuidDefaultText = null;
	
	private String[] fnameTag = new String[2];
	
	
	
	public int getFieldCount() {
		return fullfields.size();
	}
	
	public List<RepField> getConfFields() {
		return this.conffields;
	}
	
	@Override
	public List<RepField> getFullFields() {
		return this.fullfields;
	}
	
	public void addFullField(RepField field) {
		this.fullfields.add(field);
	}
	
	
	@Override
	public byte[] getUnsignedTags() {
		if (this.isUnsignedLongs!=null)
			return this.isUnsignedLongs;
		this.isUnsignedLongs = new byte[fullfields.size()];
		for (int i=0; i<this.isUnsignedLongs.length; i++) {
			this.isUnsignedLongs[i] = 0;
			if (fullfields.get(i).isUnsignedLong)
				this.isUnsignedLongs[i] = 1;
		}
		return this.isUnsignedLongs;
	}
	
	public void setUnsignedLong(String field) {
		for (int i=0; i<this.fullfields.size(); i++) {
			if (this.fullfields.get(i).slavefield.equalsIgnoreCase(field)) {
				this.fullfields.get(i).isUnsignedLong = true;
			}
		}
	}

	public void setSlaveTextField(String field) {
		for (int i=0; i<this.fullfields.size(); i++) {
			if (this.fullfields.get(i).slavefield.equalsIgnoreCase(field)) {
				this.fullfields.get(i).isSlaveText = true;
			}
		}
	}

	public boolean setIndex() {
		if ((checkFieldIndex>=0) && (keyFieldIndexs!=null)) return true;
		String s = this.fullfields.size() + "";
		this.keyFieldIndexs = new int[this.keyfields.length];
		for (int n=0; n<this.keyFieldIndexs.length; n++) this.keyFieldIndexs[n] = -1;
		for (int i = 0; i < this.fullfields.size(); i++) 
		{
			RepField field = this.fullfields.get(i);
			s = s + "[" + field.masterfield + "]";
			if ((!this.checkfield.isEmpty()) && (field.masterfield.equalsIgnoreCase(this.checkfield))) 
				this.checkFieldIndex = i;
			else {
				for (int j=0; j<this.keyfields.length; j++) {
					if (field.masterfield.equalsIgnoreCase(this.keyfields[j]))
						this.keyFieldIndexs[j] = i;
				}
			}
		}
		for (int n=0; n<this.keyFieldIndexs.length; n++) {
			if (this.keyFieldIndexs[n]>=0) continue; 
			ErrorHelper.errExit(String.format(Infos.CanNotFoundKeyField + s, this.keyfields[n]));
		}
		return true;
	}
	
	public RepField getConfField(String masterfield) {
		for (int i=0; i < conffields.size(); i++)
		{
			if (!conffields.get(i).masterfield.equalsIgnoreCase(masterfield)) continue;
			return conffields.get(i);
		}
		return null;
	}
	
	public boolean notRep(int index) {
		return fullfields.get(index).masterfield.isEmpty();
	}
	
	
	@Override	
	public boolean isHeterogenous() {
		return this.heterogeneous;
	}
	
	public void setHeterogeneous(String isHeterogeneous) {
		this.heterogeneous = isHeterogeneous.equalsIgnoreCase("true");
	}

	public void setMergedTable(String isMergedTable) {
		this.mergedTable = isMergedTable.equalsIgnoreCase("true");
	}
	
	public void setNeedRepValue(String needRepValue) {
		this.needrepvalue = needRepValue;
	}
	

	public void setMasterTable(String master) {
		this.mastertable = master;
	}

	public void setSlaveTable(String slave) {
		this.slavetable = slave;
	}
	
	public void setCheckField(String checkField) {
		this.checkfield = checkField;
	}
	

	@Override
	public String[] getColumnNames() {
		if (columnnames!=null) return columnnames;
		int count = this.fullfields.size();
		columnnames = new String[count];
		for (int i=0; i<count; i++)
		{
			columnnames[i] = "";
			if (notRep(i)) continue;
			columnnames[i] = fnameTag[0] + this.fullfields.get(i).slavefield + fnameTag[1];
		}
		return columnnames;
	}

	@Override
	public String getIDName(int index) {
		if (index<0 || index>=this.keyfields.length) throw new IllegalArgumentException("invalid value: " + index);
		return fnameTag[0] + this.fullfields.get(this.keyFieldIndexs[index]).slavefield + fnameTag[1];
	}

	@Override
	public boolean needReplicate(List<Column> columns) {
		if (this.checkFieldIndex<0)	return true;
		if (Tags.Verbose) LOGGER.info(Infos.CheckValue + "=" + columns.get(this.checkFieldIndex).toString());
		return (columns.get(this.checkFieldIndex).toString().equalsIgnoreCase(this.needrepvalue));
	}

	@Override
	public int getIDColIndex(int index) {
		if (index<0 || index>=this.keyFieldIndexs.length) throw new IllegalArgumentException("invalid value: " + index);
		return this.keyFieldIndexs[index];
	}

	@Override
	public int getColumnsCount() {
		return 0;
	}

	@Override
	public String getInsertFields(BitColumn bCol) {
		if (!InsertFields.equalsIgnoreCase("")) return InsertFields;
		String sql = "";
		for(int i=0; i<bCol.getLength(); i++)
		{
			if (!bCol.get(i)) continue;
			if (notRep(i)) continue;
			if (!sql.equalsIgnoreCase("")) sql += ",";
			sql += fnameTag[0] + this.fullfields.get(i).slavefield + fnameTag[1];
		}
		for(int i=bCol.getLength(); i<this.fullfields.size(); i++)
		{
			sql += "," + fnameTag[0] + this.fullfields.get(i).slavefield + fnameTag[1];
		}
		InsertFields = "insert into " + this.slavetable + " (" + sql + " ) values ";
		return InsertFields;
	}

	@Override
	public String getDelete() {
		return "delete from " + this.getSlaveTableName() + " where ";
	}

	@Override
	public String getSlaveTableName() {
		return this.slavetable;
	}

	@Override
	public String getMasterTableName() {
		return this.mastertable;
	}

	@Override
	public void setUsedColumn(BitColumn bCol) {
		for(int i=0; i<bCol.getLength(); i++)
		{
			if (notRep(i)) bCol.setNotUse(i);
		}
	}

	@Override
	public String getDefStr() {
		return this.defValueStr;
	}

	@Override
	public boolean haveCUIDDefault() {
		return this.cuidDefault;
	}
	
	public boolean isCUIDDefaultText() {
		if (isCuidDefaultText==null) {
			this.isCuidDefaultText = new Boolean(false);
			for (int i=0; i<this.fullfields.size(); i++) {
				if (this.fullfields.get(i).isCuidDefValue && this.fullfields.get(i).isSlaveText) {
					this.isCuidDefaultText = true;
					return this.isCuidDefaultText.booleanValue();
				}
			}
		}
		return this.isCuidDefaultText.booleanValue();
	}

	@Override
	public boolean isMergeTable() {
		return this.mergedTable;
	}

	@Override
	public void setFieldNameTag(String begin, String end) {
		fnameTag[0] = begin;
		fnameTag[1] = end;
	}

	@Override
	public void setKeyFields(String fields) {
		this.keyfields = StringUtils.split(fields, ",");
	}

	@Override
	public int[] getKeyFieldIndexs() {
		return this.keyFieldIndexs;
	}
	
	

}