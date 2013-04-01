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
package com.sarah.Schnauzer.listener.TableReplicator.RDB;

import java.util.List;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.column.BitColumn;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;


/**
 * 
 * @author SarahCla
 */
public interface ITableReplicator {
	public String   getSlaveTableName();
	public String   getMasterTableName();
	public String[] getColumnNames();
	public int getColumnsCount();
	public String getIDName(int index);
	public int getIDColIndex(int index);
	
	public boolean haveCUIDDefault(); 
	
	public boolean isHeterogenous();
	public boolean needReplicate(List<Column> columns);
	public boolean isMergeTable();
	
	public String getInsertFields(BitColumn bCol);
	public String getDelete();
	public String getDefStr();
	
	public void setUsedColumn(BitColumn bCol);
	public byte[] getUnsignedTags();
	public void setFieldNameTag(String begin, String end);
	
	public void setKeyFields(String fields);
	public int[] getKeyFieldIndexs();
	
	public List<RepField> getFullFields();
	
}