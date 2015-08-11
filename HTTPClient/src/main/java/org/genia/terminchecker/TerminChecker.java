package org.genia.terminchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

// Separation of concerns (responsibilities)
public class TerminChecker {
	private static String IMAGE_URL = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
	private static String POST_URL = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do";
	private static String NO_DATES = "Unfortunately, there are no appointments available at this time.";
	private static String WRONG_CAPTCHA = "The entered text was wrong";
	private static String HAS_DATES = "Appointments are available";
	private static String CAPTCHA_FILE = "captcha.jpg";
	
	
	final VisaType visaType;
	final int numOfMonths;

	private HttpClient client = new HttpClient();
	final private CaptchaSolver captchaSolver;
	
	

	public TerminChecker(VisaType visaType, int numOfMonths, CaptchaSolver captchaSolver) {
		this.captchaSolver = captchaSolver;
		this.numOfMonths = numOfMonths;
		this.visaType = visaType;
		
		if (numOfMonths <= 0) 
			throw new IllegalArgumentException("Should request at least 1 month");
	}

	public CheckResult checkTermins() {
		CheckResult result = new CheckResult();
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

			String responseBody = submitCaptchaForm(captchaText);
//			String responseBody = FakeResponse.getFakeResponse();
			if (responseBody == null) {
				return CheckResult.error("Response body is NULL");
			}

			if(responseBody.contains(WRONG_CAPTCHA)) {
				// TODO: Possibly make reportCaptchaAsIncorrect a generic method in the interface.
				if (captchaSolver instanceof DeathByCaptchaSolver) {
					DeathByCaptchaSolver dbcSolver = (DeathByCaptchaSolver) captchaSolver;
					dbcSolver.reportCaptchaAsIncorrect();
				}
				
				saveIncorrectCaptcha(captchaText);
				
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
			String[] responses = checkNextMonths(numOfMonths - 1);
			
			for (String response : responses) {
				if (response.contains(HAS_DATES)) {
					result.status = Status.HAS_APPOINTMENTS;
					result.appointments.addAll(parseDates(response));
				}
			}
			
		} catch (HttpException e) {
			result.errorMessage = "Fatal protocol violation: " + e.getMessage();
			result.status = Status.OTHER_ERROR;
			LoggerUtil.logError(result.errorMessage);
		} catch (IOException e) {
			result.errorMessage = "Fatal transport error: " + e.getMessage();
			result.status = Status.OTHER_ERROR;
			LoggerUtil.logError(result.errorMessage);
		} catch (WrongTextExeption e) {
			result.errorMessage = e.getMessage();
			result.status = Status.CAPTCHA_ERROR;
			LoggerUtil.logError(result.errorMessage);
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
		
		post.addParameter("categoryId", "" + visaType.categoryId);
		post.addParameter("realmId", "" + visaType.realmId);
		
		
		int statusCode = client.executeMethod(post);

		if (statusCode != HttpStatus.SC_OK) {
			LoggerUtil.logError("Post method failed: " + post.getStatusLine());
			return null;
		}
		
		return post.getResponseBodyAsString();
	}
	
	private String[] checkNextMonths(int numOfMonths) throws HttpException, IOException {
		
		String addStr = "?request_locale=en&locationCode=kiew&realmId=" + visaType.realmId + "&categoryId=" + visaType.categoryId + "&dateStr=";
		String[] responses = new String[numOfMonths];
		
		GetMethod getMethod = null;
		Calendar c = Calendar.getInstance();
		
		for(int i = 0; i < numOfMonths; i++) {
			c.add(Calendar.MONTH, numOfMonths);
			
			String dateStr = new SimpleDateFormat("dd.MM.yyyy").format(c.getTime());
			getMethod = new GetMethod(POST_URL + addStr + dateStr);
			
			getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
					new DefaultHttpMethodRetryHandler(3, false));
			
			int statusCode = client.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {
				LoggerUtil.logError("Get method failed: " + getMethod.getStatusLine());
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
			LoggerUtil.logError("Error saving captcha image. " + e.getMessage());
		}
	}
	
	private void saveIncorrectCaptcha(String captchaText) {
		File img = new File(CAPTCHA_FILE);
		File wrongCaptchas = new File("wrongCaptchas");
		wrongCaptchas.mkdir();
		img.renameTo(new File(wrongCaptchas.getAbsolutePath() + "/" + captchaText + ".jpg"));
	}
	
	private List<String> parseDates(String response) {
		
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
}
