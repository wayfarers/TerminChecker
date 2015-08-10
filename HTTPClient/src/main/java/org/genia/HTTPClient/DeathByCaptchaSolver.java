package org.genia.HTTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.SocketClient;

public class DeathByCaptchaSolver extends CaptchaSolver {
	
	protected Client client;
    protected String captchaFilename = null;
    String userName;
    String password;
    Captcha captcha = null;
    
    public DeathByCaptchaSolver() {
    	Properties props = new Properties();
		try {
			props.load(new FileInputStream("credentials.properties"));
		} catch (FileNotFoundException e) {
			Logger.logError("DBC credentials not found.");
			e.printStackTrace();
		} catch (IOException e) {
			Logger.logError("Error while loading DBC credentials. " + e.getMessage());
			e.printStackTrace();
		}
		userName = props.getProperty("username");
		password = props.getProperty("password");
		
		client = (Client)(new SocketClient(userName, password));
	}	
    
	@Override
	public String solveCaptcha(String fileName) {
		client.isVerbose = true;
		captchaFilename = fileName;
		captcha = null;
		
		try {
            // Put your CAPTCHA image file, file object, input stream,
            // or vector of bytes here:
            captcha = this.client.upload(fileName);
            if (null != captcha) {
                System.out.println("CAPTCHA " + this.captchaFilename + " uploaded: " + captcha.id);

                // Poll for the uploaded CAPTCHA status.
                int sec = 0;
                while (captcha.isUploaded() && !captcha.isSolved()) {
                    Thread.sleep(Client.POLLS_INTERVAL * 1000);
                    captcha = this.client.getCaptcha(captcha);
                    sec += 5;
                    System.out.println(sec + "sec elapsed");
                }

                if (captcha.isSolved()) {
                    System.out.println("CAPTCHA " + this.captchaFilename + " solved: " + captcha.text);
                    return captcha.text;
                } else {
                    System.out.println("Failed solving CAPTCHA");
                    Logger.logError("Failed solving CAPTCHA");
                }
            }
        } catch (java.lang.Exception e) {
        	Logger.logError(e.getMessage());
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
			if (client.report(captcha)) {
			    System.out.println("CAPTCHA " + this.captchaFilename + " reported as incorrectly solved");
			} else {
			    System.out.println("Failed reporting incorrectly solved CAPTCHA");
			    Logger.logError("Failed reporting incorrectly solved CAPTCHA");
			}
		}  catch (java.lang.Exception e) {
			Logger.logError(e.getMessage());
			e.printStackTrace();
		} finally {
			File img = new File(captchaFilename);
			File wrongCaptchas = new File("wrongCaptchas");
			wrongCaptchas.mkdir();
			img.renameTo(new File(wrongCaptchas.getAbsolutePath() + "/" + captcha.text + ".jpg"));
		}
	}
}
