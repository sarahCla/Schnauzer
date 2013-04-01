package com.sarah.Schnauzer.heartbeat;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.Schnauzer.helper.DBConnectorConfig;



/**
* @author SarahCla
*/

public class HeartBeatSender extends Thread {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatSender.class);
	
	private HeartBeatInfo info = null;
	private DBConnectorConfig slavedb = null;
	private String msgFix = "";
	private String msgErrorFix = "";
	
	protected AtomicBoolean isRunning = new AtomicBoolean(true); 
	
	public void stopRunning() {  
        isRunning.set(false);  
	} 
	
	public HeartBeatSender(DBConnectorConfig masterdb, DBConnectorConfig slavedb, HeartBeatInfo hinfo) {
		this.slavedb = slavedb;
		this.msgFix =  "<Result><PacketFlag flag=\"1\"/><MasterDBInfo Host=\"" + masterdb.host + "\" port=\"" + masterdb.port + "\" dbname=\"" + masterdb.dbname + "\"/>" +
	             "<SlaveDBInfo Host=\"" + this.slavedb.host + "\" port=\"" + this.slavedb.port + "\" dbname=\"" + 
	             this.slavedb.dbname + "\" type=\"" + this.slavedb.getType() + "\" SerailNo=\"" + hinfo.SerialNo + "\" serverid=\"" + this.slavedb.serverid + 
	             "\" Binlog=\"";
		this.msgErrorFix =  "<Result><PacketFlag flag=\"0\"/><Config Master=\"" + masterdb.host + "\" MasterPort=\"" + masterdb.port + "\" MasterDBName=\"" + masterdb.dbname + "\" Slave=\"" + this.slavedb.host + "\" SlavePort=\"" + this.slavedb.port + "\" SlaveDBName=\"" + 
	             this.slavedb.dbname + "\" type=\"" + this.slavedb.getType() + "\"/>";
		this.info = hinfo;
	}
	
	public void sendError(){
		try{
			Socket sender = new Socket(info.host, info.port);
			OutputStreamWriter osw = new OutputStreamWriter(sender.getOutputStream());
			String msg = msgErrorFix + "<Error ErrorContent=\"" + this.slavedb.errorMsg + "\"/></Result>\n\r";
			osw.write(msg);
			osw.flush();
			osw.close();
			sender.close();
			LOGGER.info("【发送错误信息心跳包成功】" + msg);
		}catch(Exception e){
			LOGGER.error("心跳包发送失败 " + e.getMessage());
		}
	}
	

	@Override
	public void run() {
		try{
			while(isRunning.get()){
				ClientSender.getInstance(info, this.slavedb, msgFix).send();
				synchronized(HeartBeatSender.class){
					Thread.sleep(info.Interval);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			LOGGER.error("心跳包发送失败 : " + e.getMessage());
		}
	}
}