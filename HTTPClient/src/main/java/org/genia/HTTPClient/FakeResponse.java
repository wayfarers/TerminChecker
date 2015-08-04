package org.genia.HTTPClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FakeResponse {
	public static String getFakeResponse() {
		try {
			FileReader reader = new FileReader(new File("D:\\fake_termin.htm"));
			char[] buf = new char[1024];
			String response = "";
			
			while (reader.read(buf) != -1) {
				response += String.valueOf(buf);
				buf = new char[1024];
			}
			reader.close();
			return response;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
