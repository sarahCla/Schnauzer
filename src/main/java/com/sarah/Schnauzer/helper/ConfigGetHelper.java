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
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.MemberField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.ScoreField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Fields.ValueField;
import com.sarah.Schnauzer.listener.TableReplicator.Redis.Impl.RedisSchnauzer;
import com.sarah.tools.type.StrHelp;

/**
* @author SarahCla
*/
public class ConfigGetHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigGetHelper.class);
	
	private Element getRootElem() {
		Element root = null;
		try {
			File inputXml = new File(Tags.ConfigFile);
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputXml);
			root = document.getRootElement();
		} catch (DocumentException e) {
			LOGGER.error(Infos.OpenConfigFile + Infos.Failed + "：" + e.getMessage());
			return null;
		}		
		return root;
	}
	
	private boolean isEqual(Attribute att, String name) {
		return att.getName().trim().equalsIgnoreCase(name);
	}
	
	private boolean isNotEqual(Attribute att, String name) {
		return (!att.getName().trim().equalsIgnoreCase(name));
	}
	
	public boolean getMailList(List<String> recipients) {
		try {
			Element root = getRootElem();
			recipients.clear();			
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
		} catch (Exception e) {
			LOGGER.error(Infos.GetMailConf + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		LOGGER.info(Infos.MailRegist, recipients.size());
		return true;
	}	

	public boolean getHeartBeatSet(HeartBeatInfo info) {
		try {
			Element root = getRootElem();
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
		} catch (Exception e) {
			LOGGER.error(Infos.GetHeartBeatConf + Infos.Failed + ":" + e.getMessage());
			return false;
		}
		return true;
		
	}
	
	public boolean getSlaveDBConfig(DBConnectorConfig dbConfig, String name) {
		try {
			Element root = getRootElem();
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
		} catch (Exception e) {
			LOGGER.error( Infos.GetDBConfigs + name + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean getMasterDBConfig(DBConnectorConfig dbConfig, String name) {
		try {
			Element root = getRootElem();
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
		} catch (Exception e) {
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
	
	public boolean getRDBRepTables(List<ITableReplicator> tables) {
		try {
			Element root = getRootElem();
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
		} catch (Exception e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	private void getRDBRepTableAttr(RDBSchnauzer table, List<Attribute> attrList) 
	{
		String value = "";
		for (int n=0; n<attrList.size(); n++) 
		{
		    Attribute att = (Attribute)attrList.get(n);
		    value = att.getValue().trim();
		    if (isEqual(att, RDBRepTableAttr.MasterTable))          table.setMasterTable(value);
		    else if (isEqual(att, RDBRepTableAttr.SlaveTable)) 	    table.setSlaveTable(value);
		    else if (isEqual(att, RDBRepTableAttr.KeyField))   	    table.setKeyFields(value);
		    else if (isEqual(att, RDBRepTableAttr.CheckField)) 	    table.setCheckField(value);
		    else if (isEqual(att, RDBRepTableAttr.NeedRepValue))   table.setNeedRepValue(value);
		    else if (isEqual(att, RDBRepTableAttr.heterogeneous)) 	table.setHeterogeneous(value);
		    else if (isEqual(att, RDBRepTableAttr.mergedTable))    	table.setMergedTable(value);
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
			    	field.isNew = StrHelp.TEmpty(field.masterfield);
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
	
	public Boolean getRedisTables(List<RedisSchnauzer> tables) {
		try {
			Element root = getRootElem();
			for(Iterator i=root.elementIterator(); i.hasNext();)
			{
				Element head = (Element)i.next();
				if (!StrHelp.TEqual(head.getName(), Tags.RedisRepTableList)) continue;
				for(Iterator j=head.elementIterator(); j.hasNext();)
				{
					Element elem = (Element) j.next();
					RedisSchnauzer table = new RedisSchnauzer();
					tables.add(table);
					getRedisTableAttr(table, elem.attributes());
					getRedisTableFields(table, elem);
				}
			}
			LOGGER.info(Infos.GetTableConfigs + Infos.OK);
		} catch (Exception e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;

	}
	
	private void getRedisTableAttr(RedisSchnauzer table, List<Attribute> attrList) {
		String value = "";
		for (int n=0; n<attrList.size(); n++) 
		{
		    Attribute att = (Attribute)attrList.get(n);
		    value = att.getValue().trim();
		    if (isEqual(att, RedisRepTableAttr.MasterTable))        table.setMasterTable(value);
		    else if (isEqual(att, RedisRepTableAttr.SlaveKey ))    table.setSlaveKey(value);
		    else if (isEqual(att, RedisRepTableAttr.DataType))   	table.setDataType(value);
		    else if (isEqual(att, RedisRepTableAttr.KeyField)) 	    table.setKeyFields(value);
		    else
		    	ErrorHelper.errExit(Tags.RedisRepTableList + "->Table" + Infos.IllegalAttr + att.getName());
		}
	}
	
	private void getCheckFields(Element elem, List<CheckField> ckfields) {
		List attrs = elem.attributes();
		CheckField field = new CheckField();
		String value = "";
		for (int n1=0; n1<attrs.size(); n1++) 
		{
		    Attribute att = (Attribute)attrs.get(n1);
		    value = att.getValue().trim();
		    if (isEqual(att, RedisCheckFieldAttr.MasterField)) {
		    	field.masterfield = value;
		    } else if (isEqual(att, RedisCheckFieldAttr.Value)) {
		    	field.setValue(value);
		    } else if (isEqual(att, RedisCheckFieldAttr.Operator)) {
		    	field.setOperator(value);
		    } else {
		    	ErrorHelper.errExit(Tags.RedisRepTableList + "->" + Tags.RedisCheckField + Infos.IllegalAttr + att.getName());
		    }
		}
		ckfields.add(field);
	}

	private void getValueField(Element elem, RedisSchnauzer table) {
		List attrs = elem.attributes();
		if (table.vlfield!=null)
			ErrorHelper.errExit("Redis Table " + table.getMasterTableName() + " already have a ValueField ");
		table.vlfield = new ValueField();
		String value = "";
		for (int n1=0; n1<attrs.size(); n1++) 
		{
		    Attribute att = (Attribute)attrs.get(n1);
		    value = att.getValue().trim();
		    if (isEqual(att, RedisBaseFieldAttr.MasterField)) {
		    	table.vlfield.masterfield = value;
		    } else {
		    	ErrorHelper.errExit(Tags.RedisRepTableList + "->" + Tags.RedisValueField + Infos.IllegalAttr + att.getName());
		    }
		}
	}

	private void getMemberField(Element elem, RedisSchnauzer table) {
		List attrs = elem.attributes();
		if (table.memfield!=null)
			ErrorHelper.errExit("Redis Table " + table.getMasterTableName() + " already have a MemberField ");
		
		table.memfield = new MemberField();
		String value = "";
		for (int n1=0; n1<attrs.size(); n1++) 
		{
		    Attribute att = (Attribute)attrs.get(n1);
		    value = att.getValue().trim();
		    if (isEqual(att, RedisBaseFieldAttr.MasterField)) {
		    	table.memfield.masterfield = value;
		    } else {
		    	ErrorHelper.errExit(Tags.RedisRepTableList + "->" + Tags.RedisMemberField + Infos.IllegalAttr + att.getName());
		    }
		}
	}
	
	private void getScoreField(Element elem, RedisSchnauzer table) {
		List attrs = elem.attributes();
		if (table.scorefield!=null)
			ErrorHelper.errExit("Redis Table " + table.getMasterTableName() + " already have a ScoreField ");
		
		table.scorefield = new ScoreField();
		String value = "";
		for (int n1=0; n1<attrs.size(); n1++) 
		{
		    Attribute att = (Attribute)attrs.get(n1);
		    value = att.getValue().trim();
		    if (isEqual(att, RedisBaseFieldAttr.MasterField)) {
		    	table.scorefield.masterfield = value;
		    } else {
		    	ErrorHelper.errExit(Tags.RedisRepTableList + "->" + Tags.RedisScoreField + Infos.IllegalAttr + att.getName());
		    }
		}
	}
	
	private void getRedisTableFields(RedisSchnauzer table, Element elem) {
		List<CheckField> ckfields = table.getCheckFields();
		
		String value = "";
		String tag = "";
		for(Iterator f=elem.elementIterator(); f.hasNext();)
		{
			Element elemField = (Element)f.next();
			tag = elemField.getName();
			if (StrHelp.TEqual(tag, Tags.RedisCheckField)) 
				getCheckFields(elemField, ckfields);
			else if (StrHelp.TEqual(tag, Tags.RedisMemberField))
				getMemberField(elemField, table);
			else if (StrHelp.TEqual(tag, Tags.RedisScoreField))
				getScoreField(elemField, table);
			else if (StrHelp.TEqual(tag, Tags.RedisValueField))
				getValueField(elemField, table);
		    else
		    	ErrorHelper.errExit(Tags.RedisRepTableList + "->" + Tags.RedisTable + Infos.IllegalTag + tag);
		}
	}
	
	
}