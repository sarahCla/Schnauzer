package com.google.code.or.listener;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.code.or.OpenReplicator;
import com.sarah.Schnauzer.helper.Infos;
import com.sarah.Schnauzer.helper.DB.MySQL.MySQLDbHelper;

/**
 * 
 * @SarahCla
 */
public class SocketGuard extends Thread {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketGuard.class);
	
	private OpenReplicator or;
	Boolean isOK = true;
	private MySQLDbHelper mHelper;
	
	public SocketGuard(OpenReplicator or, MySQLDbHelper mHelper) 
	{
		this.or = or;
		this.mHelper = mHelper;
	}

	public void run() 
	{
		Long timespan = 60000L;
		while(isOK) {
			try {
				Thread.sleep(timespan);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				if (mHelper.isClosed())
					throw new RuntimeException(Infos.ConMaster + Infos.Failed);
			} catch (Exception e) {
				isOK = false;
				try {
					this.or.stop4Redo(1, TimeUnit.SECONDS);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				LOGGER.error("socket is stopping: " + e.getMessage());
			}
		}
		LOGGER.error("SocketGuard stopped");
	}
}
