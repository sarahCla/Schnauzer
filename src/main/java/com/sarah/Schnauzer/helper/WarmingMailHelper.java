package com.sarah.Schnauzer.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sarah.tools.email.SimpleMailSender;



/**
 * 
 * @author SarahCla
 */
public class WarmingMailHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(WarmingMailHelper.class);
	private List<String> recipients;
	private SimpleMailSender serviceSms;
	private String filename;
	
	public boolean needSend() {
		return (this.recipients.size()>0);
	}
	
	public WarmingMailHelper(String filename) {
		this.filename = filename;
		ini();
	}
	
	private void ini() {
		serviceSms = new SimpleMailSender("RepRobot@163.com", "reprobot001");
		recipients = new ArrayList<String>();
		loadRecipients();
	}
	
	private void loadRecipients() {
		File file = new File(this.filename);
		if ((this.filename.equals("")) || (!file.exists()))
		{
			LOGGER.info("没有配置报警邮件");
		} else
		{
			ConfigGetHelper helper = new ConfigGetHelper();
			helper.getMailList(recipients);
		}
	}
	
	public boolean send(String title, String context) {
	    try {
	        for (String recipient : recipients) {
	        	serviceSms.send(recipient, title, context);
	        }
	    } catch (AddressException e) {
	        e.printStackTrace();
	        return false;
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        return false;
	    }			
	    return true;
	}
}