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
package com.google.code.or;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.binlog.BinlogEventListener;
import com.google.code.or.binlog.BinlogParser;
import com.google.code.or.binlog.BinlogParserListener;
import com.google.code.or.binlog.impl.ReplicationBasedBinlogParser;
import com.google.code.or.binlog.impl.parser.DeleteRowsEventParser;
import com.google.code.or.binlog.impl.parser.DeleteRowsEventV2Parser;
import com.google.code.or.binlog.impl.parser.FormatDescriptionEventParser;
import com.google.code.or.binlog.impl.parser.IncidentEventParser;
import com.google.code.or.binlog.impl.parser.IntvarEventParser;
import com.google.code.or.binlog.impl.parser.QueryEventParser;
import com.google.code.or.binlog.impl.parser.RandEventParser;
import com.google.code.or.binlog.impl.parser.RotateEventParser;
import com.google.code.or.binlog.impl.parser.StopEventParser;
import com.google.code.or.binlog.impl.parser.TableMapEventParser;
import com.google.code.or.binlog.impl.parser.UpdateRowsEventParser;
import com.google.code.or.binlog.impl.parser.UpdateRowsEventV2Parser;
import com.google.code.or.binlog.impl.parser.UserVarEventParser;
import com.google.code.or.binlog.impl.parser.WriteRowsEventParser;
import com.google.code.or.binlog.impl.parser.WriteRowsEventV2Parser;
import com.google.code.or.binlog.impl.parser.XidEventParser;
import com.google.code.or.common.glossary.column.StringColumn;
import com.google.code.or.io.impl.SocketFactoryImpl;
import com.google.code.or.net.Packet;
import com.google.code.or.net.Transport;
import com.google.code.or.net.TransportException;
import com.google.code.or.net.impl.AuthenticatorImpl;
import com.google.code.or.net.impl.TransportImpl;
import com.google.code.or.net.impl.packet.ErrorPacket;
import com.google.code.or.net.impl.packet.command.ComBinlogDumpPacket;
import com.sarah.Schnauzer.helper.DBConnectorConfig;

/**
 * 
 * @author Jingqi Xu
 */
