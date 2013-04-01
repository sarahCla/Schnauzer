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
package com.google.code.or.binlog.impl.event;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.code.or.binlog.BinlogEventV4Header;

/**
 * 
 * @author Jingqi Xu
 */
public final class BinlogEventV4HeaderImpl implements BinlogEventV4Header {
	//
	private long timestamp;
	private int eventType;
	private long serverId;
	private long eventLength;
	private long nextPosition;
	private int flags;
	private long timestampOfReceipt;
	
	private String getTypeName(int eventType) {
		switch(eventType)
		{
		case 0 : return "UNKNOWN_EVENT";
		case 1 : return "START_EVENT_V3";
		case 2 : return "QUERY_EVENT";
		case 3 : return "STOP_EVENT";
		case 4 : return "ROTATE_EVENT";
		case 5 : return "INTVAR_EVENT";
		case 6 : return "LOAD_EVENT";
		case 7 : return "SLAVE_EVENT";
		case 8 : return "CREATE_FILE_EVENT";
		case 9 : return "APPEND_BLOCK_EVENT";
		case 10 : return "EXEC_LOAD_EVENT";
		case 11 : return "DELETE_FILE_EVENT";
		case 12 : return "NEW_LOAD_EVENT";
		case 13 : return "RAND_EVENT";
		case 14 : return "USER_VAR_EVENT";
		case 15 : return "FORMAT_DESCRIPTION_EVENT";
		case 16 : return "XID_EVENT";
		case 17 : return "BEGIN_LOAD_QUERY_EVENT";
		case 18 : return "EXECUTE_LOAD_QUERY_EVENT";
		case 19 : return "TABLE_MAP_EVENT";
		case 20 : return "PRE_GA_WRITE_ROWS_EVENT";
		case 21 : return "PRE_GA_UPDATE_ROWS_EVENT";
		case 22 : return "PRE_GA_DELETE_ROWS_EVENT";
		case 23 : return "WRITE_ROWS_EVENT";
		case 24 : return "UPDATE_ROWS_EVENT";
		case 25 : return "DELETE_ROWS_EVENT";
		case 26 : return "INCIDENT_EVENT";
		case 27 : return "HEARTBEAT_LOG_EVENT";
		case 28 : return "IGNORABLE_LOG_EVENT";
		case 29 : return "ROWS_QUERY_LOG_EVENT";
		case 30 : return "WRITE_ROWS_EVENT_V2";
		case 31 : return "UPDATE_ROWS_EVENT_V2";
		case 32 : return "DELETE_ROWS_EVENT_V2";
		case 33 : return "GTID_LOG_EVENT";
		case 34 : return "ANONYMOUS_GTID_LOG_EVENT";
		case 35 : return "PREVIOUS_GTIDS_LOG_EVENT";
		}
		return "未知";
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		/*
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("timestamp", timestamp)
		.append("eventType", eventType)
		.append("typeName", getTypeName(eventType))
		.append("serverId", serverId)
		.append("eventLength", eventLength)
		.append("nextPosition", nextPosition)
		.append("flags", flags)
		.append("timestampOfReceipt", timestampOfReceipt).toString();
		*/
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("typeName", getTypeName(eventType)).toString();
	}
	
	/**
	 * 
	 */
	public int getHeaderLength() {
		return 19;
	}
	
	public long getPosition() {
		return this.nextPosition - this.eventLength;
	}
	
	/**
	 * 
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public int getEventType() {
		return eventType;
	}
	
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	
	public long getServerId() {
		return serverId;
	}
	
	public void setServerId(long serverId) {
		this.serverId = serverId;
	}
	
	public long getEventLength() {
		return eventLength;
	}
	
	public void setEventLength(long eventLength) {
		this.eventLength = eventLength;
	}
	
	public long getNextPosition() {
		return nextPosition;
	}
	
	public void setNextPosition(long nextPosition) {
		this.nextPosition = nextPosition;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public long getTimestampOfReceipt() {
		return timestampOfReceipt;
	}

	public void setTimestampOfReceipt(long timestampOfReceipt) {
		this.timestampOfReceipt = timestampOfReceipt;
	}
}
