/**
 * Copyright 2010 Phil Jacobsma
 * 
 * This file is part of Figures.
 *
 * Figures is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Figures is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Figures; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.bluewindows.figures.dao.impl.qif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.dao.ImportDao;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.qif.QIFAccountType;
import org.bluewindows.qif.QIFCallResult;
import org.bluewindows.qif.QIFImporter;
import org.bluewindows.qif.QIFPackage;
import org.bluewindows.qif.QIFRecord;

public class ImportQIFDaoImpl implements ImportDao{

	@Override
	public CallResult importFile(File file, AccountType accountType) {
		CallResult result = new CallResult();
		List<Transaction> tranList = new ArrayList<Transaction>();
		QIFImporter importer = new QIFImporter();
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return result.setCallBad("File Error", "File not found.");
		}
		QIFCallResult qifResult = importer.importQIF(fis);
		if (qifResult.isCallOK()){
			QIFPackage qifPackage = (QIFPackage)qifResult.getReturnedObject();
			if (accountType.equals(AccountType.CHECKING) || accountType.equals(AccountType.SAVINGS)) {
				if (!qifPackage.getAccountType().equals(QIFAccountType.Bank)) {
					result.setCallBad("Wrong Account Type", 
						"You are importing into a " + accountType.getText().toUpperCase() + " account but this file is from a " +
						qifPackage.getAccountType().getLabel().toUpperCase() + " account.");
				}
			}else if (accountType.equals(AccountType.CREDIT_CARD)){
				if (!qifPackage.getAccountType().equals(QIFAccountType.CCard)) {
					result.setCallBad("Wrong Account Type", 
						"You are importing into a " + accountType.getText().toUpperCase() + " account but this file is from a " +
						qifPackage.getAccountType().getLabel().toUpperCase() + " account.");
				}
			}
			List<QIFRecord> qifRecordList = qifPackage.getRecords();
			for (QIFRecord qifRecord : qifRecordList) {
				tranList.add(convertQIFTranToFiguresTran(qifRecord));
			}
			result.setReturnedObject(tranList);
		}else {
			result.setCallBad("Invalid QIF File", qifResult.getException().getMessage());
		}
		return result;
	}
	
	private Transaction convertQIFTranToFiguresTran(QIFRecord qifRec)  {
		
		Transaction trans = new Transaction();
		if (qifRec.getNumber() != null){
			trans.setNumber(qifRec.getNumber());
		}
		trans.setDate(new TransactionDate(qifRec.getDate()));
		trans.setAmount(new Money(qifRec.getAmount()));
		trans.setDescription(qifRec.getDescription());
		trans.setOriginalDescription(qifRec.getDescription());
		trans.setMemo(qifRec.getMemo());
		trans.setOriginalMemo(qifRec.getMemo());
		return trans;
	}

}
