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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.PrintWriter;  
  
import javax.xml.parsers.DocumentBuilder;  
import javax.xml.parsers.DocumentBuilderFactory;  

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Document;    
import javax.xml.transform.OutputKeys;  
import javax.xml.transform.Transformer;  
import javax.xml.transform.TransformerException;  
import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.dom.DOMSource;  
import javax.xml.transform.stream.StreamResult;  
  

import com.sarah.Schnauzer.Attrs.*;
import com.sarah.Schnauzer.heartbeat.HeartBeatInfo;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.ITableReplicator;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RepField;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.RDBSchnauzer;
import com.sarah.Schnauzer.listener.TableReplicator.RDB.Impl.SASRDBSchnauzer;
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
	private String SchnauzerID = "1";
	
	public ConfigGetHelper(String id) {
		this.SchnauzerID = id;
	}
	
	private boolean isXNode(Node node, String name) {
		return (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(name));
	}
	
    private Document load(String filename) {  
        Document document = null;  
        try {  
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder builder = factory.newDocumentBuilder();  
            document = (Document)builder.parse(new File(filename));  
            document.normalize();  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        return document;  
    }	
	
    private Node getMailNode() {
        Document document = load(Tags.ConfigFile);  
        Node root = document.getDocumentElement();  
        NodeList secondNodes = root.getChildNodes();
        for (int i=0; i<secondNodes.getLength(); i++) {
        	Node secondNode = secondNodes.item(i);
        	if (!isXNode(secondNode, Tags.MailLists)) continue;
      		return secondNode;
        }
		return null;
    }
    
	private Node getRootNode() {
        Document document = load(Tags.ConfigFile);  
        Node root = document.getDocumentElement();  
        NodeList secondNodes = root.getChildNodes();
        for (int i=0; i<secondNodes.getLength(); i++) {
        	Node secondNode = secondNodes.item(i);
        	if (!isXNode(secondNode, "Schnauzer")) continue;
    		Node atr = secondNodes.item(i).getAttributes().getNamedItem("ID"); 
        	if (atr!=null && atr.getTextContent().equalsIgnoreCase(SchnauzerID)) {
        		return secondNode;
        	}
        }
		return null;
	}
	
	private String getAttr(NamedNodeMap alist, String attrName, String title) {
		for(int i=0; i<alist.getLength(); i++) {
			if (alist.item(i).getNodeName().equalsIgnoreCase(attrName)) {
				return alist.item(i).getNodeValue().trim();
			}
		}
		ErrorHelper.errExit(title + Infos.Failed  + "：" + attrName + " not set");
		return "";
	}
	
	private String getDAttr(NamedNodeMap alist, String attrName) {
		for(int i=0; i<alist.getLength(); i++) {
			if (alist.item(i).getNodeName().equalsIgnoreCase(attrName)) {
				return alist.item(i).getNodeValue();
			}
		}
		return "";		
	}
	
	private String getDBAttr(String name, NamedNodeMap alist, String attrName) {
		for(int i=0; i<alist.getLength(); i++) {
			if (alist.item(i).getNodeName().equalsIgnoreCase(attrName)) {
				return alist.item(i).getNodeValue();
			}
		}
		ErrorHelper.errExit( Infos.GetDBConfigs + name + Infos.Failed + "：" + attrName + " not set");
		return "";
	}
	
	
	public boolean getMailList(List<String> recipients, List<String> SendMail, List<String> Mailpwd) {
		try {
			Node root = getMailNode();
			NamedNodeMap alist = root.getAttributes();
			SendMail.add(getAttr(alist, "Sender", Infos.GetMailConf));
			Mailpwd.add(getAttr(alist, "pwd", Infos.GetMailConf));
			NodeList list = root.getChildNodes();
			for(int i=0; i<list.getLength(); i++) {
				Node node = list.item(i);
				if (!isXNode(node, Tags.Mail)) continue;
				recipients.add(node.getTextContent());
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
			Node root = getRootNode();
			NodeList list = root.getChildNodes();
			for(int i=0; i<list.getLength(); i++) {
				Node node = list.item(i);
				if (!isXNode(node, Tags.HeartBeat)) continue; 
				NamedNodeMap alist = node.getAttributes();
				info.host = getAttr(alist, HeartBeatAttr.Host, Infos.GetHeartBeatConf);
				info.port = Integer.parseInt(getAttr(alist, HeartBeatAttr.Port, Infos.GetHeartBeatConf));
				info.Interval = Long.parseLong(getAttr(alist, HeartBeatAttr.Interval, Infos.GetHeartBeatConf));
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
			Node root = getRootNode();
			NodeList list = root.getChildNodes();
			for(int i=0; i<list.getLength(); i++) {
				Node node = list.item(i);
				if (!isXNode(node, name)) continue; 
				NamedNodeMap alist = node.getAttributes();
				dbConfig.host   = getDBAttr(name, alist, SlaveDBAttr.Host);
				dbConfig.port   = Integer.parseInt(getDBAttr(name, alist, SlaveDBAttr.Port));
				dbConfig.user   = getDBAttr(name, alist, SlaveDBAttr.User);
				dbConfig.pwd    = getDBAttr(name, alist, SlaveDBAttr.Pwd);
				dbConfig.dbname = getDBAttr(name, alist, SlaveDBAttr.DbName);
				dbConfig.setType(getDBAttr(name, alist, SlaveDBAttr.Type));
				if (!dbConfig.info.setSNStr(getDBAttr(name, alist, SlaveDBAttr.SerailNo))) return false;
				String value = getDAttr(alist, SlaveDBAttr.WaitTime);
				if (value!="")
					dbConfig.waittime = Integer.parseInt(value);
				dbConfig.masterID = Integer.parseInt(SchnauzerID); //Integer.parseInt(getDBAttr(name, alist, SlaveDBAttr.MasterID));
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



	public boolean getMasterDBConfig(DBConnectorConfig dbConfig, String name) {
		try {
			Node root = getRootNode();
			NodeList list = root.getChildNodes();
			for(int i=0; i<list.getLength(); i++) {
				Node node = list.item(i);
				if (!isXNode(node, name)) continue; 
				NamedNodeMap alist = node.getAttributes();
				dbConfig.host     = getDBAttr(name, alist, MasterDBAttr.Host);
				dbConfig.port     = Integer.parseInt(getDBAttr(name, alist, MasterDBAttr.Port));
				dbConfig.user     = getDBAttr(name, alist, MasterDBAttr.User);
				dbConfig.pwd      = getDBAttr(name, alist, MasterDBAttr.Pwd);
				dbConfig.dbname   = getDBAttr(name, alist, MasterDBAttr.DbName);
				dbConfig.serverid = Integer.parseInt(SchnauzerID); //Integer.parseInt(getDBAttr(name, alist, MasterDBAttr.ServerID));
				dbConfig.DateFormat = getDBAttr(name, alist, MasterDBAttr.DateFormat);
				dbConfig.SchnauzerClass = getDAttr(alist, MasterDBAttr.ClassName);
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
			Node root = getRootNode();
			NodeList list = root.getChildNodes();
			for(int i=0; i<list.getLength(); i++) {
				Node node = list.item(i);
				if (!isXNode(node, Tags.RDBRepTableList)) continue;
				NodeList tnodes = node.getChildNodes();
				for (int j=0; j<tnodes.getLength(); j++) {
					Node tnode = tnodes.item(j);
					if (!isXNode(tnode, Tags.RDBRepTable)) continue;
					if (getDAttr(tnode.getAttributes(), Tags.RepClass).equalsIgnoreCase(SASRDBSchnauzer.class.getSimpleName()))
					{
						SASRDBSchnauzer table = new SASRDBSchnauzer();
						tables.add(table);
						getTableAttrs(table, tnode.getAttributes());
						List<RepField> fields = table.getConfFields();
						getRDBRepTableFields(table, tnode, fields);
					} else
					{
						RDBSchnauzer table = new RDBSchnauzer();
						tables.add(table);
						getTableAttrs(table, tnode.getAttributes());
						List<RepField> fields = table.getConfFields();
						getRDBRepTableFields(table, tnode, fields);
					}
				}
			}
			LOGGER.info(Infos.GetTableConfigs + Infos.OK);
		} catch (Exception e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	private void getTableAttrs(RDBSchnauzer table, NamedNodeMap attrs) 
	{
		String title = Tags.RDBRepTable;
		table.setMasterTable(getAttr(attrs, RDBRepTableAttr.MasterTable, title));
		table.setSlaveTable(getAttr(attrs, RDBRepTableAttr.SlaveTable, title));
		table.setKeyFields(getAttr(attrs, RDBRepTableAttr.KeyField, title));
		table.setCheckField(getDAttr(attrs, RDBRepTableAttr.CheckField));
		table.setNeedRepValue(getDAttr(attrs, RDBRepTableAttr.NeedRepValue));
		table.setHeterogeneous(getAttr(attrs, RDBRepTableAttr.heterogeneous, title));
		table.setMergedTable(getAttr(attrs, RDBRepTableAttr.mergedTable, title));
	}
	
	private void getRDBRepTableFields(RDBSchnauzer table, Node tnode, List<RepField> fields) {
		NodeList fnodes = tnode.getChildNodes();
		for(int i=0; i<fnodes.getLength(); i++) {
			Node fnode = fnodes.item(i);
			if (!isXNode(fnode, "Field")) continue;
			RepField field = new RepField();
			NamedNodeMap attrs = fnode.getAttributes();
			field.masterfield = getAttr(attrs, RDBFieldAttr.MasterField, Tags.RDBField);
			field.isNew       = StrHelp.TEmpty(field.masterfield);
			field.slavefield  = getAttr(attrs, RDBFieldAttr.SlaveField, Tags.RDBField);
			field.defvalue    = getDAttr(attrs, RDBFieldAttr.DefaultValue);
			String type = getDAttr(attrs, RDBFieldAttr.DefaultType);
	    	if (type.equalsIgnoreCase("CUID")) {
	    		field.isCuidDefValue = true;
	    		table.cuidDefault = true;
	    	}			
	    	fields.add(field);
		}
	}
	
	public Boolean getRedisTables(List<RedisSchnauzer> tables) {
		try {
			Node root = getRootNode();
			NodeList nodes = root.getChildNodes();
			for(int i=0; i<nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (!isXNode(node, Tags.RedisRepTableList)) continue;
				NodeList tnodes = node.getChildNodes();
				for (int j=0; j<tnodes.getLength(); j++) {
					Node tnode = tnodes.item(j);
					if (!isXNode(node, "Table")) continue;
					RedisSchnauzer table = new RedisSchnauzer();
					tables.add(table);
					getRedisTableAttr(table, tnode.getAttributes());
					getRedisTableFields(table, tnode);
				}
			}
			LOGGER.info(Infos.GetTableConfigs + Infos.OK);
		} catch (Exception e) {
			LOGGER.error(Infos.GetTableConfigs + Infos.Failed + "：" + e.getMessage());
			return false;
		}
		return true;
	}
	
	private void getRedisTableAttr(RedisSchnauzer table, NamedNodeMap attrs) {
		String title = Tags.RedisRepTableList + "->Table";
		table.setMasterTable(getAttr(attrs, RedisRepTableAttr.MasterTable, title));
		table.setSlaveKey(getAttr(attrs, RedisRepTableAttr.SlaveKey, title));
		table.setDataType(getAttr(attrs, RedisRepTableAttr.DataType, title));
		table.setKeyFields(getAttr(attrs, RedisRepTableAttr.KeyField, title));
		table.setSQL(getAttr(attrs, RedisRepTableAttr.SQL, title));
	}
	
	private void getCheckField(Node node, List<CheckField> ckfields) {
		NamedNodeMap attrs = node.getAttributes();
		CheckField field = new CheckField();
		String title = Tags.RedisRepTableList + "->" + Tags.RedisCheckField;
		field.masterfield = getAttr(attrs, RedisCheckFieldAttr.MasterField, title);
		field.setValue(getAttr(attrs, RedisCheckFieldAttr.Value, title));
		field.setOperator(getAttr(attrs, RedisCheckFieldAttr.Operator, title));
		ckfields.add(field);
	}
	

	private void getValueField(Node node, RedisSchnauzer table) {
		if (table.vlfield!=null)
			ErrorHelper.errExit("Redis Table " + table.getMasterTableName() + " already have a ValueField ");
		table.vlfield = new ValueField();
		NamedNodeMap attrs = node.getAttributes();
		String title = Tags.RedisRepTableList + "->" + Tags.RedisValueField;
		table.vlfield.masterfield = getAttr(attrs, RedisBaseFieldAttr.MasterField, title);
	}
	
	private void getMemberField(Node node, RedisSchnauzer table) {
		if (table.memfield!=null)
			ErrorHelper.errExit("Redis Table " + table.getMasterTableName() + " already have a MemberField ");
		table.memfield = new MemberField();
		NamedNodeMap attrs = node.getAttributes();
		String title = Tags.RedisRepTableList + "->" + Tags.RedisMemberField;
		table.memfield.masterfield = getAttr(attrs, RedisBaseFieldAttr.MasterField, title);
	}
	
	private void getScoreField(Node node, RedisSchnauzer table) {
		if (table.scorefield!=null)
			ErrorHelper.errExit("Redis Table " + table.getMasterTableName() + " already have a ScoreField ");
		table.scorefield = new ScoreField();
		String title = Tags.RedisRepTableList + "->" + Tags.RedisScoreField;
		NamedNodeMap attrs = node.getAttributes();
		table.scorefield.masterfield = getAttr(attrs, RedisScoreFieldAttr.MasterField, title);
		table.scorefield.multiplier = Double.parseDouble(getAttr(attrs, RedisScoreFieldAttr.Multiplier, title));
	}
	
	private void getRedisTableFields(RedisSchnauzer table, Node node) {
		List<CheckField> ckfields = table.getCheckFields();
		NodeList fnodes = node.getChildNodes();
		for(int i=0; i<fnodes.getLength(); i++) {
			Node fnode = fnodes.item(i);
			if (isXNode(fnode, Tags.RedisCheckField))
				getCheckField(fnode, ckfields);
			else if (isXNode(fnode, Tags.RedisMemberField))
				getMemberField(fnode, table);
			else if (isXNode(fnode, Tags.RedisScoreField))
				getScoreField(fnode, table);
			else if (isXNode(fnode, Tags.RedisValueField))
				getValueField(fnode, table);
				
		}
	}
}