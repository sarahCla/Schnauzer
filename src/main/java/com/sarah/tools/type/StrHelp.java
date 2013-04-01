package com.sarah.tools.type;

public class  StrHelp {
	public static Boolean equal(String str1, String str2) {
		return str1.equalsIgnoreCase(str2);
	}
	
	public static Boolean notEqual(String str1, String str2) {
		return (!str1.equalsIgnoreCase(str2));
	}
	
	public static Boolean caseEqual(String str1, String str2) {
		return str1.equals(str2);
	}
	
	
	public static Boolean notCaseEqual(String str1, String str2) {
		return (!str1.equals(str2));
	}
	
}
