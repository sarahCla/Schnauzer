//TypeCheck.java
package com.sarah.tools.type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * @author SarahCla
 */
public class TypeCheck {
	
	public static boolean is2ByteHexStr(String value) {
		if (value.length()!=4) return false;
		return isHexStr(value);
	}
	
	public static boolean isHexStr(String value) {
		if ((value.length()%2)!=0) return false;
		Pattern p = Pattern.compile("[0-9a-fA-F]+");
        Matcher matcher = p.matcher(value);
		return matcher.matches();		
	}
	
	/*
	public static boolean isByteArrayEqual(byte[] b1, byte[] b2) {
		if (b1.length!=b2.length) return false;
		for (int i=0; i<b1.length; i++) {
			if (b1[i])
		}
	}*/
}
