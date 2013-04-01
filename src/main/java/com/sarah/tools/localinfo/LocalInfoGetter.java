//LocalInfoGetter.java
package com.sarah.tools.localinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author SarahCla
 */
public class LocalInfoGetter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalInfoGetter.class);
	
	public static String getOsName() {
		String os = "";
		os = System.getProperty("os.name");
		return os;
	}
	
	public static boolean getHdType(LocalInfo info) {
		if (info.isLinux) return true;
		String ls = "";
		String command = "fdisk -l";
		Process process;
		try {
			process = Runtime.getRuntime().exec(command);
			BufferedReader bufreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((ls=bufreader.readLine())!=null) {
				if (ls.contains("sd")) {
					info.HdType = "scsi";
					return true;
				} 
				if (ls.contains("hd")) {
					info.HdType = "ide";
					return true;
				}
				bufreader.close();
			}
		} catch(IOException e) {
			LOGGER.error("获取硬盘类型失败  " + e.getMessage());
			return false;
		}
		LOGGER.error("获取硬盘类型失败  unknown type");
		return false;
	}
	
	public static LocalInfo getLocalInfo() {
		LocalInfo info = new LocalInfo();
		String os = getOsName();
		if (os.startsWith("Linux")) {
			info.isLinux = true;
			if (getHdType(info)) 
			{
				getHdSerialInfo(info);
				return info;
			}
			else
				return null;
		}
		if (os.startsWith("Windows")) 
		{ 
			info.isWindows = true;
			getHdSerialInfo(info);
		}	
		return info;
	}
	
	public static boolean getHdSerialInfo(LocalInfo info) {
		if (info.isWindows) {
			getWindowsHdSerial(info);
			return true;
		}
		else if (info.isLinux) {
			getLinuxHdSerial(info);
			return true;
		}
		return false;
	}
	
	public static boolean getLinuxHdSerial(LocalInfo info) {
		String ls = "";
		String HdSerial = "";
		try {
			Process process = null; 
			if (info.HdType.equalsIgnoreCase("scsi"))
				process = Runtime.getRuntime().exec("hdparm -i /dev/sda");
			else if (info.HdType.equalsIgnoreCase("ide"))
				process = Runtime.getRuntime().exec("hdparm -i /dev/hda");
			if (process==null) return false;
			BufferedReader bufreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			int index = -1;
			int len = "SerialNo".length();
			while ((ls=bufreader.readLine())!=null) {
				index = ls.indexOf("SerialNo");
				if (index>-1) {
					HdSerial = ls.substring(index+len+1);
					break;
				}
			}
			bufreader.close();
		} catch(IOException e) {
			LOGGER.error("获取硬盘序列号失败  " + e.getMessage());
			return true;
		}
		info.setSerialNo(HdSerial);
		return true;		
	}

	
	public static boolean getWindowsHdSerial(LocalInfo info) {
		String ls = "";
		String HdSerial = "";
		try {
			Process process = Runtime.getRuntime().exec("cmd /c dir c:");
			BufferedReader bufreader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GB2312"));
			int index = -1;
			int len = "卷的序列号是".length();
			while ((ls=bufreader.readLine())!=null) {
				index = ls.indexOf("卷的序列号是");
				if (index==-1) {
					index = ls.indexOf("SerialNo");
					if (index>-1)
						len = "SerialNo".length(); 
				}
				if (index>-1) {
					HdSerial = ls.substring(index+len, ls.length());
					break;
				}
			}
			bufreader.close();
		} catch(IOException e) {
			LOGGER.error("获取硬盘序列号失败  " + e.getMessage());
			return false;
		}
		info.setSerialNo(HdSerial);
		return true;		
	}
}