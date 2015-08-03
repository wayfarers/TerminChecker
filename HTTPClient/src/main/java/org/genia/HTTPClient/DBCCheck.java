package org.genia.HTTPClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.SocketClient;

public class DBCCheck {
	public static void main(String[] args) throws java.lang.Exception
	{
		// Put your DBC username & password here:
		//Client client = (Client)(new HttpClient(args[0], args[1]));

		String userName;
	    String password;

		Properties props = new Properties();
		try {
			props.load(new FileInputStream("credentials.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		userName = props.getProperty("username");
		password = props.getProperty("password");

		Client client = (Client)(new SocketClient(userName, password));
		client.isVerbose = true;

		System.out.println("Your balance is " + client.getBalance() + " US cents");


	}
}
