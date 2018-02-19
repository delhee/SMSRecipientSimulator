package org.edu.alb.programming.sms.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.smpp.Connection;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.TimeoutException;
import org.smpp.WrongSessionStateException;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.BindTransmitterResp;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.ValueNotSetException;
import org.smpp.util.ByteBuffer;

import com.cloudhopper.commons.gsm.GsmUtil;

public class TestConcatenatedSMSThread implements Runnable{

	/**
	 * @param args
	 */
	private Session session = null;
	private String ipAddress = "127.0.0.1";
	private String systemId = "smppclient1";
	private String password = "password";
	private int port = 2775;
	private String shortMessage = "There is no positive side to credit card use. Even if you pay the bill on time, you are not beating the system. The reality is: The average family today carries a balance of $16,048 in credit card debt.";
	private String sourceAddress = "1234";
	private String destinationAddress = "12345678900";

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
				objInputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void bindToSMSC() {
		try {
			Connection conn = new TCPIPConnection(this.ipAddress, this.port);
			this.session = new Session(conn);

			BindRequest breq = new BindTransmitter();
			breq.setSystemId(this.systemId);
			breq.setPassword(this.password);
			BindTransmitterResp bresp = (BindTransmitterResp) this.session.bind(breq);

			if (bresp.getCommandStatus() == Data.ESME_ROK) {
				System.out.println("Connected to SMSC");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendSingleSMS() {
		try {
			int csmsRefNum = (new Random()).nextInt(0xFF);
			byte[][] messageParts = GsmUtil.createConcatenatedBinaryShortMessages(this.shortMessage.getBytes(), (byte) csmsRefNum);

			for (int i = 0; i < messageParts.length; i++) {
				SubmitSM request = new SubmitSM();
				request.setSourceAddr(this.sourceAddress);
				request.setDestAddr(this.destinationAddress);
				request.setEsmClass((byte) 64);
				request.setShortMessageData(new ByteBuffer(messageParts[i]));

				SubmitSMResp resp = this.session.submit(request);

				if (resp.getCommandStatus() == Data.ESME_ROK) {
					System.out.println("Message part [" + i + "]submitted....");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to submit message....");
		}
	}
	
	public void quit() {
		try {
			this.session.unbind();
		} catch (ValueNotSetException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (PDUException e) {
			e.printStackTrace();
		} catch (WrongSessionStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Test terminated");
			System.out.println("Please test next case or quit. ");
			System.out.println("For detailed instraction, please execute command: help ");
		}
	}

	@Override
	public void run() {
		loadProperties("configs/transmitterTest.props");
		bindToSMSC();
		sendSingleSMS();
		quit();
	}
}
