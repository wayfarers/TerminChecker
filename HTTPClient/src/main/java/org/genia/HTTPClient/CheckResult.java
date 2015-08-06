package org.genia.HTTPClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Status status;
	public String errorMessage;
	public List<String> appointments = new ArrayList<>();
	
	public static CheckResult error(String message) {
		CheckResult result = new CheckResult();
		result.status = Status.OTHER_ERROR;
		result.errorMessage = message;
		Logger.logError(message);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		CheckResult c = null;
		if (obj instanceof CheckResult) {
			c = (CheckResult) obj;
		} else {
			return false;
		}
		if (this.status == c.status && appointments.equals(c.appointments)) {
			return true;
		}
		
		return false;
	}
	
	public void saveOnDisk() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream("result.dat"));
			out.writeObject(this);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static CheckResult restoreLastResult() {
		try (ObjectInputStream in = new ObjectInputStream(
				new FileInputStream("result.dat"));) {
			return (CheckResult) in.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("No previous result file was found");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}
}
