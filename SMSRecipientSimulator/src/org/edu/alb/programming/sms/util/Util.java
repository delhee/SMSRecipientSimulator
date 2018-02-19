package org.edu.alb.programming.sms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {

	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String readStringInFile(String strFilePath) throws Exception {
		String strStringInFile = "";
		File objFile = new File(strFilePath);
		if (objFile.exists()) {
			try {
				FileReader objFileReader = new FileReader(objFile);
				BufferedReader objBufferedReader = new BufferedReader(objFileReader);
				String strLineInFile = objBufferedReader.readLine();
				while (strLineInFile != null) {
					strStringInFile = strStringInFile + strLineInFile + "\n";
					strLineInFile = objBufferedReader.readLine();
				}
				objBufferedReader.close();
			} catch (FileNotFoundException e) {
				strStringInFile = "";
				throw e;
			} catch (IOException e) {
				strStringInFile = "";
				throw e;
			}
		}

		return strStringInFile;
	}

	public static String stackTraceToString(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);

		return stringWriter.toString();
	}
}
