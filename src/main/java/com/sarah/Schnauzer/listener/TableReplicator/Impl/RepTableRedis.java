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
package com.sarah.Schnauzer.listener.TableReplicator.Impl;

import java.util.List;

import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.column.BitColumn;
import com.sarah.Schnauzer.listener.TableReplicator.ITableReplicator;

/**
 * 
 * @author SarahCla
 */
public class RepTableRedis implements ITableReplicator {

	@Override
	public String getSlaveTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMasterTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getIDName(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIDColIndex(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean haveCUIDDefault() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHeterogenous() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean needReplicate(List<Column> columns) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMergeTable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getInsertFields(BitColumn bCol) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDelete() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefStr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsedColumn(BitColumn bCol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getUnsignedTags() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFieldNameTag(String begin, String end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKeyFields(String fields) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] getKeyFieldIndexs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RepField> getFullFields() {
		// TODO Auto-generated method stub
		return null;
	}

}
