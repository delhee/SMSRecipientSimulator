package org.edu.alb.programming.sms.threads;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.edu.alb.programming.sms.SMSRecipientSimulator;
import org.edu.alb.programming.sms.datastruct.SMSUnit;
import org.edu.alb.programming.sms.util.Util;

public class SMSDisplayThread implements Runnable {

	private Logger logger = Logger.getLogger(SMSDisplayThread.class);
	private ArrayList<SMSUnit> smsListToDisplay = null;
	private boolean threadShouldEnd = false;
	private boolean threadEnds = false;

	public SMSDisplayThread(ArrayList<SMSUnit> smsListToDisplay) {
		logger.info("SMSDisplayThread constructor...");
		this.smsListToDisplay = smsListToDisplay;
	}

	public boolean threadEnds() {
		return this.threadEnds;
	}

	@Override
	public void run() {
		logger.info("SMSDisplayThread starts running...");
		while (!this.threadShouldEnd) {
			if (SMSRecipientSimulator.getInstance().willQuit()) {
				this.threadShouldEnd = true;
			}

			while (this.smsListToDisplay != null && this.smsListToDisplay.size() > 0) {
				logger.info("From: " + this.smsListToDisplay.get(0).getSrcAddr());
				logger.info("To: " + this.smsListToDisplay.get(0).getDestAddr());

				if (smsListToDisplay.get(0).getStrShortMessage() != null && smsListToDisplay.get(0).getStrShortMessage().equals("")) {
					if (this.smsListToDisplay.get(0).getDataCoding() == 0) {
						logger.info("Content: " + new String(this.smsListToDisplay.get(0).getShortMessageData()));
					} else if (this.smsListToDisplay.get(0).getDataCoding() == 8) {
						try {
							logger.info("Content: " + new String(this.smsListToDisplay.get(0).getShortMessageData(), "UTF-16"));
						} catch (UnsupportedEncodingException e) {
							this.logger.error(Util.stackTraceToString(e));
						}
					}
				} else {
					logger.info("Content: " + this.smsListToDisplay.get(0).getStrShortMessage());
				}
				logger.info("Date time: " + (new Date()).toString());
				this.smsListToDisplay.remove(0);
			}
			Util.sleep(1);
		}
		this.threadEnds = true;
		logger.info("SMSDisplayThread ends...");
	}

}
