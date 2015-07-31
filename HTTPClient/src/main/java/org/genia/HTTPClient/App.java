package org.genia.HTTPClient;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;
import java.util.Scanner;

public class App 
{
		private static String imageUrl = "https://service2.diplo.de/rktermin/extern/captcha.jpg?locationCode=kiew";
		private static String postUrl = "https://service2.diplo.de/rktermin/extern/appointment_showMonth.do";
		private static String noDates = "Unfortunately, there are no appointments available at this time.";
		private static String wrongText = "The entered text was wrong";
		public static void main(String[] args) {
	    // Create an instance of HttpClient.
	    HttpClient client = new HttpClient();

	    // Create a method instance.
	    GetMethod method = new GetMethod(imageUrl);
	    
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
	      post.setParameter("request_locale", "en");	//Request english lang instead of deuch


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
