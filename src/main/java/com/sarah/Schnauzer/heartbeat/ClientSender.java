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

package com.sarah.Schnauzer.heartbeat;


import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.helper.DBConnectorConfig;
import com.sarah.Schnauzer.listener.ClientTableListener;

/**
* @author SarahCla
*/

public class ClientSender{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientSender.class);
	
	private ClientSender(){}
	
	Socket sender = null;
	private static  ClientSender  instance;
	private DBConnectorConfig slavedb = null;
	private OutputStreamWriter osw = null;
	private String msgFix = "";
	private HeartBeatInfo info = null;

	public ClientSender(HeartBeatInfo hinfo, DBConnectorConfig slavedb, String msgFix) {
		this.slavedb = slavedb;
		this.msgFix = msgFix;
		this.info = hinfo;
	}
	
	public static ClientSender getInstance(HeartBeatInfo hinfo, DBConnectorConfig slavedb, String msgFix){
		if(instance==null)
		{
			synchronized(HeartBeatSender.class){ instance = new ClientSender(hinfo, slavedb, msgFix); }
		}
		return instance;
	}

	public void send(){
		try{
			sender = new Socket(info.host, info.port);
			osw = new OutputStreamWriter(sender.getOutputStream());
			String msg = msgFix + this.slavedb.binlog + "\" Position=\"" + this.slavedb.pos + "\"/></Result>\n\r";
			osw.write(msg);
			osw.flush();
			osw.close();
			sender.close();
			LOGGER.info("【发送状态心跳包成功】" + msg);
		}catch(Exception e){
			LOGGER.error("心跳包发送失败 " + e.getMessage());
		}
	}
	
	
}

