//TypeTransfer.java
package com.sarah.tools.type;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 
 * @author SarahCla
 */
public class TypeTransfer {

	
	public static Date stringToDate(String time){
	    SimpleDateFormat formatter;
	    int tempPos=time.indexOf("AD") ;
	    time=time.trim() ;
	    formatter = new SimpleDateFormat ("yyyy.MM.dd G 'at' hh:mm:ss z");
	    if(tempPos>-1){
	      time=time.substring(0,tempPos)+
	           "公元"+time.substring(tempPos+"AD".length());//china
	      formatter = new SimpleDateFormat ("yyyy.MM.dd G 'at' hh:mm:ss z");
	    }
	    tempPos=time.indexOf("-");
	    if(tempPos>-1&&(time.indexOf(" ")<0)){
	      formatter = new SimpleDateFormat ("yyyyMMddHHmmssZ");
	    }
	    else if((time.indexOf("/")>-1) &&(time.indexOf(" ")>-1)){
	      formatter = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss");
	    }
	    else if((time.indexOf("-")>-1) &&(time.indexOf(" ")>-1)){
	      formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	    }
	    else if((time.indexOf("/")>-1) &&(time.indexOf("am")>-1) ||(time.indexOf("pm")>-1)){
	      formatter = new SimpleDateFormat ("yyyy-MM-dd KK:mm:ss a");
	    }
	    else if((time.indexOf("-")>-1) &&(time.indexOf("am")>-1) ||(time.indexOf("pm")>-1)){
	      formatter = new SimpleDateFormat ("yyyy-MM-dd KK:mm:ss a");
	    } 
	    else if (time.indexOf("GMT")>-1) {
	    	formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    }
	    ParsePosition pos = new ParsePosition(0);
	    java.util.Date ctime = formatter.parse(time, pos);

	    return ctime;
	} 
	
	public static String dateToString(Date time){
	    SimpleDateFormat formatter;
	    formatter = new SimpleDateFormat ("yyyy-MM-dd");
	    String ctime = formatter.format(time);
	    return ctime;
	} 
	
	public static String dateTimeToString(Date time){
	    SimpleDateFormat formatter;
	    formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	    String ctime = formatter.format(time);
	    return ctime;
	} 
	
	public static String byteToHex(int b) {
		char b0 = getChar((b & 0xf0)>>4);
		char b1 = getChar(b & 0x0f);
		return String.valueOf(b0) + String.valueOf(b1);
	}
	
	public static char getChar(int b) {
		if (b>=0 && b<=9) {
			return (char)('0'+b);
		}
		if (b>=10 && b<=15) {
			return (char)('A'-10+b);
		}
		throw new IllegalArgumentException("getChar参数值超界" + b);
	}
	
	public static byte uniteBytes(byte hi, byte lo) {
		byte b0 = Byte.decode("0x" + new String(new byte[]{hi})).byteValue();
		b0 = (byte)(b0<<4);
		byte b1 = Byte.decode("0x" + new String(new byte[]{lo})).byteValue();
		byte result = (byte)(b0 ^ b1);
		return result;
	}
	
	public static byte[] HexString2Bytes(String src) {
		int len = src.length()/2; 
		byte[] result = new byte[len];
		byte[] tmp = src.getBytes();
		for (int i=0; i<len; i++) {
			result[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
		}
		return result;
	}
	
	public static String Bytes2HexString(byte[] b) {
		String result = "";
		for (int i=0; i<b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length()==1) 
				hex = '0' + hex;
			result += hex.toUpperCase();
		}
		return result;
	}
}