package org.genia.terminchecker;

import java.io.IOException;
import java.util.Scanner;

public class HumanInputSolver extends CaptchaSolver {
	@Override
	public String solveCaptcha(String fileName) {
		try {
			@SuppressWarnings("unused")
			Process process = new ProcessBuilder("cmd","/c", "D:/capcha.jpg").start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Enter capcha text
		System.out.println("Please, enter text from capcha image:");
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String capchaText = sc.nextLine();
		
		return capchaText;
	}
}
