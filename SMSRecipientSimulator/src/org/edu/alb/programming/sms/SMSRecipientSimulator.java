package org.edu.alb.programming.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.edu.alb.programming.sms.datastruct.SMSUnit;
import org.edu.alb.programming.sms.threads.SMPPReceiverThread;
import org.edu.alb.programming.sms.threads.SMSBatchProcessThread;
import org.edu.alb.programming.sms.threads.SMSDisplayThread;
import org.edu.alb.programming.sms.util.Util;

public class SMSRecipientSimulator {

	private static SMSRecipientSimulator instance = null;
	private static boolean willQuit = false;

	private Logger logger = Logger.getLogger(SMSRecipientSimulator.class);
	private ArrayList<SMSUnit> smsListToProcess = null;
	private ArrayList<SMSUnit> smsListToDisplay = null;
	private SMPPReceiverThread smppReceiverThread = null;
	private SMSBatchProcessThread smsBatchProcessThread = null;
	private SMSDisplayThread smsDisplayThread = null;
	private Thread thread1 = null;
	private Thread thread2 = null;
	private Thread thread3 = null;
	private String recipientConfigFile = null;

	private SMSRecipientSimulator() {
	}

	public static SMSRecipientSimulator getInstance() {
		if (instance == null) {
			instance = new SMSRecipientSimulator();
		}

		return instance;
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("configs/log4j.properties");

		SMSRecipientSimulator.getInstance().logger.info("SMSRecipientSimulator starts ...");
		SMSRecipientSimulator.getInstance().showWelcome();
		SMSRecipientSimulator.getInstance().showInstructions();
		SMSRecipientSimulator.getInstance().pauseProgram();
		SMSRecipientSimulator.getInstance().init(args);
		SMSRecipientSimulator.getInstance().startThreads();
		SMSRecipientSimulator.getInstance().scanCommand();

		SMSRecipientSimulator.getInstance().logger.info("SMSRecipientSimulator will shut down automatically");
		System.exit(1);
	}

	private void showWelcome() {
		logger.info("Showing welcome message...");
		try {
			System.out.println(Util.readStringInFile("resources/welcome.txt"));
		} catch (Exception e) {
			this.logger.error(Util.stackTraceToString(e));
		}
	}

	private void showInstructions() {
		logger.info("Showing instructions...");
		try {
			System.out.println(Util.readStringInFile("resources/instructions.txt"));
		} catch (Exception e) {
			this.logger.error(Util.stackTraceToString(e));
		}
	}

	private void pauseProgram() {
		SMSRecipientSimulator.getInstance().logger.info("Press Enter key to continue");
		try {
			System.in.read();
		} catch (IOException e) {
			this.logger.error(Util.stackTraceToString(e));
		}
	}

	private void init(String[] args) {
		this.smsListToProcess = new ArrayList<SMSUnit>();
		this.smsListToDisplay = new ArrayList<SMSUnit>();
		this.recipientConfigFile = args[0];
		this.smppReceiverThread = new SMPPReceiverThread(smsListToProcess, smsListToDisplay, this.recipientConfigFile);
		this.smsBatchProcessThread = new SMSBatchProcessThread(smsListToProcess, smsListToDisplay);
		this.smsDisplayThread = new SMSDisplayThread(smsListToDisplay);
		this.thread1 = new Thread(this.smppReceiverThread);
		this.thread2 = new Thread(this.smsBatchProcessThread);
		this.thread3 = new Thread(this.smsDisplayThread);
	}

	private void startThreads() {
		this.thread1.start();
		this.thread2.start();
		this.thread3.start();
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
					logger.info("SMSRecipientSimulator is sutting down. Please wait ...");
					break;
				case "help":
					showInstructions();
					break;
				case "welcome":
					showWelcome();
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
		return willQuit && this.smppReceiverThread.threadEnds() && this.smsBatchProcessThread.threadEnds() && this.smsDisplayThread.threadEnds();
	}

	public boolean willQuit() {
		return willQuit;
	}
}
