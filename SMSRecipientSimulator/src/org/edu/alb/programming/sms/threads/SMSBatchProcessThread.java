package org.edu.alb.programming.sms.threads;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.edu.alb.programming.sms.SMSRecipientSimulator;
import org.edu.alb.programming.sms.datastruct.SMSUnit;
import org.edu.alb.programming.sms.datastruct.comparators.SMSPartComparator;
import org.edu.alb.programming.sms.util.Util;

public class SMSBatchProcessThread implements Runnable {

	private static final int MAX_UNIT_TO_PROCESS = 50;

	private Logger logger = Logger.getLogger(SMSBatchProcessThread.class);
	private ArrayList<SMSUnit> smsListToProcess = null;
	private ArrayList<SMSUnit> smsListToDisplay = null;
	private boolean threadShouldEnd = false;
	private boolean threadEnds = false;

	public SMSBatchProcessThread(ArrayList<SMSUnit> smsListToProcess, ArrayList<SMSUnit> smsListToDisplay) {
		logger.info("SMSBatchProcessThread constructor...");
		this.smsListToProcess = smsListToProcess;
		this.smsListToDisplay = smsListToDisplay;
	}

	public boolean threadEnds() {
		return this.threadEnds;
	}

	@Override
	public synchronized void run() {
		logger.info("SMSBatchProcessThread starts running...");
		while (!this.threadShouldEnd) {
			Util.sleep(1);
			if (SMSRecipientSimulator.getInstance().willQuit()) {
				this.threadShouldEnd = true;
			}
			Collections.shuffle(this.smsListToProcess);
			Util.sleep(10);
			ArrayList<SMSUnit> pickedSMSList = pickSMSUnitsToProcess();
			Collections.sort(pickedSMSList, new SMSPartComparator());

			while (pickedSMSList.size() > 0 && pickedSMSList.size() >= pickedSMSList.get(0).getTotalParts()) {
				SMSUnit combinedSMS = new SMSUnit();
				combinedSMS.setSrcAddr(pickedSMSList.get(0).getSrcAddr());
				combinedSMS.setDestAddr(pickedSMSList.get(0).getDestAddr());
				int countAlreadyCombinedParts = 0;
				for (int i = 0; i < pickedSMSList.get(0).getTotalParts(); i++) {
					if (pickedSMSList.get(i).getRefNum() == pickedSMSList.get(0).getRefNum()) {
						byte[] smsDataWithoutUDH = Arrays.copyOfRange(pickedSMSList.get(i).getShortMessageData(), 6, pickedSMSList.get(i).getShortMessageData().length);
						if (pickedSMSList.get(i).getDataCoding() == 0) {
							combinedSMS.setDataCoding(0);
							combinedSMS.setStrShortMessage(combinedSMS.getStrShortMessage().concat(new String(smsDataWithoutUDH)));
						} else if (pickedSMSList.get(i).getDataCoding() == 8) {
							try {
								combinedSMS.setDataCoding(8);
								combinedSMS.setStrShortMessage(combinedSMS.getStrShortMessage().concat(new String(smsDataWithoutUDH, "UTF-16")));
							} catch (UnsupportedEncodingException e) {
								this.logger.error(Util.stackTraceToString(e));
							}
						}
						countAlreadyCombinedParts++;
					}
				}

				if (countAlreadyCombinedParts == pickedSMSList.get(0).getTotalParts()) {
					for (int i = 0; i < countAlreadyCombinedParts; i++) {
						pickedSMSList.remove(0);
					}
				}
				this.smsListToDisplay.add(combinedSMS);
			}
			this.smsListToProcess.addAll(0, pickedSMSList);
		}
		this.threadEnds = true;
		logger.info("SMSBatchProcessThread ends...");
	}

	private ArrayList<SMSUnit> pickSMSUnitsToProcess() {
		int pickedSMSListSize = (this.smsListToProcess.size() < MAX_UNIT_TO_PROCESS ? this.smsListToProcess.size() : MAX_UNIT_TO_PROCESS);
		ArrayList<SMSUnit> pickedSMSList = new ArrayList<SMSUnit>();

		for (int i = 0; i < pickedSMSListSize; i++) {
			pickedSMSList.add(this.smsListToProcess.get(0));

			this.smsListToProcess.remove(0);
		}

		return pickedSMSList;
	}
}
