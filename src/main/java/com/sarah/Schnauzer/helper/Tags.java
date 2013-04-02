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

/**
 * 
 * @author SarahCla
 */
public interface Tags {
	
	String ConfigFile = "Config.xml";
	
	String HeartBeat = "HeartBeat";
	
	String MailLists = "WarmingSendMailList";
	String MasterDB = "MasterDBInfo";
	String SlaveDB = "SlaveDBInfo";
	
	String RDBRepTableList = "RepTableList";
	String RedisRepTableList = "RedisRepTableList";
	
	String RDBField = "RepTableList->Table->Field";
	
	String RedisTable = "Table";
	String RedisCheckField = "CheckField"; 
	String RedisValueField = "ValueField";
	String RedisMemberField = "MemberField";
	String RedisScoreField = "ScoreField";
	
	String MySql = "MySql";
	String binlog = "binlog";
	String pos = "pos";
	String softname = "Schnauzer";
	String repTable = "RepStatus";
	String version = "2.0.0.1";
	
}
