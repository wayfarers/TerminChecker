package org.genia.terminchecker;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: Change Serialization to JSON
//TODO: Learn basics of regexp. Examination!!! Hahahaha
//TODO: Logging using SLF + Log4j
//TODO: tests

public class TerminCheckerApp {
	
	static final String RESULT_FILE = "result.dat";
	static final boolean SEND_NOTIFICATIONS = false;
	static final int NUM_OF_MONTH = 4;
	
	private static final Logger logger = LoggerFactory.getLogger(TerminChecker.class);
	
	public static void main(String[] args) {
		
		logger.debug("Started, yay!");
		
		TerminChecker checker = new TerminChecker(VisaType.NATIONAL, NUM_OF_MONTH, new DeathByCaptchaSolver());
		CheckResult result = null;
		List<String> emails = new ArrayList<>();
		emails.add("genia.sushko@gmail.com");
		emails.add("yura.sushko@gmail.com");
		emails.add("ljoljka-2008@yandex.ru");
		boolean captchaIncorrect = true;
		while (captchaIncorrect) {
			result = checker.checkTermins();
			switch (result.status) {
			case HAS_APPOINTMENTS:
				// Notify by email on change
				System.out.println("There are new appointments.");
				for (String date : result.appointments) {
					System.out.println(date);
				}
				if (isDatesChanged(result)) {
					for (String email : emails) {
						sendNotification(result, email);
					}
				}
				captchaIncorrect = false;
				break;
			case NO_APPOINTMENTS:
				// Log it.
				System.out.println("There are no appointments.");
				result.appointments.add("There are no appointments.");
				if (isDatesChanged(result)) {
					for (String email : emails) {
						sendNotification(result, email);
					}
				}
				captchaIncorrect = false;
				break;
			case CAPTCHA_ERROR:
				// Save the picture and log the error
				System.out.println("Wrong captcha text");
				System.out.println(result.errorMessage);
				break;
			case OTHER_ERROR:
				// Log the error
				System.out.println("Other error occured");
				System.out.println(result.errorMessage);
				LoggerUtil.logError("Result status: " + result.status.toString());
				return; 	//Don't need to save previous result if other error occured. Just log the error.
			default:
				break;
			}
		}
		LoggerUtil.logError("Result status: " + result.status.toString());
		result.saveOnDisk(RESULT_FILE);
	}
	
	private static void sendNotification(CheckResult result, String email) {
		
		if (!SEND_NOTIFICATIONS)
			return;
		
		Properties creds = new Properties();
		try {
			creds.load(new FileInputStream("mailCredentials.properties"));
		} catch (FileNotFoundException e) {
			LoggerUtil.logError("Mail credentials not found.");
			e.printStackTrace();
		} catch (IOException e) {
			LoggerUtil.logError("Error while loading credentials.");
			e.printStackTrace();
		}
		String subject = "Termin Checker: changes in dates!";
		String body = "";

		if(result.status == Status.NO_APPOINTMENTS) {
			body = "No dates are available for now.";
		} else if (result.status == Status.HAS_APPOINTMENTS) {
			body += "Appointments are available:\n\n";
			for (String date : result.appointments) {
				body += date + "\n";
			}
		} else {
			body = "Some error occured: " + result.errorMessage;
		}
		
		MailUtils.sendEmail(creds, email, subject, body);
	}
	
	private static boolean isDatesChanged(CheckResult result) {
		CheckResult prevResult = CheckResult.restoreFrom(RESULT_FILE);
		return !result.equals(prevResult);
	}
}