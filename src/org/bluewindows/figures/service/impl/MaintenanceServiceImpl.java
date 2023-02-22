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
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.domain.persistence.Persistence;
import org.bluewindows.figures.filter.Filter;
import org.bluewindows.figures.service.MaintenanceService;
import org.bluewindows.figures.service.ServiceFactory;

public class MaintenanceServiceImpl implements MaintenanceService {
	
	private static MaintenanceServiceImpl instance = new MaintenanceServiceImpl();

	private MaintenanceServiceImpl() {
	}
	
	public static MaintenanceServiceImpl getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CallResult importFile(File file, Account account) {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().setStatusBad("Could not load transactions. " + result.getErrorMessage());
    		return null;
		}
		result = ServiceFactory.getInstance().getImportSvc().importFile(file, account);
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().setStatusBad("Could not load transactions. " + result.getErrorMessage());
			return result;
		}
		List<Transaction> importedTransactions = (List<Transaction>) result.getReturnedObject();
		// Imported transaction are in ascending order by date
		TransactionDate importStartDate = importedTransactions.get(0).getDate();
		TransactionDate importEndDate = importedTransactions.get(importedTransactions.size()-1).getDate();
		result = ServiceFactory.getInstance().getPersistenceSvc().getAccountDateRange(account);
		if (result.isBad()) return result;
		DateRange accountDateRange = (DateRange) result.getReturnedObject();
		// Because QIF files (and possibly some OFX files) do not provide a unique identifier for each transaction, 
		// it is not possible to merge transactions within a given date and preserve the original transaction order.
		// So if the account already contains a given date, imported transactions for that date will be skipped.
		List<Transaction> transactionsToLoad;
		if (accountDateRange.getEndDate().compareTo(importStartDate) < 0 ||
		   (accountDateRange.getStartDate().compareTo(importEndDate) > 0)){
			transactionsToLoad = importedTransactions;
		}else {
			transactionsToLoad = getTransactionsForDatesNotYetLoaded(importedTransactions, account.getTransactions());
		}
		if (transactionsToLoad.size() > 0) {
			result = loadImportedTransactions(account, accountDateRange, transactionsToLoad);
			if (result.isBad()) return result;
		}
		List<Integer> importCounts = new ArrayList<Integer>();
		importCounts.add(Integer.valueOf(transactionsToLoad.size()));
		importCounts.add(Integer.valueOf(importedTransactions.size() - transactionsToLoad.size()));	
		result.setReturnedObject(importCounts);
		return result;
	}
	
	private List<Transaction> getTransactionsForDatesNotYetLoaded(List<Transaction> importedTransactions, List<Transaction> alreadyLoadedTransactions) {
		List<TransactionDate> alreadLoadedDates = new ArrayList<TransactionDate>();
		TransactionDate currentDate = TransactionDate.MINIMUM_DATE;
		List<Transaction> transactionsToLoad = new ArrayList<Transaction>();
		// Loaded transactions are sorted by date
		for (Transaction transaction : alreadyLoadedTransactions) {
			if (!transaction.getDate().equals(currentDate)) {
				alreadLoadedDates.add(transaction.getDate());
				currentDate = transaction.getDate();
			}
		}
		for (Transaction importedTransaction : importedTransactions) {
			if (!alreadLoadedDates.contains(importedTransaction.getDate())) {
				transactionsToLoad.add(importedTransaction);
			}
		}
		return transactionsToLoad;
	}

	private CallResult loadImportedTransactions(Account account, DateRange accountDateRange, List<Transaction> transactionsToImport) {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().startTransaction();
		if (result.isBad()) return result;
		if (transactionsToImport.get(0).getDate().compareTo(accountDateRange.getEndDate()) > 0) {
			result = addNewTransactions(transactionsToImport, account);
		}else {
			result = mergeNewTransactions(transactionsToImport, account);
		}
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		// Save this import start date to use as a starting date for defining filters for "new" transactions
		// If multiple files are loaded, retain the earliest load date that hasn't yet been filtered
		if (account.getLastLoadedDate().compareTo(transactionsToImport.get(0).getDate()) < 0) {
			if (account.getLastFilteredDate().equals(account.getLastLoadedDate())) {
				account.setLastLoadDate(transactionsToImport.get(0).getDate());
			}
		}else {
			account.setLastLoadDate(transactionsToImport.get(0).getDate());
		}
		result = ServiceFactory.getInstance().getPersistenceSvc().updateAccount(account);
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		result = ServiceFactory.getInstance().getPersistenceSvc().commitTransaction();
		if (result.isBad()) return result;
		ServiceFactory.getInstance().getPersistenceSvc().optimize();
		return result;
	}
	
	private CallResult addNewTransactions(List<Transaction> newTransactions, Account account) {
		Money currentBalance;
		if (account.getTransactionCount() > 0) {
			currentBalance = account.getTransactions().get(0).getBalance();
		}else {
			currentBalance = account.getInitialBalance();
		}
		balanceNewTransactions(newTransactions, currentBalance);
		return filterAndSaveTransactions(newTransactions, account);
	}
	
	private CallResult mergeNewTransactions(List<Transaction> importedTransactions, Account account) {
		for (Transaction transaction : importedTransactions) {
			transaction.setBalance(Money.ZERO);
		}
		CallResult result = filterAndSaveTransactions(importedTransactions, account);
		if (result.isBad()) return result;
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		if (result.isBad()) return result;
		applyInitialBalance(account.getTransactions(), account.getInitialBalance());
		List<Transaction> updatedTransactions = new ArrayList<Transaction>();
		for (Transaction transaction : account.getTransactions()) {
			if (transaction.isUpdated()) updatedTransactions.add(transaction);
		}
		if (updatedTransactions.size() > 0) {
			result = ServiceFactory.getInstance().getPersistenceSvc().updateTransactions(updatedTransactions);
		}
		return result;
	}

	@Override
	public int filterTransactions(List<Transaction> transactions, Account account) {
		if (transactions.isEmpty()){
			return 0;
		}
		if (account.getFilterSet().getFilters().isEmpty()){
			return 0;
		}
		Figures.logger.info("Filtering transactions...");
		int transactionsUpdatedCount = 0;
		int redundantFiltersCount = 0;
		boolean transactionAlreadyFiltered;
		boolean thisFilterExecuted;
		boolean redundantFilterFound;
		List<Filter> executedFilters = new ArrayList<Filter>();
		List<Filter> redundantFilters = new ArrayList<Filter>();
		for (Transaction trans : transactions) {
			transactionAlreadyFiltered = false;
			executedFilters.clear();
			redundantFilterFound = false;
			for (Filter filter : account.getFilterSet().getFilters()) {
				thisFilterExecuted = filter.execute(trans);
				if (thisFilterExecuted) {
					executedFilters.add(filter);
					// Only check for redundant filters for existing transactions, not for newly imported ones
					if (transactionAlreadyFiltered && trans.getDate().compareTo(account.getLastLoadedDate()) < 0) { 
						redundantFilterFound = true;
						if (! redundantFilters.contains(filter)) {
							redundantFiltersCount++;
							redundantFilters.add(filter);
						}
					}
					transactionAlreadyFiltered = true;
				}
			}
			if (redundantFilterFound) {
				for (Filter filter : executedFilters) {
					Figures.logger.info("Redundant filter: " + filter.getFieldName() + " " + filter.getExpression() + " " + filter.getSearchValue());
				}
				if (executedFilters.get(0).getField().equals(Persistence.DESCRIPTION)) {
					Figures.logger.info("Transaction description: " + trans.getDescription() + ", Original description: " + trans.getOriginalDescription());
				}else if (executedFilters.get(0).getField().equals(Persistence.MEMO)) {
					Figures.logger.info("Transaction memo: " + trans.getMemo() + ", Original memo: " + trans.getOriginalMemo());
				}
			}
			if (trans.isUpdated()) transactionsUpdatedCount++;
		}
		Figures.logger.info("Filtered " + transactions.size() + " transactions.  Updated " + transactionsUpdatedCount + " transactions.");
		if (redundantFiltersCount > 0) {
			Figures.logger.info("Found " + redundantFiltersCount + " redundant filters.");
		}
		return redundantFiltersCount;
	}
	
	@Override
	public CallResult filterTransaction(Transaction transaction, Account account) {
		CallResult result = new CallResult();
		if (transaction == null){
			return result;
		}
		if (account.getFilterSet().getFilters().isEmpty()){
			return result;
		}
		for (Filter filter : account.getFilterSet().getFilters()) {
			filter.execute(transaction);
		}
		if (transaction.isUpdated()) {
			result = ServiceFactory.getInstance().getPersistenceSvc().updateTransaction(transaction);
		}
		return result;
	}
	
	@Override
	public CallResult filterAndSaveTransactions(List<Transaction> transactions, Account account) {
		filterTransactions(transactions, account);
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().addTransactions(transactions, account);
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}

	@Override
	public CallResult filterAndUpdateTransactions(Account account) {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		if (result.isBad()) return result;
		int redundantFilterCount = filterTransactions(account.getTransactions(), account);
		List<Transaction> updatedTransactions = account.getUpdatedTransactions();
		if (updatedTransactions.size() > 0) {
			ServiceFactory.getInstance().getPersistenceSvc().startTransaction();
			result = ServiceFactory.getInstance().getPersistenceSvc().updateTransactions(updatedTransactions);
			if (result.isGood()) {
				result = ServiceFactory.getInstance().getPersistenceSvc().commitTransaction();
			}else {
				ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			}
		}
		List<Integer> returnedCounts = new ArrayList<Integer>();
		returnedCounts.add(Integer.valueOf(updatedTransactions.size()));
		returnedCounts.add(Integer.valueOf(redundantFilterCount));
		result.setReturnedObject(returnedCounts);
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}
	
	@Override
	public CallResult resequenceFilters(FilterSet filterSet, int filterIdToUpdate, int newSequence) {
		CallResult result = new CallResult();
		Filter filterToUpdate = null;
		for (Filter filter : filterSet.getFilters()) {
			filter.setReSequence((float)filter.getSequence());
			if (filter.getID() == filterIdToUpdate) {
				filterToUpdate = filter;
			}
		}
		if (filterToUpdate == null) {
			return result.setCallBad("Filter with ID " + filterIdToUpdate + " not found.");
		}
		if (newSequence < filterToUpdate.getSequence().intValue()) {
			filterToUpdate.setReSequence((float)(newSequence - 0.5));
		}else {
			filterToUpdate.setReSequence((float)(newSequence + 0.5));
		}
		Collections.sort(filterSet.getFilters(), new Comparator<Filter>() {
			@Override
			public int compare(Filter f1, Filter f2) {
				if (f1.getReSequence() < f2.getReSequence()) return -1;
		        if (f1.getReSequence() > f2.getReSequence()) return 1; 
		        return 0;
			}
		});	
		int sequence = 0;
		for (Filter filter : filterSet.getFilters()) {
			sequence++;
			if (sequence != filter.getSequence().intValue()) {
				filter.setSequence(sequence);
				result = ServiceFactory.getInstance().getPersistenceSvc().updateFilter(filter);
				if (result.isBad()) return result;
			}
		}
		return result;
	}

 
	@Override
	public Money applyInitialBalance(List<Transaction> transactions, Money initialBalance) {
		if (transactions.size() == 0) return initialBalance;
		Money firstTransactionBalance = new Money(0.00);
		firstTransactionBalance.add(initialBalance);
		firstTransactionBalance.add(transactions.get(transactions.size()-1).getAmount());
		if (!transactions.get(transactions.size()-1).getBalance().equals(firstTransactionBalance)) {
			transactions.get(transactions.size()-1).setBalance(firstTransactionBalance);
		}
		if (transactions.size() == 1) return initialBalance;
		BigDecimal thisTransactionAmount = null;
		BigDecimal prevTransactionBalance = null;
		// Transactions are in descending date order (from most recent to least recent)
		for (int i = transactions.size()-2; i >= 0; i--) {
			Transaction transaction = transactions.get(i);
			thisTransactionAmount = transaction.getAmount().getValue();
			prevTransactionBalance = transactions.get(i+1).getBalance().getValue();
			Money balance = new Money(thisTransactionAmount.add(prevTransactionBalance));
			if (!balance.equals(transaction.getBalance())) {
				transaction.setBalance(balance);
			}
		}
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return transactions.get(0).getBalance();
	}

	@Override
	public Money applyCurrentBalance(Account account, Money currentBalance) {
		if (account.getTransactionCount() == 0) return currentBalance;
		account.getTransactions().get(0).setBalance(currentBalance);
		if (account.getTransactionCount() == 1) return currentBalance;
		BigDecimal prevTransactionBalance = null;
		BigDecimal prevTransactionAmount = null;
		List<Transaction> transactions = account.getTransactions();
		// Transactions are in descending date order (from most recent to least recent)
		for (int i = 1; i < transactions.size(); i++) {
			prevTransactionBalance = transactions.get(i-1).getBalance().getValue();
			prevTransactionAmount = transactions.get(i-1).getAmount().getValue();
			transactions.get(i).setBalance(new Money(prevTransactionBalance.subtract(prevTransactionAmount)));
		}
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return account.getTransactions().get(account.getTransactionCount()-1).getBalance();
	}
	
	private Money balanceNewTransactions(List<Transaction> transactions, Money currentBalance) {
		if (transactions.size() == 0) return currentBalance;
		Money newCurrentBalance = new Money(currentBalance.getValue().add(transactions.get(0).getAmount().getValue()));
		transactions.get(0).setBalance(newCurrentBalance);
		if (transactions.size() == 1) return newCurrentBalance;
		BigDecimal thisTransactionAmount = null;
		BigDecimal prevTransactionBalance = null;
		// Transactions are in ascending order (from least recent to most recent)
		for (int i = 1; i < transactions.size(); i++) {
			thisTransactionAmount = transactions.get(i).getAmount().getValue();
			prevTransactionBalance = transactions.get(i-1).getBalance().getValue();
			transactions.get(i).setBalance(new Money(thisTransactionAmount.add(prevTransactionBalance)));
		}
		return transactions.get(0).getBalance();
	}
	
	@Override
	public CallResult deleteAccount(Account account) {
		ServiceFactory.getInstance().getPersistenceSvc().startTransaction();
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().deleteTransactions(account);
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		
		result = ServiceFactory.getInstance().getPersistenceSvc().commitTransaction();
		result = ServiceFactory.getInstance().getPersistenceSvc().optimize();
		ServiceFactory.getInstance().getPersistenceSvc().startTransaction();
		
		result = ServiceFactory.getInstance().getPersistenceSvc().getReports();
		if (result.isBad()) return result;
		@SuppressWarnings("unchecked")
		List<SummaryReport> reports = (List<SummaryReport>) result.getReturnedObject();
		for (SummaryReport report : reports) {
			if (report.getAccounts().contains(account)) {
				report.deleteAccount(account);
				result = ServiceFactory.getInstance().getPersistenceSvc().updateReport(report);
				if (result.isBad()) break;
			}
		}
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		result = ServiceFactory.getInstance().getPersistenceSvc().deleteAccount(account);
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		result = ServiceFactory.getInstance().getPersistenceSvc().commitTransaction();
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		Figures.accountTimestamp = new Timestamp(System.currentTimeMillis());
		return ServiceFactory.getInstance().getPersistenceSvc().optimize();
	}

}
