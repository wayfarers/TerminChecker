package org.genia.HTTPClient;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class App 
{
		private static String IMAGE_URL = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
		private static String postUrl = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do";
		private static String noDates = "Unfortunately, there are no appointments available at this time.";
		private static String wrongText = "The entered text was wrong";
		
		public static void main(String[] args) {
	    // Create an instance of HttpClient.
	    HttpClient client = new HttpClient();

	    // Create a method instance.
	    GetMethod method = new GetMethod(IMAGE_URL);
	    
	    // Provide custom retry handler is necessary
	    
	    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
	    		new DefaultHttpMethodRetryHandler(3, false));

	    try {
	      // Execute the method.

	      int statusCode = client.executeMethod(method);

	      if (statusCode != HttpStatus.SC_OK) {
	        System.err.println("Method failed: " + method.getStatusLine());
	      }

	      //Save capcha image
	      InputStream is = method.getResponseBodyAsStream();
	      FileOutputStream fos = new FileOutputStream(new File("D:\\capcha.jpg"));
	      int inByte;
	      while((inByte = is.read()) != -1) 
	    	  fos.write(inByte);
	      is.close();
	      fos.close();
	      
	      Process process = new ProcessBuilder("cmd","/c", "D:/capcha.jpg").start();	//show image

	      //Enter capcha text
	      System.out.println("Please, enter text from capcha image:");
	      Scanner sc = new Scanner(System.in);
	      String capchaText = sc.nextLine();
	      
	      //Form and execute POST method
	      PostMethod post = new PostMethod(postUrl);
	      post.addParameter("action:appointment_showMonth", "Weiter");
	      post.addParameter("captchaText", capchaText);
	      post.addParameter("categoryId", "584");
	      post.addParameter("locationCode", "kiew");
	      post.addParameter("realmId", "357");
	      post.setParameter("request_locale", "en");	//Request english lang instead of german


	      statusCode = client.executeMethod(post);

	      if (statusCode != HttpStatus.SC_OK) {
	    	  System.err.println("Post method failed: " + post.getStatusLine());
	      }

	      // Read the response body.
	      String responseBody = post.getResponseBodyAsString();
	      // Deal with the response.
	      
	      if(responseBody.contains(wrongText)) {
	    	  throw new WrongTextExeption("The entered text was wrong");
	      }
	      
	      
	      if(responseBody.contains(noDates)) {
	    	  System.out.println(noDates);
	      } else {
	    	// Use caution: ensure correct character encoding and is not binary data
	    	  System.out.println(responseBody);
	      }
	      
	      

	    } catch (HttpException e) {
	      System.err.println("Fatal protocol violation: " + e.getMessage());
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.err.println("Fatal transport error: " + e.getMessage());
	      e.printStackTrace();
	    } catch (WrongTextExeption e) {
			System.out.println(e.getMessage());
		} finally {
	      // Release the connection.
	      method.releaseConnection();
	    }  
	  }
}

class TerminChecker {
	
	CaptchaSolver captchaSolver;
	
	public TerminChecker(CaptchaSolver captchaSolver) {
		this.captchaSolver = captchaSolver;
	}
	
	public CheckResult checkTermins() {
		return null;
	}
}

abstract class CaptchaSolver {
	abstract public String solveCaptcha(String fileName);
}

class DeathByCaptchaSolver extends CaptchaSolver {

	@Override
	public String solveCaptcha(String fileName) {
		return null;
	}
}

class HumanInputSolver extends CaptchaSolver {

	@Override
	public String solveCaptcha(String fileName) {
		return null;
	}
}

enum Status {
	HAS_APPOINTMENTS, 
	NO_APPOINTMENTS, 
	CAPTCHA_ERROR, 
	OTHER_ERROR;
}

class CheckResult {
	Status status;
	public String errorMessage;
	public List<String> appointments;
}


class TerminCheckerApp {
	public static void main(String[] args) {
		TerminChecker checker = new TerminChecker(new HumanInputSolver());
		
		CheckResult result = checker.checkTermins();
		switch (result.status) {
		case HAS_APPOINTMENTS:
			// Notify by email on change
		case NO_APPOINTMENTS:
			// Log it.
		case CAPTCHA_ERROR:
			// Save the picture and log the error
		case OTHER_ERROR:
			// Log the error
		default:
			break;
		}
	}
}

// Proper design
// DeathcByCaptcha
// Email notifications
// Cron job on the server
