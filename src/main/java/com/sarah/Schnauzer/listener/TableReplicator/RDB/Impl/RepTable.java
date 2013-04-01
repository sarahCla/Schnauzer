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
import org.apache.commons.lang.StringUtils;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.column.BitColumn;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.AbstractTableReplicator;



/**
 * 
 * @author SarahCla
 */
public class RepTable extends AbstractTableReplicator {

	public int[] keyFieldIndexs = null;
	private String[] keyfields = null;

	@Override
	public String getMasterTableName() {
		return "ptype";
	}
	
	@Override
	public String getSlaveTableName() {
		return "ptype";
	}

	@Override
	public String[] getColumnNames() {
		String[] cols = new String[]{"typeId", "parTypeId", "leveal", "sonnum", "soncount", "CanModify", "UserCode", "p_barcode", "p_bartype", "barcode", "FullName", "Name", "Standard", "Type", "Area",
				"Unit1", "Comment", "deleted", "warnup", "warndown", "RowIndex", "id", "parid", "profileid", "supplyInfo", "preprice", "preprice2", "preprice3", "preprice4", "recPrice", "recPrice1",
				"recPriceBase", "isStop", "namePy", "minPrice", "prop1_enabled", "prop2_enabled", "prop3_enabled", "taobao_cid", "pic_url", "createtype", "technicalservicerate", 
				"feature1", "feature2", "feature3", "modifiedTime"};
		return cols;
	}

	@Override
	public String getIDName(int index) {
		return "id";
	}

	@Override
	public boolean needReplicate(List<Column> columns) {
		return (columns.get(9).toString().equalsIgnoreCase("NeedReplicateTo   A"));
	}



	@Override
	public int getIDColIndex(int index) {
		return 21;
	}

	@Override
	public boolean isHeterogenous() {
		return false;
	}

	@Override
	public void setUsedColumn(BitColumn bCol) {
	}

	@Override
	public String getDefStr() {
		return "";
	}

	@Override
	public boolean haveCUIDDefault() {
		return false;
	}

	@Override
	public boolean isMergeTable() {
		return false;
	}

	@Override
	public void setKeyFields(String fields) {
		this.keyfields = StringUtils.split(fields, ",");
	}

	@Override
	public int[] getKeyFieldIndexs() {
		return this.keyFieldIndexs;
	}

	@Override
	public List<RepField> getFullFields() {
		return null;
	}


	
}