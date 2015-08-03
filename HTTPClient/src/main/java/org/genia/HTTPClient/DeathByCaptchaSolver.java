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
			e.printStackTrace();
		} catch (IOException e) {
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
		
		try {
            // Put your CAPTCHA image file, file object, input stream,
            // or vector of bytes here:
            captcha = this.client.upload(fileName);
            if (null != captcha) {
                System.out.println("CAPTCHA " + this.captchaFilename + " uploaded: " + captcha.id);

                // Poll for the uploaded CAPTCHA status.
                while (captcha.isUploaded() && !captcha.isSolved()) {
                    Thread.sleep(Client.POLLS_INTERVAL * 1000);
                    captcha = this.client.getCaptcha(captcha);
                    System.out.println("5s");
                }

                if (captcha.isSolved()) {
                    System.out.println("CAPTCHA " + this.captchaFilename + " solved: " + captcha.text);

                    // Report incorrectly solved CAPTCHA if neccessary.
                    // Make sure you've checked if the CAPTCHA was in fact
                    // incorrectly solved, or else you might get banned as
                    // abuser.
                    /*if (this._client.report(captcha)) {
                        System.out.println("CAPTCHA " + this._captchaFilename + " reported as incorrectly solved");
                    } else {
                        System.out.println("Failed reporting incorrectly solved CAPTCHA");
                    }*/
                } else {
                    System.out.println("Failed solving CAPTCHA");
                }
            }
        } catch (java.lang.Exception e) {
            System.err.println(e.toString());
        }
		
		return captcha.text;
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
			}
		}  catch (java.lang.Exception e) {
			e.printStackTrace();
		} finally {
			File img = new File(captchaFilename);
			File wrongCaptchas = new File(img.getParent() + "wrongCaptchas\\");
			wrongCaptchas.mkdir();
			img.renameTo(new File(wrongCaptchas.getAbsolutePath() + "\\" + captcha.text + ".jpg"));
		}
	}
}
