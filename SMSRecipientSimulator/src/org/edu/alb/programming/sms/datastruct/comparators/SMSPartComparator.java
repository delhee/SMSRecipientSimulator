package org.edu.alb.programming.sms.datastruct.comparators;

import java.util.Comparator;

import org.edu.alb.programming.sms.datastruct.SMSUnit;

public class SMSPartComparator implements Comparator<SMSUnit> {

	@Override
	public int compare(SMSUnit smsUnit1, SMSUnit smsUnit2) {
		String strDestAddr1 = smsUnit1.getDestAddr();
		String strDestAddr2 = smsUnit2.getDestAddr();
		int compareResult1 = strDestAddr1.compareTo(strDestAddr2);

		if (compareResult1 != 0) {
			return compareResult1;
		} else {
			String strSrcAddr1 = smsUnit1.getSrcAddr();
			String strSrcAddr2 = smsUnit2.getSrcAddr();
			int compareResult2 = strSrcAddr1.compareTo(strSrcAddr2);

			if (compareResult2 != 0) {
				return compareResult2;
			} else {
				Integer nRefNum1 = smsUnit1.getRefNum();
				Integer nRefNum2 = smsUnit2.getRefNum();
				int compareResult3 = nRefNum1.compareTo(nRefNum2);

				if (compareResult3 != 0) {
					return compareResult3;
				} else {
					Integer nTotalParts1 = smsUnit1.getTotalParts();
					Integer nTotalParts2 = smsUnit2.getTotalParts();
					int compareResult4 = nTotalParts1.compareTo(nTotalParts2);

					if (compareResult4 != 0) {
						return compareResult4;
					} else {
						Integer nPartNumber1 = smsUnit1.getPartNumber();
						Integer nPartNumber2 = smsUnit2.getPartNumber();
						return nPartNumber1.compareTo(nPartNumber2);
					}
				}
			}
		}
	}

}