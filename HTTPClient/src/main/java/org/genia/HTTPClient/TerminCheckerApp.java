package org.genia.HTTPClient;

public class TerminCheckerApp {
	public static void main(String[] args) {
		TerminChecker checker = new TerminChecker(new DeathByCaptchaSolver());
		
		CheckResult result = checker.checkTermins();
		switch (result.status) {
		case HAS_APPOINTMENTS:
			// Notify by email on change
			System.out.println("There are new appointments.");
			for (String date : result.appointments) {
				System.out.println(date);
			}
			if (TerminChecker.isDatesChanged(result)) {
				checker.sendNotification("genia@gmail.com");
				System.out.println("Notification was sent to genia@gmail.com");
			}
			break;
		case NO_APPOINTMENTS:
			// Log it.
			System.out.println("There are no appointments.");
			if (TerminChecker.isDatesChanged(result)) {
				checker.sendNotification("genia@gmail.com");
				System.out.println("Notification was sent to genia@gmail.com");
			}
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
		
		result.saveOnDisk();
	}
}