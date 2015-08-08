package org.genia.HTTPClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Logger {
	public static void logError(String error) {
		Date currentDate = Calendar.getInstance(TimeZone.getTimeZone("Kiev")).getTime();
		String message = new SimpleDateFormat("dd.mm.yy HH:mm:ss").format(currentDate) + ": " +  error + "\n";
		File logFile = new File("log.txt");
		try {
			logFile.createNewFile();
			FileWriter writer = new FileWriter(logFile, true);
			writer.append(message);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
