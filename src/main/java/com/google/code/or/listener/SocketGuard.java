package com.google.code.or.listener;

import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.code.or.OpenReplicator;
import com.google.code.or.net.impl.TransportImpl;
import com.sarah.Schnauzer.helper.ErrorHelper;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.DB.SlaveHelperFactory;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLDbHelper;

/**
 * 
 * @SarahCla
 */
public class SocketGuard extends Thread {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketGuard.class);
	
	private OpenReplicator or;
	private TransportImpl trans; 
	Boolean isOK = true;
	private MySQLDbHelper mHelper;
	
	public SocketGuard(OpenReplicator or, MySQLDbHelper mHelper) 
	{
		this.or = or;
		this.mHelper = mHelper;
	}

	public void run() 
	{
		
		while(isOK) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				LOGGER.info("check the connection ...");
				//ResultSet rsMaster = mHelper.getRS("select 1");
				//if (rsMaster==null)
				if (mHelper.isClosed())
					throw new RuntimeException(Infos.ConMaster + Infos.Failed);
			} catch (Exception e) {
				isOK = false;
				LOGGER.error("socket been stoped" + e.getMessage());
				throw new RuntimeException("socket been stoped" + e.getMessage());
			}
		}
	}
}
