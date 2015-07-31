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
	      Scanner sc = new Scanner(System.in);
	      String capchaText = sc.nextLine();

	      //Form and execute POST method
	      PostMethod post = new PostMethod(postUrl);
	      post.addParameter("action:appointment_showMonth", "Weiter");
	      post.addParameter("captchaText", capchaText);
	      post.addParameter("categoryId", "584");
	      post.addParameter("locationCode", "kiew");
	      post.addParameter("realmId", "357");


	      statusCode = client.executeMethod(post);

	      if (statusCode != HttpStatus.SC_OK) {
	    	  System.err.println("Post method failed: " + post.getStatusLine());
	      }

	      // Read the response body.
	      byte[] responseBody = post.getResponseBody();
	      // Deal with the response.
	      
	      
	      // Use caution: ensure correct character encoding and is not binary data
	      System.out.println(new String(responseBody));

	    } catch (HttpException e) {
	      System.err.println("Fatal protocol violation: " + e.getMessage());
	      e.printStackTrace();
	    } catch (IOException e) {
	      System.err.println("Fatal transport error: " + e.getMessage());
	      e.printStackTrace();
	    } finally {
	      // Release the connection.
	      method.releaseConnection();
	    }  
	  }
}
