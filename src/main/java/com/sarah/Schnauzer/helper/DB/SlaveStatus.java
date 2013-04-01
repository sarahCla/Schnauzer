package com.sarah.Schnauzer.helper.DB;

public class SlaveStatus {
	public String binlog = "";
	public int pos = -1;
	public int masterID = -1;
	
	public SlaveStatus(String binlog, int pos, int masterID) {
		this.binlog = binlog;
		this.pos = pos;
		this.masterID = masterID;
	}
	
}
