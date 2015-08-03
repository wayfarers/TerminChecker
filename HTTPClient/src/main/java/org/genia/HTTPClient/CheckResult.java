package org.genia.HTTPClient;

import java.util.List;

public class CheckResult {
	Status status;
	public String errorMessage;
	public List<String> appointments;
	
	public static CheckResult error(String message) {
		CheckResult result = new CheckResult();
		result.status = Status.OTHER_ERROR;
		result.errorMessage = message;
		
		return result;
	}
}
