/**
 * 
 * @author SarahCla
 */
package com.sarah.tools.email;

import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class SimpleMailSender {
	 
    private final transient Properties props = System.getProperties();
    private transient MailAuthenticator authenticator;
    private transient Session session;
 
    public SimpleMailSender(final String smtpHostName, final String username, final String password) {
    init(username, password, smtpHostName);
    }
 
    public SimpleMailSender(final String username, final String password) {
    	final String smtpHostName = "smtp." + username.split("@")[1];
    	init(username, password, smtpHostName);
    }
 
    private void init(String username, String password, String smtpHostName) {
    	props.put("mail.smtp.auth", "true");
    	props.put("mail.smtp.host", smtpHostName);
    	authenticator = new MailAuthenticator(username, password);
    	session = Session.getInstance(props, authenticator);
    }
 
    public void send(String recipient, String subject, Object content) throws AddressException, MessagingException {
    	final MimeMessage message = new MimeMessage(session);
    	message.setFrom(new InternetAddress(authenticator.getUsername()));
    	message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
    	message.setSubject(subject);
    	message.setContent(content.toString(), "text/html;charset=utf-8");
    	Transport.send(message);
    }
 
    public void send(List<String> recipients, String subject, Object content) throws AddressException, MessagingException {
    	final MimeMessage message = new MimeMessage(session);
    	message.setFrom(new InternetAddress(authenticator.getUsername()));
    	final int num = recipients.size();
    	InternetAddress[] addresses = new InternetAddress[num];
	    for (int i = 0; i < num; i++) {
	        addresses[i] = new InternetAddress(recipients.get(i));
	    }
	    message.setRecipients(RecipientType.TO, addresses);
	    message.setSubject(subject);
	    message.setContent(content.toString(), "text/html;charset=utf-8");
	    Transport.send(message);
    }
}