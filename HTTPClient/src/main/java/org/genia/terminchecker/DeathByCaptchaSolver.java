package org.genia.terminchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.SocketClient;

public class DeathByCaptchaSolver extends CaptchaSolver {
	
	protected Client dbcClient;
    protected String captchaFilename = null;
    String userName;
    String password;
    Captcha captcha = null;
    
    public DeathByCaptchaSolver() {
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream("credentials.properties"));
		} catch (FileNotFoundException e) {
			LoggerUtil.logError("DBC credentials not found.");
			e.printStackTrace();
		} catch (IOException e) {
			LoggerUtil.logError("Error while loading DBC credentials. " + e.getMessage());
			e.printStackTrace();
		}
		userName = props.getProperty("username");
		password = props.getProperty("password");
		
		dbcClient = (Client)(new SocketClient(userName, password));
	}	
    
	@Override
	public String solveCaptcha(String fileName) {
		dbcClient.isVerbose = true;
		captchaFilename = fileName;
		captcha = null;
		
		try {
            // Put your CAPTCHA image file, file object, input stream,
            // or vector of bytes here:
            captcha = this.dbcClient.upload(fileName);
            if (null != captcha) {
                System.out.println("CAPTCHA " + this.captchaFilename + " uploaded: " + captcha.id);

                // Poll for the uploaded CAPTCHA status.
                int sec = 0;
                while (captcha.isUploaded() && !captcha.isSolved()) {
                    Thread.sleep(Client.POLLS_INTERVAL * 1000);
                    captcha = this.dbcClient.getCaptcha(captcha);
                    sec += 5;
                    System.out.println(sec + "sec elapsed");
                }

                if (captcha.isSolved()) {
                    System.out.println("CAPTCHA " + this.captchaFilename + " solved: " + captcha.text);
                    return captcha.text;
                } else {
                    System.out.println("Failed solving CAPTCHA");
                    LoggerUtil.logError("Failed solving CAPTCHA");
                }
            }
        } catch (java.lang.Exception e) {
        	LoggerUtil.logError(e.getMessage());
            System.err.println(e.toString());
        }
		
		return null;
	}
	
	public void reportCaptchaAsIncorrect() {
		// Report incorrectly solved CAPTCHA if neccessary.
        // Make sure you've checked if the CAPTCHA was in fact
        // incorrectly solved, or else you might get banned as
        // abuser.
        try {
			if (dbcClient.report(captcha)) {
			    System.out.println("CAPTCHA " + this.captchaFilename + " reported as incorrectly solved");
			} else {
			    System.out.println("Failed reporting incorrectly solved CAPTCHA");
			    LoggerUtil.logError("Failed reporting incorrectly solved CAPTCHA");
			}
		}  catch (java.lang.Exception e) {
			LoggerUtil.logError(e.getMessage());
			e.printStackTrace();
		} 
	}
}
