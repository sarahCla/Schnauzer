//CUID.java
package com.sarah.tools.cuid;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.tools.localinfo.LocalInfo;
import com.sarah.tools.type.TypeTransfer;

/**
 * 
 * @author SarahCla
 */
public class CUID {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CUID.class);
	
	private static long GetLo()
    {
        long ticks = System.nanoTime() / 10; 
        return ticks;
    }
	
	public static String getCuid(LocalInfo info) {
		String cuid = getHexCuid(info);
		return "0x" + cuid;
		/*
		BigInteger b = new BigInteger(cuid, 16);
		long value = b.longValue();
		String sR=Long.toString(value);
		return sR; */
	}
	
	public static String getHexCuid(LocalInfo info) {
		String cuid = info.getSNStr();
		long ticks = GetLo() - info.getMinDataTime()*100;
		String st = TypeTransfer.byteToHex((int)((ticks & 0xff0000000000L)>>5*8)) +
				TypeTransfer.byteToHex((int)((ticks & 0xff00000000L)>>4*8)) +
				TypeTransfer.byteToHex((int)((ticks & 0xff000000L)>>3*8)) +
				TypeTransfer.byteToHex((int)((ticks & 0xff0000L)>>2*8)) +
				TypeTransfer.byteToHex((int)((ticks & 0xff00L)>>1*8)) +
				TypeTransfer.byteToHex((int)((ticks & 0xffL)));
		cuid += st;
		return cuid;
	}
}
