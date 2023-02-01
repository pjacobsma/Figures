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

package org.bluewindows.figures.dao.impl.ofx;

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
import org.bluewindows.ofx.OFXAccountType;
import org.bluewindows.ofx.OFXBankMsgResponse;
import org.bluewindows.ofx.OFXBankStatement;
import org.bluewindows.ofx.OFXBankStatementResponse;
import org.bluewindows.ofx.OFXStatementTrans;
import org.bluewindows.ofx.OFXBankTransList;
import org.bluewindows.ofx.OFXCallResult;
import org.bluewindows.ofx.OFXCardMsgResponse;
import org.bluewindows.ofx.OFXCardStatement;
import org.bluewindows.ofx.OFXCardStatementResponse;
import org.bluewindows.ofx.OFXContext;
import org.bluewindows.ofx.OFXImporter;

public class ImportOFXDaoImpl implements ImportDao {

	@Override
	public CallResult importFile(File file, AccountType accountType) {
		CallResult result = new CallResult();
		OFXCallResult ofxResult = new OFXCallResult();
		OFXImporter importer = new OFXImporter();
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return result.setCallBad("File Error", "File not found.");
		}
		ofxResult = importer.importOFX(fis);
		if (ofxResult.isCallOK()){
			OFXContext context = ofxResult.getContext();
			OFXBankMsgResponse bankMsgResponse = context.getBankMsgResponse();
			ArrayList<OFXBankStatementResponse> bankStatementResponses = bankMsgResponse.getBankStatementResponses();
			OFXCardMsgResponse cardMsgResponse = context.getCardMsgResponse();
			ArrayList<OFXCardStatementResponse> cardStatementResponses = cardMsgResponse.getCardStatementResponses();
			if (!bankStatementResponses.isEmpty()) {
				result = processBankStatements(accountType, bankStatementResponses);
			}else if (!cardStatementResponses.isEmpty()) {
				if (!accountType.getText().equals("Credit Card")) {
					result.setCallBad("Wrong Account Type", 
						"You are importing into a " + accountType.getText().toUpperCase() + " account but this file is from a CREDIT CARD account.");
					return result;
				}
				result = processCardStatements(accountType, cardStatementResponses);
			}else {
				result.setCallBad("Invalid OFX File", "File does not contain bank or credit card statements");
			}
		}else{
			result.setCallBad("Invalid OFX File", ofxResult.getException().getMessage());
		}
		return result;
	}

	private CallResult processBankStatements(AccountType accountType, ArrayList<OFXBankStatementResponse> bankStatementResponses) {
		CallResult result = new CallResult();
		List<Transaction> tranList = new ArrayList<Transaction>();
		for (OFXBankStatementResponse response : bankStatementResponses) {
			ArrayList<OFXBankStatement> statements = response.getStatements();
			for (OFXBankStatement statement : statements) {
				OFXAccountType ofxAccountType = statement.getBankAcctFrom().getAccountType();
				if (!ofxAccountType.toString().toUpperCase().equals(accountType.getText().toUpperCase())) {
					result.setCallBad("Wrong Account Type", 
						"You are importing into a " + accountType.getText().toUpperCase() + " account but this file is from a " + ofxAccountType.toString() + " account.");
					return result;
				}
				OFXBankTransList bankTransList = statement.getBankTransList();
				ArrayList<OFXStatementTrans> transactions = bankTransList.getTransactions();
				for (OFXStatementTrans ofxTransaction : transactions) {
					tranList.add(convertOFXTranToFiguresTran(ofxTransaction));
				}
			}
		}
		return result.setReturnedObject(tranList);
	}
	
	private CallResult processCardStatements(AccountType accountType, ArrayList<OFXCardStatementResponse> cardStatementResponses) {
		CallResult result = new CallResult();
		List<Transaction> tranList = new ArrayList<Transaction>();
		for (OFXCardStatementResponse response : cardStatementResponses) {
			ArrayList<OFXCardStatement> statements = response.getStatements();
			for (OFXCardStatement statement : statements) {
				OFXBankTransList bankTransList = statement.getBankTransList();
				ArrayList<OFXStatementTrans> transactions = bankTransList.getTransactions();
				for (OFXStatementTrans ofxTransaction : transactions) {
					tranList.add(convertOFXTranToFiguresTran(ofxTransaction));
				}
			}
		}
		return result.setReturnedObject(tranList);
	}
	
	private Transaction convertOFXTranToFiguresTran(OFXStatementTrans ofxTrans){
		Transaction trans = new Transaction();
		if (ofxTrans.getCheckNum() != null){
			trans.setNumber(ofxTrans.getCheckNum());
		}
		trans.setDate(new TransactionDate(ofxTrans.getDatePosted().toLocalDate()));
		trans.setAmount(new Money(ofxTrans.getAmount()));
		trans.setDescription(ofxTrans.getName());
		trans.setOriginalDescription(ofxTrans.getName());
		trans.setMemo(ofxTrans.getMemo());
		trans.setOriginalMemo(ofxTrans.getMemo());
		trans.setBalance(new Money("0"));
		return trans;
	}

}
