package org.genia.HTTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class TerminChecker {
	private static String IMAGE_URL = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
	private static String POST_URL = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do";
	private static String NO_DATES = "Unfortunately, there are no appointments available at this time.";
	private static String WRONG_TEXT = "The entered text was wrong";
	private static String HAS_DATES = "Appointments are available";
	private static String CAPTCHA_FILE = "captcha.jpg";
	
	CaptchaSolver captchaSolver;
	VisaType visaType = VisaType.NATIONAL;

	HttpClient client = new HttpClient();
	
	CheckResult result = new CheckResult();

	public TerminChecker(CaptchaSolver captchaSolver) {
		this.captchaSolver = captchaSolver;
	}

	public CheckResult checkTermins() {
		
		String captchaText = null;

		GetMethod getMethod = new GetMethod(IMAGE_URL);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(3, false));

		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {
				return CheckResult.error("Get method failed: " + getMethod.getStatusLine());
			}

			saveCaptchaImage(getMethod.getResponseBodyAsStream(), CAPTCHA_FILE);

			captchaText = captchaSolver.solveCaptcha(CAPTCHA_FILE);
			if (captchaText == null) {
				return CheckResult.error("Capcha text is NULL");
			}

			String responseBody = submitCaptchaForm(captchaText); 	//for guest visa
//			String responseBody = FakeResponse.getFakeResponse();
			if (responseBody == null) {
				return CheckResult.error("Response body is NULL");
			}

			if(responseBody.contains(WRONG_TEXT)) {
				// TODO: Possibly make reportCaptchaAsIncorrect a generic method in the interface.
				if (captchaSolver instanceof DeathByCaptchaSolver) {
					DeathByCaptchaSolver dbcSolver = (DeathByCaptchaSolver) captchaSolver;
					dbcSolver.reportCaptchaAsIncorrect();
				}
				throw new WrongTextExeption("The entered text was wrong. Captcha text: " + captchaText);
			}

			if(responseBody.contains(NO_DATES)) {
				result.status = Status.NO_APPOINTMENTS;
			} else if(responseBody.contains(HAS_DATES)) {
				result.status = Status.HAS_APPOINTMENTS;
				result.appointments = parseDates(responseBody);
			} else {
				System.out.println(responseBody);
				return CheckResult.error("Cannot parse dates");
			}
			
			//check also next 3 month
			String[] responses = checkNextMonth(3);
			
			for (String response : responses) {
				if (response.contains(HAS_DATES)) {
					result.status = Status.HAS_APPOINTMENTS;
					result.appointments.addAll(parseDates(response));
				}
			}
			
		} catch (HttpException e) {
			result.errorMessage = "Fatal protocol violation: " + e.getMessage();
			result.status = Status.OTHER_ERROR;
			Logger.logError(result.errorMessage);
		} catch (IOException e) {
			result.errorMessage = "Fatal transport error: " + e.getMessage();
			result.status = Status.OTHER_ERROR;
			Logger.logError(result.errorMessage);
		} catch (WrongTextExeption e) {
			result.errorMessage = e.getMessage();
			result.status = Status.CAPTCHA_ERROR;
			Logger.logError(result.errorMessage);
		} finally {
			// Release the connection.
			getMethod.releaseConnection();
		}  

		return result;
	}
	
	private String submitCaptchaForm(String captchaText) throws HttpException, IOException {

		PostMethod post = new PostMethod(POST_URL);
		post.addParameter("action:appointment_showMonth", "Weiter");
		post.addParameter("captchaText", captchaText);
		post.addParameter("locationCode", "kiew");
		post.setParameter("request_locale", "en");
		
		switch (visaType) {
		case GUEST:
			post.addParameter("categoryId", "584");
			post.addParameter("realmId", "357");
			break;
		case NATIONAL:
			post.addParameter("categoryId", "906");
			post.addParameter("realmId", "561");
			break;
		default:	//default - guest visa
			post.addParameter("categoryId", "584");
			post.addParameter("realmId", "357");
			break;
		}
		
		int statusCode = client.executeMethod(post);

		if (statusCode != HttpStatus.SC_OK) {
			Logger.logError("Post method failed: " + post.getStatusLine());
			return null;
		}
		
		return post.getResponseBodyAsString();
	}
	
	private String[] checkNextMonth(int plusMonth) throws HttpException, IOException {
		
		String addStrGuest = "?request_locale=en&locationCode=kiew&realmId=561&categoryId=906&dateStr=";
		String addStrNational = "?request_locale=en&locationCode=kiew&realmId=561&categoryId=906&dateStr=";
		String[] responses = new String[plusMonth];
		
		GetMethod getMethod = null;
		Calendar c = Calendar.getInstance();
		
		for(int i = 0; i < plusMonth; i++) {
			c.add(Calendar.MONTH, plusMonth);
			String dateStr = new SimpleDateFormat("dd.MM.yyyy").format(c.getTime());
			
			switch (visaType) {
			case GUEST:
				getMethod = new GetMethod(POST_URL + addStrGuest + dateStr);
				break;
			case NATIONAL:
				getMethod = new GetMethod(POST_URL + addStrNational + dateStr);
				break;
			default:
				getMethod = new GetMethod(POST_URL + addStrGuest + dateStr);
				break;
			}
			
			getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
					new DefaultHttpMethodRetryHandler(3, false));
			
			int statusCode = client.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {
				Logger.logError("Get method failed: " + getMethod.getStatusLine());
				return responses;
			}
			responses[i] = getMethod.getResponseBodyAsString();
			
		}
		return responses;
	}
	
	private void saveCaptchaImage(InputStream is, String fileName) {
		try (FileOutputStream fos = new FileOutputStream(new File(fileName))) {
			int inByte;
			while((inByte = is.read()) != -1) 
				fos.write(inByte);
			is.close();
		} catch (IOException e) {
			Logger.logError("Error saving captcha image. " + e.getMessage());
		}
		
		
	}
	
	public List<String> parseDates(String response) {
		
		List<String> dateList = new ArrayList<>();	
		
//		Matcher matcher = Pattern.compile("<h4>[ ]*([^< ]+)[ ]*<").matcher(response);
//		
//		while (matcher.find()) {
//			dateList.add(matcher.group(1));
//		}
//		
		
		
		String start = "<h4>";
		String end = "</h4>";
		
		int startIndex = response.indexOf(start);
		int endIndex = response.indexOf(end);
		while (startIndex != -1) {
			String date = response.substring(startIndex + 5, endIndex).trim();
			dateList.add(date);
			startIndex = response.indexOf(start, endIndex);
			endIndex = response.indexOf(end, startIndex);
		}
		return dateList;
	}
	
	public void sendNotification(String email) {
		if (email == null) {
			return;
		}
		Properties creds = new Properties();
		try {
			creds.load(new FileInputStream("mailCredentials.properties"));
		} catch (FileNotFoundException e) {
			Logger.logError("Mail credentials not found.");
			e.printStackTrace();
		} catch (IOException e) {
			Logger.logError("Error while loading credentials.");
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
	
	public static boolean isDatesChanged(CheckResult result) {
		CheckResult prevResult = CheckResult.restoreLastResult();
		return !result.equals(prevResult);
	}
}
