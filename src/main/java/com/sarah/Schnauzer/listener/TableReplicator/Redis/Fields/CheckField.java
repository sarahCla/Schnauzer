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
package com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.code.or.common.util.MySQLConstants;

/**
 * 
 * @author SarahCla
 */
public class CheckField extends BaseField {
	private static final Logger LOGGER = LoggerFactory.getLogger(CheckField.class);
	
	private String[]   valuelist;
	public  int       unsigntag = -1;
	private Operator  checker;
	public  String    dateFormatStr = "YYYY-MM-DD";
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("MasterField", masterfield)
		.append("FieldIndex", fieldindex)
		.append("FieldType", fieldtype)
		.append("valuelist", valuelist)
		.append("unsigntag", unsigntag)
		.append("checher", checker)
		.append("dateFormStr", dateFormatStr).toString();
	}	

	private Operator str2Op(String s) {
		Operator o = null;
		try {
			o = Operator.valueOf(s);
		} catch(Exception e) {
			return null;
		}
		return o;
	}
	
	public void setValue(String s) {
		valuelist = s.split(",");
		for (int i=0; i<valuelist.length; i++) {
			valuelist[i] = valuelist[i].trim();
		}
	}
	
	public void setOperator(String s) {
		checker = str2Op(s);
		if (checker==null) {
			LOGGER.error("无效的操作符: " + s);
			System.exit(-1);
		}
	}
	
	
	private Boolean opPass(String v) {
		switch (fieldtype)
		{
			case MySQLConstants.TYPE_INT24:
				return Comparator.cmpAsInt(v, valuelist[0], checker);
				
			case MySQLConstants.TYPE_DECIMAL:
			case MySQLConstants.TYPE_NEWDECIMAL:
				return Comparator.cmpAsBigDecimal(v, valuelist[0], checker);
				
			case MySQLConstants.TYPE_FLOAT:
				return Comparator.cmpAsFloat(v, valuelist[0], checker);
				
			case MySQLConstants.TYPE_DOUBLE:
				return Comparator.cmpAsDouble(v, valuelist[0], checker);
			 
			case MySQLConstants.TYPE_TIMESTAMP:
			case MySQLConstants.TYPE_YEAR:		
			case MySQLConstants.TYPE_NEWDATE:	
				//ToDo
				return false;
				
			case MySQLConstants.TYPE_LONGLONG:
				return Comparator.cmpAsLong(v, valuelist[0], checker);
				
			case MySQLConstants.TYPE_DATE:
				return Comparator.cmpAsDate(v, valuelist[0], checker);
				
			case MySQLConstants.TYPE_TIME:	
			case MySQLConstants.TYPE_DATETIME:
				return Comparator.cmpAsDateTime(v, valuelist[0], this.dateFormatStr, checker);
		    default:
		    	LOGGER.error("ToDo: 还没有支持的类型 fieldtype=" + fieldtype);
		    	System.exit(-1);
		}
		return false;
	}
	
	
	//List<Column> columns
	//columns.get(this.checkFieldIndex).toString()
	
	public Boolean pass(String value) {
		String v = value.trim();
		switch(checker) {
		case IN :				return Comparator.existIn(v, valuelist);			
		case NotIn :			return Comparator.notExitIn(v, valuelist);
		
		case LessThan :
		case MoreThan :
		case LessOrEqual :
		case MoreOrEqual :
		case Equal :			return opPass(v);
		
		case IsEmpty :
		case IsNull:			return (v.isEmpty());
		
		default:  			    return false;
		}
	}
}
