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

package org.bluewindows.figures.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.impl.ofx.ImportOFXDaoImpl;
import org.bluewindows.figures.dao.impl.qif.ImportQIFDaoImpl;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.service.ImportService;

public class ImportServiceImpl implements ImportService {

	private static ImportServiceImpl instance = new ImportServiceImpl();
	private ImportOFXDaoImpl ofxImporter = new ImportOFXDaoImpl();
	private ImportQIFDaoImpl qifImporter = new ImportQIFDaoImpl();
	
	private ImportServiceImpl(){
	}
	
	public static ImportServiceImpl getInstance(){
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CallResult importFile(File file, Account account) {
		CallResult result = new CallResult();
		try {
			if (isQifFile(file)) {
				result = qifImporter.importFile(file, account.getType());
			}else if (isOfxFile(file)){
				result = ofxImporter.importFile(file, account.getType());
			}else {
				return result.setCallBad("File Import Failure", "Not a valid QIF or OFX file");
			}
		} catch (IOException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("File Import Failure", e.getLocalizedMessage());
		}
		if (result.isBad()) return result;
		account.setImportFolder(file.getParent());
		List<Transaction> transactions = (List<Transaction>)result.getReturnedObject();
		if (transactions.size() > 0) sortTransactions(transactions);
		return result;
	}
	
	private boolean isQifFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		char curChar = (char)fis.read();
		while(curChar == ' ' || curChar == '\n' || curChar == '\r') {
			curChar = (char)fis.read();
		}
		fis.close();
		return curChar == '!';
	}
	
	private boolean isOfxFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		char curChar = (char)fis.read();
		while(curChar == ' ' || curChar == '\n' || curChar == '\r') {
			curChar = (char)fis.read();
		}
		StringBuilder sb = new StringBuilder();
		sb.append((char)curChar);
		for (int i = 0; i < 8; i++) {
			sb.append((char)fis.read());
		}
		fis.close();
		return sb.toString().equals("OFXHEADER");
	}
	
	private void sortTransactions(List<Transaction> transactions) {
		boolean orderIsAscending = (transactions.get(0).getDate().compareTo(transactions.get(transactions.size()-1).getDate()) < 0);
		if (orderIsAscending) {
			if (ascendingOrderFound(transactions)) {
				return;
			}else {
				sortAscendingTransactions(transactions);
			}
		}else {
			sortDescendingTransactions(transactions);
		}
	}

	private boolean ascendingOrderFound(List<Transaction> transactions) {
		for (int i = 0; i < transactions.size()-2; i++) {
			if (transactions.get(i).getDate().compareTo(transactions.get(i+1).getDate()) > 0){
				return false;
			}
		}
		return true;	
	}
	
	// Sort the transactions into ascending order by date, preserving the transaction order within date
	private void sortAscendingTransactions(List<Transaction> transactions) {
		Map<TransactionDate, Integer> dateMap = new HashMap<TransactionDate, Integer>();
		for (Transaction transaction : transactions) {
			Integer transactionId = dateMap.get(transaction.getDate());
			if (transactionId == null) {
				transaction.setID(1);
				dateMap.put(transaction.getDate(), Integer.valueOf(1));
			}else {
				transaction.setID(transactionId.intValue()+1);
				dateMap.put(transaction.getDate(), Integer.valueOf(transactionId.intValue()+1));
			}
		}
		Collections.sort(transactions);
	}
	
	// Sort the currently descending transactions into ascending order by date, preserving the transaction order within date
	private void sortDescendingTransactions(List<Transaction> transactions) {
		Map<TransactionDate, Integer> dateMap = new HashMap<TransactionDate, Integer>();
		for (Transaction transaction : transactions) {
			Integer transactionId = dateMap.get(transaction.getDate());
			if (transactionId == null) {
				transaction.setID(99999);
				dateMap.put(transaction.getDate(), Integer.valueOf(99999));
			}else {
				transaction.setID(transactionId.intValue()-1);
				dateMap.put(transaction.getDate(), Integer.valueOf(transactionId.intValue()+1));
			}
		}
		Collections.sort(transactions);
	}

}
