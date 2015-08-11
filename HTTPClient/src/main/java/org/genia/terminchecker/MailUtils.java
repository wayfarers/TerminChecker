package org.genia.terminchecker;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailUtils {
	public static void sendEmail(final Properties credentials, String to, String subject, String body) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(credentials.getProperty("username"),
							credentials.getProperty("password"));
				}
			});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("es.terminchecker@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(body);

			Transport.send(message);

			System.out.println("Email was sent to " + to);

		} catch (MessagingException e) {
			LoggerUtil.logError("Error while sending email. " + e.getMessage());
//			throw new RuntimeException(e);
		}
	}
}
