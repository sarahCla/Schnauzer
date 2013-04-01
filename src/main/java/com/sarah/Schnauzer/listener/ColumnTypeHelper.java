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
package com.sarah.Schnauzer.listener;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.UnsignedLong;
import com.google.code.or.common.glossary.column.BitColumn;
import com.google.code.or.common.glossary.column.StringColumn;
import com.google.code.or.common.util.MySQLConstants;
import com.sarah.Schnauzer.helper.WarmingMailHelper;
import com.sarah.Schnauzer.listener.TableReplicator.Impl.RepField;
import com.sarah.tools.type.TypeTransfer;

/**
 * 
 * @author SarahCla
 */
public class ColumnTypeHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTableListener.class);

	public String databaseName = "";	
	public String tableName = "";
	public byte[] colTypes;
	public long position = 0;
	public String dateFormatStr = "";
	public long tableMapPos = 0;
	
	public StringColumn binlogFileName;
	
	
	public String getValueStr(List<Column> columns, BitColumn bCol, byte[] unsigntags, List<RepField> repFields) throws UnsupportedEncodingException 
	{
		String sql = "";
		for (int i=0; i<columns.size(); i++)
		{
			if (!bCol.get(i)) continue;
			if (!sql.isEmpty()) sql += ", ";
			sql += getColStr(i, columns.get(i), unsigntags[i], repFields.get(i));
		}
		return sql;
	}
	

	public String getUpdataStr(List<Column> columns, String[] names, byte[] unsigntags, List<RepField> repFields) throws UnsupportedEncodingException
	{
		String sql = "";
		for (int i=0; i<columns.size(); i++)
		{
			if (names[i].isEmpty()) continue;
			if (!sql.isEmpty()) sql += ", ";
			sql += names[i] + "=" + getColStr(i, columns.get(i), unsigntags[i], repFields.get(i));
		}
		return sql;
	}
	

	public String getColStr(int index, Column col, byte unsigntag, RepField field) throws UnsupportedEncodingException  
	{
		if (col==null)
		{
			LOGGER.error("col is null====================");
			return "NULL";
		} 
		
		if (col.getValue()==null) return "NULL";
		
		String value = col.getValue().toString();
		int result = this.colTypes[index] & 0xff; 
		switch (result)
		{
			case MySQLConstants.TYPE_MEDIUM_BLOB:
			case MySQLConstants.TYPE_BLOB:
			case MySQLConstants.TYPE_LONG_BLOB:
			case MySQLConstants.TYPE_VARCHAR:
			case MySQLConstants.TYPE_VAR_STRING:
			case MySQLConstants.TYPE_STRING:
				String res = "";
				try {
					byte[] bs = (byte[])col.getValue(); 
					if (field.characterset.equalsIgnoreCase("utf8"))
						res = new String(bs, "utf-8");
					else
						res = new String(bs, "gbk");
				} catch (UnsupportedEncodingException e1) {
					LOGGER.error("编码转换错误");
					throw e1;
				}
				value = "'" + res + "'";
				break;
			case MySQLConstants.TYPE_TIMESTAMP:
			case MySQLConstants.TYPE_YEAR:		
			case MySQLConstants.TYPE_NEWDATE:	
				value = "'" + col.toString() + "'";
				break;
			case MySQLConstants.TYPE_LONGLONG:
				if (unsigntag==1) {
					//value = "0x" + Long.toHexString((Long)col.getValue());
					value = UnsignedLong.valueOf((Long)col.getValue()).toString();
				}
				else
					value = col.getValue().toString();
				if (field.isSlaveText)
					value = "'" + value + "'";
				break;
			case MySQLConstants.TYPE_DATE:
				value = "'" + col.toString() + "'";
				break;
			case MySQLConstants.TYPE_TIME:	
			case MySQLConstants.TYPE_DATETIME:
				if (dateFormatStr.isEmpty()) {
					value = "'" + col.toString() + "'";
				} else {
			      try {
			    	  Date d2 = new SimpleDateFormat(this.dateFormatStr, Locale.US).parse(col.toString());		
			    	  value = "'" + TypeTransfer.dateTimeToString(d2) + "'";
			      } catch (ParseException e) {
			    	 WarmingMailHelper mailsender = new WarmingMailHelper("Config.xml");
			    	 mailsender.send("【复制故障】", "日期格式： " + col.toString() + "解析失败，请在config.xml中添加格式配置");
			    	 System.exit(-1);
			      }
				}
		  	    break;
		    default:
		    	if (field.isSlaveText)
		    		value = "'" + value + "'";
		}
		
		return value;
	}
}	