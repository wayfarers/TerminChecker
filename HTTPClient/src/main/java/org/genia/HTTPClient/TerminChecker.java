package org.genia.HTTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;

//TODO: Email notifications
//TODO: Cron job on the server
//TODO: Learn basics of regexp. Examination!!! Hahahaha

public class TerminChecker {
	private static String IMAGE_URL = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
	private static String POST_URL = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do";
	private static String NO_DATES = "Unfortunately, there are no appointments available at this time.";
	private static String WRONG_TEXT = "The entered text was wrong";
	private static String HAS_DATES = "Appointments are available";
	
	CaptchaSolver captchaSolver;

	HttpClient client = new HttpClient();

	public TerminChecker(CaptchaSolver captchaSolver) {
		this.captchaSolver = captchaSolver;
	}

	public CheckResult checkTermins() {
		CheckResult result = new CheckResult();
		String captchaText = null;
		String fileName = "D:\\captcha.jpg";

		GetMethod getMethod = new GetMethod(IMAGE_URL);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(3, false));

		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {
				return CheckResult.error("Get method failed: " + getMethod.getStatusLine());
			}

			saveCaptchaImage(getMethod.getResponseBodyAsStream(), fileName);

			captchaText = captchaSolver.solveCaptcha(fileName);

			String responseBody = submitCaptchaForm(captchaText);

			if(responseBody.contains(WRONG_TEXT)) {
				// TODO - possibly make reportCaptchaAsIncorrect a generic method in the interface.
				if (captchaSolver instanceof DeathByCaptchaSolver) {
					DeathByCaptchaSolver dbcSolver = (DeathByCaptchaSolver) captchaSolver;
					dbcSolver.reportCaptchaAsIncorrect();
				}
				throw new WrongTextExeption("The entered text was wrong");
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

		} catch (HttpException e) {
			result.errorMessage = "Fatal protocol violation: " + e.getMessage();
			result.status = Status.OTHER_ERROR;
		} catch (IOException e) {
			result.errorMessage = "Fatal transport error: " + e.getMessage();
			result.status = Status.OTHER_ERROR;
		} catch (WrongTextExeption e) {
			result.errorMessage = e.getMessage();
			result.status = Status.CAPTCHA_ERROR;
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
		post.addParameter("categoryId", "584");
		post.addParameter("locationCode", "kiew");
		post.addParameter("realmId", "357");
		post.setParameter("request_locale", "en");
		
		int statusCode = client.executeMethod(post);

		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Post method failed: " + post.getStatusLine());
		}
		
		return post.getResponseBodyAsString();
	}

	private void saveCaptchaImage(InputStream is, String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(fileName));
		int inByte;
		while((inByte = is.read()) != -1) 
			fos.write(inByte);
		is.close();
		fos.close();
	}
	
	public List<String> parseDates(String response) {
		
		List<String> dateList = new ArrayList<>();	//Diamond doesn't work
//		
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
			String date = StringUtils.deleteWhitespace(response.substring(startIndex + 5, endIndex));
			dateList.add(StringUtils.replace(date, "DAY", "DAY - "));
			startIndex = response.indexOf(start, endIndex);
//			endIndex = response.indexOf(end, startIndex);
//			" sdfgsdfg    ".trim();
		}
		return dateList;
	}
	
	public void sendNotification(String email) {
		//TODO: implement notification
	}
	
	public static boolean isDatesChanged(CheckResult result) {
		CheckResult prevResult = CheckResult.restoreLastResult();
		return !result.equals(prevResult);
	}
}
