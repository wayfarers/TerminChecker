package org.genia.HTTPClient;

public class TerminCheckerApp {
	public static void main(String[] args) {
		TerminChecker checker = new TerminChecker(new DeathByCaptchaSolver());
		
		CheckResult result = checker.checkTermins();
		switch (result.status) {
		case HAS_APPOINTMENTS:
			// Notify by email on change
			System.out.println("There is a new appointments.");
			break;
		case NO_APPOINTMENTS:
			// Log it.
			System.out.println("There is no appointments.");
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
}