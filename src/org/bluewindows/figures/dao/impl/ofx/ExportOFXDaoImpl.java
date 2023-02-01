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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.ExportDao;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.ofx.OFXAccountType;
import org.bluewindows.ofx.OFXBankMsgResponse;
import org.bluewindows.ofx.OFXBankStatement;
import org.bluewindows.ofx.OFXBankStatementResponse;
import org.bluewindows.ofx.OFXBankTransList;
import org.bluewindows.ofx.OFXContext;
import org.bluewindows.ofx.OFXExporter;
import org.bluewindows.ofx.OFXFinancialInstitution;
import org.bluewindows.ofx.OFXSeverityType;
import org.bluewindows.ofx.OFXSignOnMsgResponse;
import org.bluewindows.ofx.OFXSignOnResponse;
import org.bluewindows.ofx.OFXStatementTrans;
import org.bluewindows.ofx.OFXTransType;

public class ExportOFXDaoImpl implements ExportDao {
	
	private OFXExporter exporter = new OFXExporter();

	@Override
	public CallResult exportAccount(Account account, File file) {
		CallResult result = new CallResult();
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Export Failed", e.getLocalizedMessage());
		}
		BufferedWriter bw = new BufferedWriter(fw);
		if (account.getType().equals(AccountType.CREDIT_CARD)) {
			exportCreditCardAccount(account, bw);
		}else {
			exportBankAccount(account, bw);
		}
		try {
			bw.close();
		} catch (IOException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Export Failed.", e.getLocalizedMessage());
		}
		return result;
	}
	
	private CallResult exportBankAccount(Account account, BufferedWriter bw) {
			CallResult result = new CallResult();
			List<Transaction> transactions = new ArrayList<Transaction>(account.getTransactionCount());
			transactions.addAll(account.getTransactions());
			Collections.sort(transactions);
			OFXContext context = new OFXContext();
			OFXSignOnMsgResponse signOnMsgResponse = context.getSignOnMsgResponse();
			OFXSignOnResponse signOnResponse = signOnMsgResponse.getSignOnResponse();
			OFXFinancialInstitution fi = signOnResponse.getFinancialInstitution();
			OFXBankMsgResponse bankMsgResponse = context.getBankMsgResponse();
			OFXBankStatementResponse bankStmtResponse = new OFXBankStatementResponse();
			bankMsgResponse.getBankStatementResponses().add(bankStmtResponse);
			OFXBankStatement statement = new OFXBankStatement();
			bankStmtResponse.addStatement(statement);
			OFXBankTransList ofxTransList = statement.getBankTransList();
			try {
				fi.setFid("1");
				fi.setOrg(account.getName());
				signOnResponse.setServerDate(LocalDateTime.now());
				signOnResponse.getStatus().setCode(BigInteger.ZERO);
				signOnResponse.getStatus().setSeverity(OFXSeverityType.INFO);
				bankStmtResponse.setTransUID("0");
				bankStmtResponse.getStatus().setCode(BigInteger.ZERO);
				bankStmtResponse.getStatus().setSeverity(OFXSeverityType.INFO);
				if (account.getType().equals(AccountType.CHECKING)) {
					statement.getBankAcctFrom().setAccountType(OFXAccountType.CHECKING);
				}else {
					statement.getBankAcctFrom().setAccountType(OFXAccountType.SAVINGS);
				}
				statement.getBankAcctFrom().setBankID("1");
				statement.getBankAcctFrom().setAcctID("1");
				statement.setCurDef(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
				statement.getLedgerBalance().setAmount(transactions.get(transactions.size()-1).getBalance().getValue());
				statement.getLedgerBalance().setDateOf(transactions.get(transactions.size()-1).getDate().value().atStartOfDay());
				ofxTransList.setDateStart(transactions.get(0).getDate().value());
				ofxTransList.setDateEnd(transactions.get(transactions.size()-1).getDate().value());
				for (Transaction trans : transactions) {
					ofxTransList.addTransaction(convertTransactionToOFXTransaction(trans));
				}
			} catch (ParseException e) {
				Figures.logStackTrace(e);
				return result.setCallBad("Export Failed", e.getLocalizedMessage());
			}
			exporter.exportOFX(context, bw);
			return result;
	}
	
	private CallResult exportCreditCardAccount(Account account, BufferedWriter bw) {
		List<Transaction> transactions = new ArrayList<Transaction>(account.getTransactionCount());
		transactions.addAll(account.getTransactions());
		Collections.sort(transactions);
		return null;
	}
	
	private OFXStatementTrans convertTransactionToOFXTransaction(Transaction trans) throws ParseException {
		OFXStatementTrans OFXTrans = new OFXStatementTrans();
		OFXTrans.setFitID(Integer.valueOf(trans.getID()).toString());
		if (!trans.getNumber().isEmpty()) {
			OFXTrans.setTransType(OFXTransType.CHECK);
			OFXTrans.setCheckNum(trans.getNumber());
		}else if (trans.getAmount().isCredit()){
			OFXTrans.setTransType(OFXTransType.CREDIT);
		}else {
			OFXTrans.setTransType(OFXTransType.DEBIT);
		}
		OFXTrans.setAmount(trans.getAmount().getValue());
		OFXTrans.setDatePosted(trans.getDate().value().atStartOfDay());
		OFXTrans.setName(trans.getDescription());
		OFXTrans.setMemo(trans.getMemo());
		return OFXTrans;
	}

}
