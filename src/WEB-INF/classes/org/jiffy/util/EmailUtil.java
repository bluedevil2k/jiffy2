package org.jiffy.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.mail.SimpleEmail;

public class EmailUtil 
{
	public static Logger logger = LogManager.getLogger();
	
	// Send the message on a separate thread, so we don't block
	public static void send(String to, String subject, String message) throws Exception
	{
		Thread thread = new Thread(){
			
			@Override
			public void run(){
				try
				{
					SimpleEmail email = new SimpleEmail();
				
					email.addTo(to);
					email.setHostName(Jiffy.getValue("mailOutgoingServer"));
					email.setFrom(Jiffy.getValue("mailFromEmail"), Jiffy.getValue("mailFromName"));
					email.setSubject(subject);
					email.setMsg(message);
					email.setAuthentication(Jiffy.getValue("mailOutgoingUsername"), Jiffy.getValue("mailOutgoingPassword"));
					
					if (Jiffy.getValue("mailOutgoingServer").indexOf("@gmail") > -1)
					{
						email.setSSL(true);
						email.setSslSmtpPort(Jiffy.getValue("mailSMTPPort"));
					}
					else
					{
						email.setSmtpPort(Jiffy.getInt("mailSMTPPort"));
					}
					
					// every once in a while the email fails to send - if this happens, try sending it again
					try
					{
						email.send();
					}
					catch (Exception ex)
					{
						try
						{
							email.sendMimeMessage();
						}
						catch (Exception ex2)
						{
							throw ex2;
						}
					}

				}
				catch (Exception ex) { LogUtil.printErrorDetails(logger, ex); }
			}
		};
		
		thread.start();
	}
	
	public static void send(String to, String subject, String message, String outgoingServer, String fromEmail, String fromName, String outgoingUsername, String outgoingPassword, int smtpPort) throws Exception
	{
		Thread thread = new Thread(){
			
			@Override
			public void run(){
				try
				{
					SimpleEmail email = new SimpleEmail();
				
					email.addTo(to);
					email.setHostName(outgoingServer);
					email.setFrom(fromEmail, fromName);
					email.setSubject(subject);
					email.setMsg(message);
					email.setAuthentication(outgoingUsername, outgoingPassword);
					
					if (outgoingServer.indexOf("@gmail") > -1)
					{
						email.setSSL(true);
						email.setSslSmtpPort("" + smtpPort);
					}
					else
					{
						email.setSmtpPort(smtpPort);
					}
					
					// every once in a while the email fails to send - if this happens, try sending it again
					try
					{
						email.send();
					}
					catch (Exception ex)
					{
						try
						{
							email.sendMimeMessage();
						}
						catch (Exception ex2)
						{
							throw ex2;
						}
					}

				}
				catch (Exception ex) { LogUtil.printErrorDetails(logger, ex); }
			}
		};
		
		thread.start();
	}
}
