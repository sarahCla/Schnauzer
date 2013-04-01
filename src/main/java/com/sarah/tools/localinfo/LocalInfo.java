//LocalInfo.java
package com.sarah.tools.localinfo;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sarah.tools.type.TypeCheck;
import com.sarah.tools.type.TypeTransfer;


/**
 * 
 * @author SarahCla
 */
public class LocalInfo {
	public boolean isWindows;
	public boolean isLinux;
	public String HdType;
	private String snFull;
	private String ustr;
	private byte[] sns = new byte[2];
	private long minDateTime;
	
	public LocalInfo() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.YEAR,2008);
		c.set(Calendar.MONTH,7);
		c.set(Calendar.DATE,11);
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		this.minDateTime = c.getTimeInMillis(); 
	}
	
	public long getMinDataTime() {
		return this.minDateTime;
	}
	
	public byte[] getSNBytes() {
		return sns;
	}
	
	public String getSNStr() {
		return ustr;
	}
	
	public boolean setSNStr(String sn) {
		if (!TypeCheck.is2ByteHexStr(sn.trim())) return false; 
		this.ustr = sn;
		return true;
	}
	
	public void setSerialNo(String serialNo) {
		if (serialNo.isEmpty()) {
			this.snFull = "";
			this.ustr = "";
		} else {
			this.snFull = serialNo.trim();
			this.ustr = this.snFull.substring(snFull.length()-4, snFull.length());
			sns = TypeTransfer.HexString2Bytes(this.ustr);
		}
	}
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
		.append("isWindows", isWindows)
		.append("isLinux", isLinux)
		.append("HdType", HdType)
		.append("硬盘序列号", snFull)
		.append("序列号", this.ustr).toString();
	}
}