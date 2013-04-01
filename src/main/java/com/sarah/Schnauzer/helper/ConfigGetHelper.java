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

import com.sarah.Schnauzer.Attrs.*;
import com.sarah.Schnauzer.heartbeat.HeartBeatInfo;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RDBSchnauzer;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.CheckField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Impl.RedisSchnauzer;
import com.sarah.tools.type.StrHelp;

/**
* @author SarahCla
*/
public class ConfigGetHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigGetHelper.class);
	
	private boolean isEqual(Attribute att, String name) {
		return att.getName().trim().equalsIgnoreCase(name);
	}
	
	private boolean isNotEqual(Attribute att, String name) {
		return (!att.getName().trim().equalsIgnoreCase(name));
	}
	
	public boolean getMailList(List<String> recipients) {
		try {
			File inputXml = new File(Tags.ConfigFile);
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
			LOGGER.error(Infos.GetMailConf + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		LOGGER.info(Infos.MailRegist, recipients.size());
		return true;
	}	

	public boolean getHeartBeatSet(HeartBeatInfo info) {
		try {
			File inputXml = new File(Tags.ConfigFile);
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
				    if (isEqual(att, HeartBeatAttr.Host))    		info.host = value;
				    else if (isEqual(att, HeartBeatAttr.Port))    	info.port = Integer.parseInt(value);
				    else if (isEqual(att, HeartBeatAttr.Interval)) 	info.Interval = Long.parseLong(value);
				    else  {
				    	LOGGER.error(Tags.HeartBeat + Infos.IllegalAttr  + att.getName());
				    	System.exit(-1);
				    }
				}				
				break;
			}
			LOGGER.info(Infos.GetHeartBeatConf + Infos.OK);
		} catch (DocumentException e) {
			LOGGER.error(Infos.GetHeartBeatConf + Infos.Failed + ":" + e.getMessage());
			return false;
		}
		return true;
		
	}
	
	public boolean getSlaveDBConfig(DBConnectorConfig dbConfig, String name) {
		try {
			File inputXml = new File(Tags.ConfigFile);
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
				    if (isEqual(att, SlaveDBAttr.Host))				    	dbConfig.host = value;
				    else if (isEqual(att, SlaveDBAttr.Port))			   	dbConfig.port = Integer.parseInt(value);
				    else if (isEqual(att, SlaveDBAttr.User))			   	dbConfig.user = value;
				    else if (isEqual(att, SlaveDBAttr.Pwd))			    	dbConfig.pwd = value;
				    else if (isEqual(att, SlaveDBAttr.DbName))		    	dbConfig.dbname = value;
				    else if (isEqual(att, SlaveDBAttr.Type))               dbConfig.setType(value); 
				    else if (isEqual(att, SlaveDBAttr.SerailNo)) {
				    	if (!dbConfig.info.setSNStr(value))	return false;
				    }
				    else if (isEqual(att, SlaveDBAttr.WaitTime))		   	dbConfig.waittime = Integer.parseInt(value);
				    else if (isEqual(att, SlaveDBAttr.MasterID))		   	dbConfig.masterID = Integer.parseInt(value);
				    else  				
				    	ErrorHelper.errExit(name + Infos.IllegalAttr + att.getName());
				}				
				break;
			}			 
			LOGGER.info( Infos.GetDBConfigs + name + Infos.OK);
		} catch (DocumentException e) {
			LOGGER.error( Infos.GetDBConfigs + name + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	public boolean getMasterDBConfig(DBConnectorConfig dbConfig, String name) {
		try {
			File inputXml = new File(Tags.ConfigFile);
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
				    if (isEqual(att, MasterDBAttr.Host))				    	dbConfig.host = value;
				    else if (isEqual(att, MasterDBAttr.Port))			    	dbConfig.port = Integer.parseInt(value);
				    else if (isEqual(att, MasterDBAttr.User))			    	dbConfig.user = value;
				    else if (isEqual(att, MasterDBAttr.Pwd))			    	dbConfig.pwd = value;
				    else if (isEqual(att, MasterDBAttr.DbName))		    		dbConfig.dbname = value;
				    else if (isEqual(att, MasterDBAttr.ServerID))		    	dbConfig.serverid = Integer.parseInt(value);
				    else if (isEqual(att, MasterDBAttr.DateFormat))	    		dbConfig.DateFormat = value;
				    else  				
				    	ErrorHelper.errExit(name + Infos.IllegalAttr + att.getName());
				}				
				break;
			}
			if (StrHelp.TEqual(name, Tags.MasterDB)) dbConfig.setType(Tags.MySql); 
			LOGGER.info( Infos.GetDBConfigs + name + Infos.OK);
		} catch (DocumentException e) {
			LOGGER.error( Infos.GetDBConfigs + name + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	
	public boolean getDBConfig(DBConnectorConfig dbConfig, String name) {
		if (StrHelp.TEqual(name, Tags.MasterDB)) 
			return getMasterDBConfig(dbConfig, name);
		else
			return getSlaveDBConfig(dbConfig, name);
	}
	
	/**
	 * Get Attributes of <RepTableList><Table> 
	 * @param table
	 * @param attrList
	 * @return
	 */
	private void getRDBRepTableAttr(RDBSchnauzer table, List<Attribute> attrList) 
	{
		String value = "";
		for (int n=0; n<attrList.size(); n++) 
		{
		    Attribute att = (Attribute)attrList.get(n);
		    value = att.getValue().trim();
		    if (isEqual(att, RDBRepTable.MasterTable))          table.setMasterTable(value);
		    else if (isEqual(att, RDBRepTable.SlaveTable)) 	    table.setSlaveTable(value);
		    else if (isEqual(att, RDBRepTable.KeyField))   	    table.setKeyFields(value);
		    else if (isEqual(att, RDBRepTable.CheckField)) 	    table.setCheckField(value);
		    else if (isEqual(att, RDBRepTable.NeedRepValue))   table.setNeedRepValue(value);
		    else if (isEqual(att, RDBRepTable.heterogeneous)) 	table.setHeterogeneous(value);
		    else if (isEqual(att, RDBRepTable.mergedTable))    	table.setMergedTable(value);
		    else
		    	ErrorHelper.errExit(Tags.RDBRepTableList + Infos.IllegalAttr + att.getName());
		}
	}
	
	private void getRDBRepTableFields(RDBSchnauzer table, Element elem, List<RepField> fields) {
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
			    if (isEqual(att, RDBFieldAttr.MasterField))
			    {
			    	field.masterfield = value;
			    	field.isNew = field.masterfield.equalsIgnoreCase("");
			    }
			    else if (isEqual(att, RDBFieldAttr.SlaveField))    	field.slavefield = value;
			    else if (isEqual(att, RDBFieldAttr.DefaultValue))  	field.defvalue = value;
			    else if (isEqual(att, RDBFieldAttr.DefaultType)) {
			    	String type = value;
			    	if (type.equalsIgnoreCase("CUID")) {
			    		field.isCuidDefValue = true;
			    		table.cuidDefault = true;
			    	}
			    }
			    else
			    	ErrorHelper.errExit(Tags.RDBField + Infos.IllegalAttr + att.getName());
			}
			fields.add(field);
		}
	}
	
	/*
	private void getRedisTableCheckFields(RedisSchnauzer table, Element elem, List<CheckField> fields) {
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
			    if (isEqual(att, RDBFieldAttr.MasterField))
			    {
			    	field.masterfield = value;
			    	field.isNew = field.masterfield.equalsIgnoreCase("");
			    }
			    else if (isEqual(att, RDBFieldAttr.SlaveField))    	field.slavefield = value;
			    else if (isEqual(att, RDBFieldAttr.DefaultValue))  	field.defvalue = value;
			    else if (isEqual(att, RDBFieldAttr.DefaultType)) {
			    	String type = value;
			    	if (type.equalsIgnoreCase("CUID")) {
			    		field.isCuidDefValue = true;
			    		table.cuidDefault = true;
			    	}
			    }
			    else
			    	ErrorHelper.errExit(Tags.RDBField + Infos.IllegalAttr + att.getName());
			}
			fields.add(field);
		}
	}
	*/
	
	public boolean getRDBRepTables(List<ITableReplicator> tables) {
		try {
			File inputXml = new File(Tags.ConfigFile);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			for(Iterator i=root.elementIterator(); i.hasNext();)
			{
				Element head = (Element)i.next();
				if (!StrHelp.TEqual(head.getName(), Tags.RDBRepTableList)) continue;
				for(Iterator j=head.elementIterator(); j.hasNext();)
				{
					Element elem = (Element) j.next();
					RDBSchnauzer table = new RDBSchnauzer();
					tables.add(table);
					getRDBRepTableAttr(table, elem.attributes());
					List<RepField> fields = table.getConfFields();
					getRDBRepTableFields(table, elem, fields);
				}
			}
			LOGGER.info(Infos.GetTableConfigs + Infos.OK);
		} catch (DocumentException e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
}