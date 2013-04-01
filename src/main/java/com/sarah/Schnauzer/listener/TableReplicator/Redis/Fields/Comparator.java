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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.sarah.Schnauzer.helper.*;


/**
 * 
 * @author SarahCla
 */
public class Comparator {

	public static Boolean existIn(String s1, String[] ss) {
		for(int i=0; i<ss.length; i++) {
			if (s1.equalsIgnoreCase(ss[i])) return true;
		}
		return false;
	}
	
	public static Boolean notExitIn(String s1, String[] ss) {
		for(int i=0; i<ss.length; i++) {
			if (s1.equalsIgnoreCase(ss[i])) return false;
		}
		return true;
	}
	
	public static Boolean cmpAsInt(String s1, String s2, Operator checker) {
		int v1 = Integer.parseInt(s1);
		int v2 = Integer.parseInt(s2);
		switch(checker) {
		case LessThan:   		return (v1<v2);
		case LessOrEqual:  	    return (v1<=v2);
		case MoreThan:			return (v1>v2);
		case MoreOrEqual:		return (v1>=v2);
		case Equal:  			return (v1==v2);
		default:
			ErrorHelper.errExit("待处理的操作符：Operator.cmpAsInt:" + checker);
		}
		return false;
	}
	
	public static Boolean cmpAsBigDecimal(String s1, String s2, Operator checker) {
		BigDecimal v1 = new BigDecimal(s1);
		BigDecimal v2 = new BigDecimal(s2);
		switch(checker) {
		case LessThan:			return (v1.compareTo(v2)==-1);
		case LessOrEqual:		return ((v1.compareTo(v2)==-1)||(v1.compareTo(v2)==0));
		case MoreThan:			return (v1.compareTo(v2)==1);
		case MoreOrEqual:		return ((v1.compareTo(v2)==1)||(v1.compareTo(v2)==0));
		case Equal: 			return (v1.compareTo(v2)==0);
		default:
			ErrorHelper.errExit("待处理的操作符：Operator.cmpAsBigDecimal:" + checker);
		}
		return false;
	}
	
	public static Boolean cmpAsDouble(String s1, String s2, Operator checker) {
		Double v1 = Double.parseDouble(s1);
		Double v2 = Double.parseDouble(s2);
		switch(checker) {
		case LessThan:			return (v1<v2);
		case LessOrEqual:		return (v1<=v2);
		case MoreThan:			return (v1>v2);
		case MoreOrEqual:		return (v1>=v2);
		case Equal: 			return ((v1-v2)<0.000000001);
		default:
			ErrorHelper.errExit("待处理的操作符：Operator.cmpAsDouble:" + checker);
		}
		return false;
	}
	
	public static Boolean cmpAsFloat(String s1, String s2, Operator checker) {
		Float v1 = Float.parseFloat(s1);
		Float v2 = Float.parseFloat(s2);
		switch(checker) {
		case LessThan:			return (v1<v2);
		case LessOrEqual:		return (v1<=v2);
		case MoreThan:			return (v1>v2);
		case MoreOrEqual:		return (v1>=v2);
		case Equal: 			return ((v1-v2)<0.000000001);
		default:
			ErrorHelper.errExit("待处理的操作符：Operator.cmpAsFloat:" + checker);
		}
		return false;
	}

	
	public static Boolean cmpAsLong(String s1, String s2, Operator checker) {
		Long v1 = Long.parseLong(s1);
		Long v2 = Long.parseLong(s2);
		switch(checker) {
		case LessThan:			return (v1<v2);
		case LessOrEqual:		return (v1<=v2);
		case MoreThan:			return (v1>v2);
		case MoreOrEqual:		return (v1>=v2);
		case Equal: 			return ((v1-v2)<0.000000001);
		default:
			ErrorHelper.errExit("待处理的操作符：Operator.cmpAsLong:" + checker);
		}
		return false;
	}

	public static Boolean cmpAsDate(String s1, String s2, Operator checker) {
		Date d1 = new Date();
		Date d2 = new Date();
		try {
			SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD");
			d1 = format.parse(s1);
			d2 = format.parse(s2);
		}
		catch(ParseException pe) {
			ErrorHelper.errExit("日期类型转换错误:d1=[" + s1 + "] d2=[" + s2 + "]" + pe.getMessage());
		}
		switch(checker) {
		case LessThan:			return (d1.before(d2));
		case LessOrEqual:		return (d1.before(d2)||(d1.compareTo(d2)==0));
		case MoreThan:			return (d1.after(d2));
		case MoreOrEqual:		return (d1.after(d2)||(d1.compareTo(d2)==0));
		case Equal: 			return (d1.compareTo(d2)==0);
		default:
			ErrorHelper.errExit("待处理的操作符：Operator.cmpAsDate:" + checker);
		}
		return false;
	}

	public static Boolean cmpAsDateTime(String s1, String s2, String dateFormatStr, Operator checker) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD");
			Date d1 = new SimpleDateFormat(dateFormatStr, Locale.US).parse(s1);
			Date d2 = format.parse(s2);	    	  
			switch(checker) {
			case LessThan:			return (d1.before(d2));
			case LessOrEqual:		return (d1.before(d2)||(d1.compareTo(d2)==0));
			case MoreThan:			return (d1.after(d2));
			case MoreOrEqual:		return (d1.after(d2)||(d1.compareTo(d2)==0));
			case Equal: 			return (d1.compareTo(d2)==0);
			default:
				ErrorHelper.errExit("待处理的操作符：Operator.cmpAsDateTime:" + checker);
			}
		} catch (ParseException e) {
			ErrorHelper.errExit("日期类型转换错误:d1=[" + s1 + "] d2=[" + s2 + "] dateFormatStr=[" + dateFormatStr + "]" + e.getMessage());
		}		
		return false;
	}
	
}
