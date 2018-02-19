package org.edu.alb.programming.sms.datastruct;

public class SMSUnit {
	private String srcAddr = "";
	private String destAddr = "";
	private String strShortMessage = "";
	private byte[] shortMessageData = null;
	private int dataCoding = 0;
	private int esmClass = 0;
	private int refNum = -1;
	private int totalParts = -1;
	private int partNumber = -1;

	public String getSrcAddr() {
		return srcAddr;
	}

	public void setSrcAddr(String srcAddr) {
		this.srcAddr = srcAddr;
	}

	public String getDestAddr() {
		return destAddr;
	}

	public void setDestAddr(String destAddr) {
		this.destAddr = destAddr;
	}

	public String getStrShortMessage() {
		return strShortMessage;
	}

	public void setStrShortMessage(String strShortMessage) {
		this.strShortMessage = strShortMessage;
	}

	public byte[] getShortMessageData() {
		return shortMessageData;
	}

	public void setShortMessageData(byte[] shortMessageData) {
		this.shortMessageData = shortMessageData;
	}

	public int getDataCoding() {
		return dataCoding;
	}

	public void setDataCoding(int dataCoding) {
		this.dataCoding = dataCoding;
	}

	public int getEsmClass() {
		return esmClass;
	}

	public void setEsmClass(int esmClass) {
		this.esmClass = esmClass;
	}

	public int getRefNum() {
		return refNum;
	}

	public void setRefNum(int refNum) {
		this.refNum = refNum;
	}

	public int getTotalParts() {
		return totalParts;
	}

	public void setTotalParts(int totalParts) {
		this.totalParts = totalParts;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

}
