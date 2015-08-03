package org.genia.HTTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

//TODO: DeathcByCaptcha
//TODO: Email notifications
//TODO: Cron job on the server

public class TerminChecker {
	private static String IMAGE_URL = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
	private static String POST_URL = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do";
	private static String NO_DATES = "Unfortunately, there are no appointments available at this time.";
	private static String WRONG_TEXT = "The entered text was wrong";
	
	CaptchaSolver captchaSolver;

	HttpClient client = new HttpClient();

	public TerminChecker(CaptchaSolver captchaSolver) {
		this.captchaSolver = captchaSolver;
	}

	public CheckResult checkTermins() {
		CheckResult result = new CheckResult();
		String captchaText = null;
		String fileName = "D:\\capcha.jpg";

		GetMethod getMethod = new GetMethod(IMAGE_URL);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(3, false));

		try {
			int statusCode = client.executeMethod(getMethod);

			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Get method failed: " + getMethod.getStatusLine());
			}

			saveCaptchaImage(getMethod.getResponseBodyAsStream(), fileName);

			captchaText = captchaSolver.solveCaptcha(fileName);

			String responseBody = sendCaptchaText(captchaText);

			if(responseBody.contains(WRONG_TEXT)) {
				throw new WrongTextExeption("The entered text was wrong");
			}

			if(responseBody.contains(NO_DATES)) {
				result.status = Status.NO_APPOINTMENTS;
			} else {
				System.out.println(responseBody);
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
	
	private String sendCaptchaText(String captchaText) throws HttpException, IOException {

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
}
