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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.ExportDao;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.qif.QIFAccountType;
import org.bluewindows.qif.QIFCallResult;
import org.bluewindows.qif.QIFCategory;
import org.bluewindows.qif.QIFExporter;
import org.bluewindows.qif.QIFRecord;

public class ExportQIFDaoImpl implements ExportDao {
	
	QIFExporter exporter = new QIFExporter();

	@Override
	public CallResult exportAccount(Account account, File file) {
		CallResult result = new CallResult();
		if (account == null) {
			return result.setCallBad("Missing Export Account", "No account specified for export");
		}
		if (file == null) {
			return result.setCallBad("Missing Export File", "No file specified for export");
		}
		QIFAccountType qifAccountType = null;
		if (account.getType().equals(AccountType.CREDIT_CARD)) {
			qifAccountType = QIFAccountType.CCard;
		}else {
			qifAccountType = QIFAccountType.Bank;
		}
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().setStatusBad("Could not load transactions. " + result.getErrorMessage());
    		return null;
		}
		List<Transaction> transactions = new ArrayList<Transaction>(account.getTransactionCount());
		transactions.addAll(account.getTransactions());
		Collections.sort(transactions);
		List<QIFRecord> qifRecords = new ArrayList<QIFRecord>();
		for (Transaction transaction : transactions) {
			qifRecords.add(convertTransactionToQIFRecord(transaction));
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
		} catch (IOException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Export Failed", e.getLocalizedMessage());
		}
		BufferedWriter writer = new BufferedWriter(fw);
		QIFCallResult qifResult = exporter.exportQIF(qifAccountType, qifRecords, writer);
		if (qifResult.isCallBad()) {
			result.setCallBad("Export Failed", qifResult.getException().getMessage());
		}
		return result;
	}
	
	private QIFRecord convertTransactionToQIFRecord(Transaction transaction) {
		QIFRecord qifRecord = new QIFRecord();
		qifRecord.setAmount(transaction.getAmount().getValue());
		qifRecord.setDate(transaction.getDate().value());
		qifRecord.setDescription(transaction.getDescription());
		qifRecord.setMemo(transaction.getMemo());
		if (!transaction.getNumber().isEmpty()) {
			qifRecord.setNumber(transaction.getNumber());
		}
		if (transaction.getCategories().size() == 1) {
			QIFCategory qifCategory = new QIFCategory();
			qifCategory.setName(transaction.getCategories().get(0).getName());
			qifRecord.addCategory(qifCategory);
		}else if (transaction.getCategories().size() > 1) {
			for (TransactionCategory transactionCategory : transaction.getCategories()) {
				QIFCategory qifCategory = new QIFCategory();
				qifCategory.setName(transactionCategory.getName());
				qifCategory.setAmount(transactionCategory.getAmount().getValue());
				qifRecord.addCategory(qifCategory);
			}
		}
		return qifRecord;
	}

}
