package org.edu.alb.programming.sms;

import java.io.IOException;
import java.util.Scanner;

import org.edu.alb.programming.sms.threads.TestConcatenatedSMSThread;
import org.edu.alb.programming.sms.threads.TestIndividualSMSThread;
import org.edu.alb.programming.sms.threads.TestMessagePayloadThread;
import org.edu.alb.programming.sms.util.Util;

public class TestTransmitter {

	private static TestTransmitter instance = null;
	private static boolean willQuit = false;

	private TestTransmitter() {
	}

	public static TestTransmitter getInstance() {
		if (instance == null) {
			instance = new TestTransmitter();
		}

		return instance;
	}

	public static void main(String[] args) {
		TestTransmitter.getInstance().showWelcome();
		TestTransmitter.getInstance().showInstructions();
		TestTransmitter.getInstance().pauseProgram();
		TestTransmitter.getInstance().scanCommand();
	}

	private void pauseProgram() {
		System.out.println("Press Enter key to continue");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showWelcome() {
		System.out.println("Welcome!");
	}

	private void showInstructions() {
		System.out.println();
		System.out.println("Instructions: Please turn on SMPPSim and SMSRecipientSimulator. ");
		System.out.println("Press 1: send an individual SMS");
		System.out.println("Press 2: send an SMS through message_payload");
		System.out.println("Press 3: send a concatenated SMS");
		System.out.println("command quit or exit to quit the program");
		System.out.println();
	}

	private void scanCommand() {
		Scanner commandScanner = new Scanner(System.in);
		String command = "";
		while (!quitNow()) {
			if (!willQuit) {
				command = commandScanner.nextLine();

				switch (command) {
				case "quit":
				case "exit":
					willQuit = true;
					System.out.println("Program is sutting down. Please wait ...");
					break;
				case "welcome":
					showWelcome();
				case "help":
					showInstructions();
					break;
				case "1":
					new Thread(new TestIndividualSMSThread()).start();
					break;
				case "2":
					new Thread(new TestConcatenatedSMSThread()).start();
					break;
				case "3":
					new Thread(new TestMessagePayloadThread()).start();
					break;
				case "":
					break;
				default:
					System.out.println("Incorrect command");
					showInstructions();
					break;
				}
			}
			Util.sleep(1);
		}

		if (commandScanner != null) {
			commandScanner.close();
			commandScanner = null;
		}
	}

	private boolean quitNow() {
		return willQuit;
	}

	public boolean willQuit() {
		return willQuit;
	}
}
