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
package com.google.code.or.binlog.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.or.binlog.BinlogEventParser;
import com.google.code.or.binlog.impl.event.BinlogEventV4HeaderImpl;
import com.google.code.or.io.XInputStream;
import com.google.code.or.net.Transport;
import com.google.code.or.net.impl.TransportImpl;
import com.google.code.or.net.impl.packet.EOFPacket;
import com.google.code.or.net.impl.packet.ErrorPacket;
import com.google.code.or.net.impl.packet.OKPacket;
import com.sarah.tools.localinfo.LocalInfoGetter;

/**
 * 
 * @author Jingqi Xu
 */
public class ReplicationBasedBinlogParser extends AbstractBinlogParser {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationBasedBinlogParser.class);
	
	//
	public Transport transport;
	protected String binlogFileName;
	
	public Boolean sockStarted = false;
	
	/**
	 * 
	 */
	public ReplicationBasedBinlogParser() {
	}
	
	@Override
	protected void doStart() throws Exception {
		// NOP
	}

	@Override
	protected void doStop(long timeout, TimeUnit unit) throws Exception {
		// NOP
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
	
	public String getBinlogFileName() {
		return binlogFileName;
	}

	public void setBinlogFileName(String binlogFileName) {
		this.binlogFileName = binlogFileName;
	}

	/**
	 * 
	 */
	@Override
	protected void doParse() throws Exception {
		final XInputStream is = this.transport.getInputStream();
		final Context context = new Context(this.binlogFileName);
		LOGGER.info("PID=" + LocalInfoGetter.getPID());
		while(isRunning()) {
			try {
				final int packetLength = is.readInt(3);
				final int packetSequence = is.readInt(1);
				is.setReadLimit(packetLength); // Ensure the packet boundary
				
				sockStarted = true;
			
				final int packetMarker = is.readInt(1);
				
				if(packetMarker != OKPacket.PACKET_MARKER) { // 0x00
					if((byte)packetMarker == ErrorPacket.PACKET_MARKER) {
						final ErrorPacket packet = ErrorPacket.valueOf(packetLength, packetSequence, packetMarker, is);
						throw new NestableRuntimeException(packet.toString());
					} else if((byte)packetMarker == EOFPacket.PACKET_MARKER) {
						//final EOFPacket packet = EOFPacket.valueOf(packetLength, packetSequence, packetMarker, is);
						running2.set(false);
						LOGGER.info("========Get EOFPacket should retry==========PID=" + LocalInfoGetter.getPID());						
						//continue;
						//throw new NestableRuntimeException(packet.toString());
					} else {
						throw new NestableRuntimeException("assertion failed, invalid packet marker: " + packetMarker);
					}
				}
				
				// Parse the event header
				final BinlogEventV4HeaderImpl header = new BinlogEventV4HeaderImpl();
				header.setTimestamp(is.readLong(4) * 1000L);
				header.setEventType(is.readInt(1));
				header.setServerId(is.readLong(4));
				header.setEventLength(is.readInt(4));
				header.setNextPosition(is.readLong(4));
				header.setFlags(is.readInt(2));
				header.setTimestampOfReceipt(System.currentTimeMillis());
				if(isVerbose() && LOGGER.isInfoEnabled()) {
					//LOGGER.info("received an event, sequence: {}, header: {}", packetSequence, header);
				}
				
				// Parse the event body
				if(this.eventFilter != null && !this.eventFilter.accepts(header, context)) {
					this.defaultParser.parse(is, header, context);
				} else {
					BinlogEventParser parser = getEventParser(header.getEventType());
					if(parser == null) parser = this.defaultParser;
					parser.parse(is, header, context);
				}
				
				// Ensure the packet boundary
				if(is.available() != 0) {
					throw new NestableRuntimeException("assertion failed, available: " + is.available() + ", event type: " + header.getEventType());
				}
			} catch (Exception e) {
				LOGGER.error(e.toString() + "===PID=" + LocalInfoGetter.getPID());
				if (e.getMessage().equalsIgnoreCase("noTableMapEvent")) {
					running2.set(false);
					throw new NestableRuntimeException("noTableMapEvent");
				}
				running2.set(false);
				break;
			} finally {
				is.setReadLimit(0);
			}
		}
	}
}
