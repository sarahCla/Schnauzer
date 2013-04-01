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
package com.sarah.Schnauzer.helper;

import java.io.File;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.heartbeat.HeartBeatInfo;
import com.sarah.Schnauzer.listener.TableReplicator.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.Impl.RepTableNew;
import com.sarah.tools.type.StrHelp;

/**
* @author SarahCla
*/
public class ConfigGetHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigGetHelper.class);
	
	private String filename = "Config.xml";
	
	private boolean isEqual(Attribute att, String name) {
		return att.getName().trim().equalsIgnoreCase(name);
	}
	
	private boolean isNotEqual(Attribute att, String name) {
		return (!att.getName().trim().equalsIgnoreCase(name));
	}
	
	public boolean getMailList(List<String> recipients) {
		try {
			File inputXml = new File(this.filename);
			SAXReader saxReader = new SAXReader();
			recipients.clear();
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			for(@SuppressWarnings("unchecked")
			Iterator<Element> i=root.elementIterator(); i.hasNext();)
			{
				Element head = (Element)i.next();
				if (!head.getName().equalsIgnoreCase(Tags.MailLists)) continue;
				for(Iterator<Element> j = head.elementIterator();j.hasNext();)
				{
					Element elem = (Element) j.next();
					recipients.add(elem.getText());
				}
			}
		} catch (DocumentException e) {
			LOGGER.error("获取注册邮件错误：" + e.getMessage());
			return false;
		}
		LOGGER.info("注册了" + recipients.size() + "个报警邮件");
		return true;
	}	

	public boolean getHeartBeatSet(HeartBeatInfo info) {
		try {
			File inputXml = new File(this.filename);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			String value = "";
			for(Iterator i=root.elementIterator(); i.hasNext();)
			{
				Element head = (Element)i.next();
				if (!StrHelp.TEqual(head.getName(), Tags.HeartBeat)) continue;
				List<Attribute> attrList = head.attributes();
				for (int n=0; n<attrList.size(); n++) 
				{
				    Attribute att = (Attribute)attrList.get(n);
				    value = att.getValue().trim();
				    if (isEqual(att, "Host"))
				    	info.host = value;
				    else if (isEqual(att,"port"))
				    	info.port = Integer.parseInt(value);
				    else if (isEqual(att,"Interval"))
				    	info.Interval = Long.parseLong(value);
				    else  {
				    	LOGGER.error("HeartBeat中出现未知属性：" + att.getName());
				    	System.exit(-1);
				    }
				}				
				break;
			}
			LOGGER.info("获取心跳配置成功");
		} catch (DocumentException e) {
			LOGGER.error("获取心跳配置失败：" + e.getMessage());
			return false;
		}
		return true;
		
	}
	
	public boolean getDBConfig(DBConnectorConfig dbConfig, String name) {
		try {
			File inputXml = new File(this.filename);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			String value = "";
			for(Iterator i=root.elementIterator(); i.hasNext();)
			{
				Element head = (Element)i.next();
				if (!StrHelp.TEqual(head.getName(), name)) continue;
				List<Attribute> attrList = head.attributes();
				for (int n=0; n<attrList.size(); n++) 
				{
				    Attribute att = (Attribute)attrList.get(n);
					value = att.getValue().trim();
				    if (isEqual(att, "Host"))				    	dbConfig.host = value;
				    else if (isEqual(att, "port"))			    	dbConfig.port = Integer.parseInt(value);
				    else if (isEqual(att, "user"))			    	dbConfig.user = value;
				    else if (isEqual(att, "pwd"))			    	dbConfig.pwd = value;
				    else if (isEqual(att, "dbname"))		    	dbConfig.dbname = value;
				    else if (isEqual(att, "serverid"))		    	dbConfig.serverid = Integer.parseInt(value);
				    else if (isEqual(att, "type"))			    	dbConfig.setType(value);
				    else if (isEqual(att, "instanceName"))	    	dbConfig.instanceName = value;
				    else if (isEqual(att, "SerailNo")) {
				    	if (!dbConfig.info.setSNStr(value))	return false;
				    }
				    else if (isEqual(att, "DateFormat"))	    	dbConfig.DateFormat = value;
				    else if (isEqual(att, "WaitTime"))		    	dbConfig.waittime = Integer.parseInt(value);
				    else if (isEqual(att, "MasterID"))		    	dbConfig.masterID = Integer.parseInt(value);
				    else  				
				    	ErrorHelper.errExit("DBInfo中出现未知属性：" + att.getName());
				}				
				break;
			}
			if (StrHelp.TEqual(name, Tags.MasterDB)) dbConfig.setType(Tags.MySql); 
			LOGGER.info("获取" + name + "配置成功");
		} catch (DocumentException e) {
			LOGGER.error("获取" + name + "配置失败：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Get Attributes of <RepTableList><Table> 
	 * @param table
	 * @param attrList
	 * @return
	 */
	private void getRepTableAttr(RepTableNew table, List<Attribute> attrList) 
	{
		String value = "";
		for (int n=0; n<attrList.size(); n++) 
		{
		    Attribute att = (Attribute)attrList.get(n);
		    value = att.getValue().trim();
		    if (isEqual(att, "MasterTable"))   	        table.setMasterTable(value);
		    else if (isEqual(att, "SlaveTable"))  	    table.setSlaveTable(value);
		    else if (isEqual(att, "KeyField"))   	    table.setKeyFields(value);
		    else if (isEqual(att, "CheckField"))   	    table.setCheckField(value);
		    else if (isEqual(att, "NeedRepValue")) 	    table.setNeedRepValue(value);
		    else if (isEqual(att, "heterogeneous")) 	table.setHeterogeneous(value);
		    else if (isEqual(att, "mergedTable"))    	table.setMergedTable(value);
		    else
		    	ErrorHelper.errExit("RepTableList中出现未知属性：" + att.getName());
		}
	}
	
	private void getRepTableFields(RepTableNew table, Element elem, List<RepField> fields) {
		String value = "";
		for(Iterator f=elem.elementIterator(); f.hasNext();)
		{
			RepField field = new RepField();
			Element elemField = (Element)f.next();
			List attrList2 = elemField.attributes();
			for (int n1=0; n1<attrList2.size(); n1++) 
			{
			    Attribute att = (Attribute)attrList2.get(n1);
			    value = att.getValue().trim();
			    if (isEqual(att, "MasterField"))
			    {
			    	field.masterfield = value;
			    	field.isNew = field.masterfield.equalsIgnoreCase("");
			    }
			    else if (isEqual(att, "SlaveField"))    	field.slavefield = value;
			    else if (isEqual(att, "DefaultValue"))   	field.defvalue = value;
			    else if (isEqual(att, "DefaultType")) {
			    	String type = value;
			    	if (type.equalsIgnoreCase("CUID")) {
			    		field.isCuidDefValue = true;
			    		table.cuidDefault = true;
			    	}
			    }
			    else
			    	ErrorHelper.errExit("列中出现未知属性：" + att.getName());
			}
			fields.add(field);
		}
		
	}
	
	public boolean getRepTables(List<ITableReplicator> tables) {
		try {
			File inputXml = new File(this.filename);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			for(Iterator i=root.elementIterator(); i.hasNext();)
			{
				Element head = (Element)i.next();
				if (!head.getName().trim().equalsIgnoreCase("RepTableList")) continue;
				for(Iterator j=head.elementIterator(); j.hasNext();)
				{
					Element elem = (Element) j.next();
					RepTableNew table = new RepTableNew();
					tables.add(table);
					getRepTableAttr(table, elem.attributes());
					List<RepField> fields = table.getConfFields();
					getRepTableFields(table, elem, fields);
				}
			}
			LOGGER.info("获取待复制表配置成功");
		} catch (DocumentException e) {
			LOGGER.error("获取待复制表配置失败：" + e.getMessage());
			return false;
		}
		return true;
	}
	
}