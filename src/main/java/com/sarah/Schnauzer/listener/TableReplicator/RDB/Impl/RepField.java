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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * 
 * @author SarahCla
 */
public class RepField {
	public String masterfield = "";
	public String slavefield = "";
	public String defvalue = "";
	public boolean isCuidDefValue = false;
	public boolean isNew = false;
	public boolean isUnsignedLong = false;
	public int sFieldLength = -1;
	public boolean isSlaveText = false;
	public String characterset = "gbk";
	
	public RepField() {
		
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("MasterField", masterfield)
		.append("SlaveField", slavefield)
		.append("DefaultValue", defvalue)
		.append("IsNew", isNew)
		.append("isCuidDefValue", this.isCuidDefValue)
		.append("isUnsignedLong", this.isUnsignedLong)
		.append("SlaveFieldLength", this.sFieldLength)
		.append("isSlaveText", this.isSlaveText).toString();
	}
	
	
	public RepField(String masterField, String slaveField, String defValue) {
		this.masterfield = masterField;
		this.slavefield = slaveField;
		this.defvalue = defValue;
	}
	
	public void copy(RepField field) {
		if (field==null) return;
		this.masterfield = field.masterfield;
		this.slavefield = field.slavefield;
		this.defvalue = field.defvalue;
		this.isNew = field.isNew;
		this.isCuidDefValue = field.isCuidDefValue;
		this.isUnsignedLong = field.isUnsignedLong;
		this.sFieldLength = field.sFieldLength;
		this.isSlaveText = field.isSlaveText;
		this.characterset = field.characterset;
	}
	
}