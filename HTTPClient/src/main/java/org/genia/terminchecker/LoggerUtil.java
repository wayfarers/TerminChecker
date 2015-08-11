package org.genia.terminchecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LoggerUtil {
	public static void logError(String error) {
		DateFormat df = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		TimeZone zone = TimeZone.getTimeZone("Europe/Kiev");
		df.setTimeZone(zone);
		String message = df.format(new Date()) + ": " +  error + "\n";
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
