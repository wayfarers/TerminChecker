package org.genia.HTTPClient;

import java.util.ArrayList;
import java.util.List;

//TODO: check also next 2 month
//TODO: Change Serialization to JSON
//TODO: cron job
//TODO: Learn basics of regexp. Examination!!! Hahahaha

public class TerminCheckerApp {
	public static void main(String[] args) {
		TerminChecker checker = new TerminChecker(new DeathByCaptchaSolver());
		CheckResult result = null;
		List<String> emails = new ArrayList<>();
		emails.add("genia.sushko@gmail.com");
		emails.add("yura.sushko@gmail.com");
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
				if (TerminChecker.isDatesChanged(result)) {
					for (String email : emails) {
						checker.sendNotification(email);
					}
				}
				captchaIncorrect = false;
				break;
			case NO_APPOINTMENTS:
				// Log it.
				System.out.println("There are no appointments.");
				result.appointments.add("There are no appointments.");
				if (TerminChecker.isDatesChanged(result)) {
					for (String email : emails) {
						checker.sendNotification(email);
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
				break;
			default:
				break;
			}
		}
		result.saveOnDisk();
	}
}