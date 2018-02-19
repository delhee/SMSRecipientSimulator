package org.edu.alb.programming.sms.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.edu.alb.programming.sms.SMSRecipientSimulator;
import org.edu.alb.programming.sms.datastruct.SMSUnit;
import org.edu.alb.programming.sms.util.Util;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.TimeoutException;
import org.smpp.WrongSessionStateException;
import org.smpp.pdu.BindReceiver;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.PDU;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.ValueNotSetException;

public class SMPPReceiverThread implements Runnable {
	private ArrayList<SMSUnit> smsListToProcess = null;
	private ArrayList<SMSUnit> smsListToDisplay = null;
	private boolean threadShouldEnd = true;
	private boolean threadEnds = true;

	private Logger logger = Logger.getLogger(SMPPReceiverThread.class);
	private Session session = null;
	private String recipientConfigFile = null;
	private String ipAddress = "127.0.0.1";
	private String systemId = "smppclient1";
	private String password = "password";
	private int port = 2775;
	private String phoneNumber = "12345678";

	public SMPPReceiverThread(ArrayList<SMSUnit> smsListToProcess, ArrayList<SMSUnit> smsListToDisplay, String recipientConfigFile) {
		logger.info("SMPPReceiverThread constructor...");
		this.smsListToProcess = smsListToProcess;
		this.smsListToDisplay = smsListToDisplay;
		this.recipientConfigFile = recipientConfigFile;
	}

	private void loadProperties(String recipientConfigFile) {
		File objFile = new File(recipientConfigFile);
		if (objFile.exists()) {
			try {
				InputStream objInputStream = new FileInputStream(objFile);
				Properties properties = new Properties();
				properties.load(objInputStream);
				this.ipAddress = properties.getProperty("SMSC_IP_ADDRESS");
				this.port = Integer.parseInt(properties.getProperty("SMPP_PORT"));
				this.systemId = properties.getProperty("SYSTEM_ID");
				this.password = properties.getProperty("PASSWORD");
				this.phoneNumber = properties.getProperty("PHONE_NUMBER");
				objInputStream.close();
			} catch (FileNotFoundException e) {
				this.logger.error(Util.stackTraceToString(e));
			} catch (IOException e) {
				this.logger.error(Util.stackTraceToString(e));
			}
		}
	}

	public boolean threadEnds() {
		return this.threadEnds;
	}

	@Override
	public void run() {
		logger.info("SMPPReceiverThread starts running...");
		this.threadShouldEnd = false;
		this.threadEnds = false;
		loadProperties(recipientConfigFile);
		if (bindToSmsc()) {
			while (!this.threadShouldEnd) {
				if (SMSRecipientSimulator.getInstance().willQuit()) {
					this.threadShouldEnd = true;
				} else {
					receivePDU();
					Util.sleep(2);
				}
			}

			if (unbind()) {
				logger.info("Unbind successful...");
				this.threadEnds = true;
			} else {
				logger.info("Retrying unbind...");
				if (unbind()) {
					logger.info("Unbind successful...");
				} else {
					logger.info("Unbind not successful...");
				}
				this.threadEnds = true;
			}
		} else {
			this.threadEnds = true;
		}
		Util.sleep(1);
		logger.info("SMPPReceiverThread ends...");
	}

	private synchronized boolean bindToSmsc() {

		boolean result = false;
		try {
			// setup connection
			TCPIPConnection connection = new TCPIPConnection(ipAddress, port);
			connection.setReceiveTimeout(2000);
			this.session = new Session(connection);

			// set request parameters
			BindRequest request = new BindReceiver();
			request.setSystemId(systemId);
			request.setPassword(password);

			// send request to bind
			BindResponse response = session.bind(request);
			if (response.getCommandStatus() == Data.ESME_ROK) {
				logger.info("SMPP Receiver is connected to SMSC.");
				result = true;
			}
		} catch (Exception e) {
			this.logger.error(Util.stackTraceToString(e));
			this.threadShouldEnd = true;
			this.threadEnds = true;
		}

		return result;
	}

	private void receivePDU() {
		if (session != null) {
			try {
				PDU pdu = session.receive(1500);

				if (pdu != null) {
					DeliverSM sms = (DeliverSM) pdu;

					if (sms.getDestAddr().getAddress().equals(this.phoneNumber)) {
						SMSUnit smsUnit = new SMSUnit();

						smsUnit.setSrcAddr(sms.getSourceAddr().getAddress());
						smsUnit.setDestAddr(sms.getDestAddr().getAddress());
						smsUnit.setDataCoding(sms.getDataCoding());
						smsUnit.setEsmClass(sms.getEsmClass());

						if (sms.getShortMessageData().length() == 0) {
							smsUnit.setShortMessageData(sms.getMessagePayload().getBuffer());
							this.smsListToDisplay.add(smsUnit);
						} else {
							smsUnit.setShortMessageData(sms.getShortMessageData().getBuffer());
							if (sms.getEsmClass() == 0) {
								this.smsListToDisplay.add(smsUnit);
							} else if (sms.getEsmClass() <= 127 && sms.getEsmClass() <= 64) {
								byte[] smsPart = sms.getShortMessageData().getBuffer();

								if (smsPart.length >= 6) {
									smsUnit.setRefNum((int) smsPart[3]);
									smsUnit.setTotalParts((int) smsPart[4]);
									smsUnit.setPartNumber((int) smsPart[5]);

									this.smsListToProcess.add(smsUnit);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				this.logger.error(Util.stackTraceToString(e));
			}
		}
	}

	private synchronized boolean unbind() {
		boolean result = false;
		try {
			session.unbind();
			result = true;
		} catch (ValueNotSetException e) {
			this.logger.error(Util.stackTraceToString(e));
		} catch (TimeoutException e) {
			this.logger.error(Util.stackTraceToString(e));
		} catch (PDUException e) {
			this.logger.error(Util.stackTraceToString(e));
		} catch (WrongSessionStateException e) {
			this.logger.error(Util.stackTraceToString(e));
		} catch (IOException e) {
			this.logger.error(Util.stackTraceToString(e));
		} finally {
			this.threadShouldEnd = true;
			this.threadEnds = true;
		}
		return result;
	}
}