public class OpenReplicator {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenReplicator.class);
	protected DBConnectorConfig master;
	protected DBConnectorConfig slave;
	
	
	protected String encoding = "utf-8";
	protected int level1BufferSize = 1024 * 1024;
	protected int level2BufferSize = 8 * 1024 * 1024;
	protected int socketReceiveBufferSize = 512 * 1024;
	
	//
	protected Transport transport;
	protected BinlogParser binlogParser;
	protected BinlogEventListener binlogEventListener;
	protected final AtomicBoolean running = new AtomicBoolean(false);
	
	
	public void setMasterAndSlave(DBConnectorConfig master,  DBConnectorConfig slave) 
	{
		this.master = master;
		this.slave = slave;
	}
	
	/**
	 * 
	 */
	public boolean isRunning() {
		return this.running.get();
	}
	
	public void start() throws Exception {

		while (1==1)
		{
			if(!this.running.compareAndSet(false, true)) {
				return;
			}

			LOGGER.info("开始注册master数据库[" + this.slave.binlog + "][" + this.slave.pos + "]");
			
			if(this.transport == null) this.transport = getDefaultTransport();
			this.transport.connect(this.master.host, this.master.port);
			
			dumpBinlog();
			
			if(this.binlogParser == null) this.binlogParser = getDefaultBinlogParser();
			this.binlogParser.setEventListener(this.binlogEventListener);
			this.binlogParser.addParserListener(new BinlogParserListener.Adapter() {
				@Override
				public void onStop(BinlogParser parser) {
					stopQuietly(0, TimeUnit.MILLISECONDS);
				}
			});
			
			this.binlogParser.start();

			/*
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			for(String line = br.readLine(); line != null; line = br.readLine()) {
			    if(line.equals("q")) {
			    	stopQuietly(0, TimeUnit.MILLISECONDS);
			        return;
			    }
			}
			*/

			ReplicationBasedBinlogParser p = (ReplicationBasedBinlogParser)this.binlogParser;
			while (p.running2.get()==true) { Thread.sleep(1000); }
			while (p.isRunning()) { Thread.sleep(1000); }
		}
	}

	public void stop(long timeout, TimeUnit unit) throws Exception {
		//
		if(!this.running.compareAndSet(true, false)) {
			return;
		}
		
		//
		this.transport.disconnect();
		this.binlogParser.stop(timeout, unit);
		
	}
	
	public void stopQuietly(long timeout, TimeUnit unit) {
		try {
			stop(timeout, unit);
		} catch(Exception e) {
			// NOP
		}
	}
	

	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	
	public int getLevel1BufferSize() {
		return level1BufferSize;
	}

	public void setLevel1BufferSize(int level1BufferSize) {
		this.level1BufferSize = level1BufferSize;
	}

	public int getLevel2BufferSize() {
		return level2BufferSize;
	}

	public void setLevel2BufferSize(int level2BufferSize) {
		this.level2BufferSize = level2BufferSize;
	}
	
	public int getSocketReceiveBufferSize() {
		return socketReceiveBufferSize;
	}

	public void setSocketReceiveBufferSize(int socketReceiveBufferSize) {
		this.socketReceiveBufferSize = socketReceiveBufferSize;
	}

	/**
	 * 
	 */
	public Transport getTransport() {
		return transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}
	
	public BinlogParser getBinlogParser() {
		return binlogParser;
	}

	public void setBinlogParser(BinlogParser parser) {
		this.binlogParser = parser;
	}
	
	public BinlogEventListener getBinlogEventListener() {
		return binlogEventListener;
	}

	public void setBinlogEventListener(BinlogEventListener listener) {
		this.binlogEventListener = listener;
	}

	/**
	 * 
	 */
	protected void dumpBinlog() throws Exception {
		final ComBinlogDumpPacket command = new ComBinlogDumpPacket();
		command.setBinlogFlag(0);
		command.setServerId(this.master.serverid);
		command.setBinlogPosition(this.slave.pos);
		command.setBinlogFileName(StringColumn.valueOf(this.slave.binlog.getBytes(this.encoding)));
		this.transport.getOutputStream().writePacket(command);
		this.transport.getOutputStream().flush();
		
		//
		final Packet packet = this.transport.getInputStream().readPacket();
		if(packet.getPacketBody()[0] == ErrorPacket.PACKET_MARKER) {
			final ErrorPacket error = ErrorPacket.valueOf(packet);
			throw new TransportException(error);
		} 
	}
	
	protected Transport getDefaultTransport() throws Exception {
		//
		final TransportImpl r = new TransportImpl();
		r.setLevel1BufferSize(this.level1BufferSize);
		r.setLevel2BufferSize(this.level2BufferSize);
		
		//
		final AuthenticatorImpl authenticator = new AuthenticatorImpl();
		authenticator.setUser(this.master.user);
		authenticator.setPassword(this.master.pwd);
		authenticator.setEncoding(this.encoding);
		r.setAuthenticator(authenticator);
		
		//
		final SocketFactoryImpl socketFactory = new SocketFactoryImpl();
		socketFactory.setKeepAlive(true);
		socketFactory.setTcpNoDelay(false);
		socketFactory.setReceiveBufferSize(this.socketReceiveBufferSize);
		r.setSocketFactory(socketFactory);
		return r;
	}
	
	protected ReplicationBasedBinlogParser getDefaultBinlogParser() throws Exception {
		//
		final ReplicationBasedBinlogParser r = new ReplicationBasedBinlogParser();
		r.registgerEventParser(new StopEventParser());
		r.registgerEventParser(new RotateEventParser());
		r.registgerEventParser(new IntvarEventParser());
		r.registgerEventParser(new XidEventParser());
		r.registgerEventParser(new RandEventParser());
		r.registgerEventParser(new QueryEventParser());
		r.registgerEventParser(new UserVarEventParser());
		r.registgerEventParser(new IncidentEventParser());
		r.registgerEventParser(new TableMapEventParser());
		r.registgerEventParser(new WriteRowsEventParser());
		r.registgerEventParser(new UpdateRowsEventParser());
		r.registgerEventParser(new DeleteRowsEventParser());
		r.registgerEventParser(new WriteRowsEventV2Parser());
		r.registgerEventParser(new UpdateRowsEventV2Parser());
		r.registgerEventParser(new DeleteRowsEventV2Parser());
		r.registgerEventParser(new FormatDescriptionEventParser());
		
		//
		r.setTransport(this.transport);
		r.setBinlogFileName(this.master.binlog);
		return r;
	}
}
