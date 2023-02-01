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

import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY;
import static org.bluewindows.figures.domain.persistence.Persistence.MONTH;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.AccountDao;
import org.bluewindows.figures.dao.CategoryDao;
import org.bluewindows.figures.dao.FilterDao;
import org.bluewindows.figures.dao.FilterSetDao;
import org.bluewindows.figures.dao.PersistenceAdminDao;
import org.bluewindows.figures.dao.SummaryDao;
import org.bluewindows.figures.dao.TransactionDao;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.figures.enums.CategoryInclusion;
import org.bluewindows.figures.enums.CheckInclusion;
import org.bluewindows.figures.enums.DeductibleInclusion;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.FilterResult;
import org.bluewindows.figures.enums.TransactionInclusion;
import org.bluewindows.figures.filter.Filter;
import org.bluewindows.figures.service.PersistenceService;

public class PersistenceServiceImpl implements PersistenceService {

	private static PersistenceServiceImpl instance = new PersistenceServiceImpl();
	private PersistenceAdminDao persistenceAdminDao;
	private AccountDao accountDao;
	private CategoryDao categoryDao;
	private FilterDao filterDao;
	private FilterSetDao filterSetDao;
	private TransactionDao transactionDao;
	private SummaryDao summaryDao;
	private boolean opened = false;

	private PersistenceServiceImpl() {
	}

	public static PersistenceServiceImpl getInstance(){
		return instance;
	}
	
	@Override
	public boolean isPersistenceValid(File file) throws IOException {
		return persistenceAdminDao.isPersistenceValid(file);
	}
	
	@Override
	public CallResult initialize(String fileName, String password, boolean isNew) {
		CallResult result = new CallResult();
		if (isNew){
			result = openNew(fileName, password);
		}else{
			result = openExisting(fileName, password);
		}
		return result;
	}

	@Override
	public CallResult createDefaultData() {
		CallResult result = new CallResult();
		for (Category category : Figures.DEFAULT_CATEGORIES) {
			result = categoryDao.insertCategory(category);
			if (result.isBad()) return result;
		}
		FilterSet checkingFilterSet = new FilterSet("Checking");
		checkingFilterSet.setDefaultColumn(FilterField.DESCRIPTION.toString());
		checkingFilterSet.setDefaultExpression(FilterExpression.CONTAINS.toString());
		checkingFilterSet.setDefaultResult(FilterResult.REPLACE_DESCRIPTION.toString());
		result = filterSetDao.addSet(checkingFilterSet);
		if (result.isBad()) return result;
		result = filterSetDao.getLastSet();
		if (result.isBad()) return result;
		checkingFilterSet = (FilterSet)result.getReturnedObject();
		FilterSet savingsFilterSet = new FilterSet("Savings");
		savingsFilterSet.setDefaultColumn(FilterField.DESCRIPTION.toString());
		savingsFilterSet.setDefaultExpression(FilterExpression.CONTAINS.toString());
		savingsFilterSet.setDefaultResult(FilterResult.REPLACE_DESCRIPTION.toString());
		result = filterSetDao.addSet(savingsFilterSet);
		if (result.isBad()) return result;
		result = filterSetDao.getLastSet();
		if (result.isBad()) return result;
		savingsFilterSet = (FilterSet)result.getReturnedObject();
		Account checkingAccount = new Account(0, "Checking", AccountType.CHECKING, checkingFilterSet, Money.ZERO, TransactionDate.MINIMUM_DATE);
		result = accountDao.addAccount(checkingAccount);
		if (result.isBad()) return result;
		Account savingsAccount = new Account(0, "Savings", AccountType.SAVINGS, savingsFilterSet, Money.ZERO, TransactionDate.MINIMUM_DATE);
		result = accountDao.addAccount(savingsAccount);
		if (result.isBad()) return result;
		result = accountDao.getAccounts();
		if (result.isBad()) return result;
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(accounts.get(0));
		SummaryReport sampleReport1 = new SummaryReport();
		sampleReport1.setName("Spending by Month");
		sampleReport1.setAccounts(reportAccounts);
		sampleReport1.setSummaryFields(MONTH);
		sampleReport1.setTransactionInclusion(TransactionInclusion.WITHDRAWALS);
		sampleReport1.setCategoryInclusion(CategoryInclusion.ALL);
		sampleReport1.setDeductibleInclusion(DeductibleInclusion.NONE);
		sampleReport1.setCheckInclusion(CheckInclusion.NONE);
		result = summaryDao.saveReport(sampleReport1);
		SummaryReport sampleReport2 = new SummaryReport();
		sampleReport2.setName("Spending by Month and Category");
		sampleReport2.setAccounts(reportAccounts);
		sampleReport2.setSummaryFields(MONTH + "," + CATEGORY);
		sampleReport2.setTransactionInclusion(TransactionInclusion.WITHDRAWALS);
		sampleReport2.setCategoryInclusion(CategoryInclusion.ALL);
		sampleReport2.setDeductibleInclusion(DeductibleInclusion.NONE);
		sampleReport2.setCheckInclusion(CheckInclusion.NONE);
		result = summaryDao.saveReport(sampleReport2);
		return result;
	}
	
	@Override
	public CallResult openExisting(String fileName, String password){
		CallResult result = new CallResult();
		File file = new File(fileName);
		if (!file.exists()) {
			result.setCallBad("File Open Failure", "Could not open " + file.getAbsolutePath());
			return result;
		}
		result = persistenceAdminDao.openExisting(fileName, password);
		if (result.isBad()) return result;
		opened = true;
		return result;
	}
	
	@Override
	public CallResult openNew(String fileName, String password){
		return persistenceAdminDao.openNew(fileName, password);
	}
	
	@Override
	public CallResult close() {
		return persistenceAdminDao.close();
	}

	@Override
	public CallResult getAccounts() {
		return accountDao.getAccounts();
	}
	
	@Override
	public CallResult getAccount(int accountID) {
		return accountDao.getAccount(accountID);
	}

	@Override
	public CallResult getLastAccount() {
		return accountDao.getLastAccount();
	}

	@Override
	public CallResult addAccount(Account account) {
		CallResult result = accountDao.addAccount(account);
		if (result.isBad()){
			persistenceAdminDao.rollBackTransaction();
			return result;
		}
		Figures.accountTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}

	@Override
	public CallResult updateAccount(Account account) {
		Figures.accountTimestamp = new Timestamp(System.currentTimeMillis());
		return accountDao.updateAccount(account);
	}

	@Override
	public CallResult deleteAccount(Account account) {
		CallResult result = accountDao.deleteAccount(account);
		if (result.isBad()){
			return result;
		}
		Figures.accountTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}

	@Override
	public CallResult getAccountDateRange(Account account) {
		CallResult result = transactionDao.getTransactionDateRange(account);
		if (result.isBad()) return result;
		DateRange dateRange = (DateRange)result.getReturnedObject();
		if (dateRange.getStartDate() == null) {
			dateRange.setStartDate(TransactionDate.MINIMUM_DATE);
			dateRange.setEndDate(TransactionDate.MINIMUM_DATE);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CallResult getCategories() {
		CallResult result = categoryDao.getCategories();
		if (result.isBad()) return result;
		List<Category> categories = (List<Category>)result.getReturnedObject();
		Collections.sort(categories, new Comparator<Category>() {
			@Override
			public int compare(Category c1, Category c2) {
				return c1.getName().compareTo(c2.getName());
			}
		});	
		return result;
	}

	@Override
	public CallResult getLastCategory() {
		return categoryDao.getLastCategory();
	}

	@Override
	public CallResult addCategory(String categoryName) {
		Figures.categoryTimestamp = new Timestamp(System.currentTimeMillis());
		return categoryDao.addCategory(categoryName);
	}

	@Override
	public CallResult updateCategory(Category category) {
		CallResult result = new CallResult();
		if (category.getID() == 0) {
			return result.setCallBad("Cannot update system default None category.");
		}
		Figures.categoryTimestamp = new Timestamp(System.currentTimeMillis());
		return categoryDao.updateCategory(category);
	}
	
	@Override
	public CallResult checkCategoryUsage(int categoryID) {
		return categoryDao.checkCategoryUsage(categoryID);
	}

	@Override
	public CallResult deleteCategory(int categoryID) {
		CallResult result = new CallResult();
		if (categoryID == 0) {
			return result.setCallBad("Cannot delete system default None category.");
		}
		result = persistenceAdminDao.startTransaction();
		if (result.isBad()) return result;
		result = purgeCategoryFromFilters(categoryID);
		if (result.isBad()) return rollBackTransaction(result);
		result = categoryDao.deleteCategory(categoryID);
		if (result.isBad()) return rollBackTransaction(result);
		result = persistenceAdminDao.commitTransaction();
		Figures.categoryTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}

	@SuppressWarnings("unchecked")
	private CallResult purgeCategoryFromFilters(int categoryID) {
		CallResult result = new CallResult();
		if (categoryID == 0) {
			return result.setCallBad("Cannot purge system default None category.");
		}
		result = getFilterSets();
		if (result.isBad()) return result;
		List<FilterSet> filterSets = (List<FilterSet>) result.getReturnedObject();
		for (FilterSet filterSet : filterSets) {
			for (Filter filter : filterSet.getFilters()) {
				if (filter.getCategoryID() == categoryID) {
					result = deleteFilter(filter.getID());
					Figures.filterTimestamp = new Timestamp(System.currentTimeMillis());
					if (result.isBad()) return result;
				}
			}
		}
		return result;
	}
	
	private CallResult rollBackTransaction(CallResult result) {
		persistenceAdminDao.rollBackTransaction();
		return result;
	}

	@Override
	public CallResult addFilter(Filter filter) {
		Figures.filterTimestamp = new Timestamp(System.currentTimeMillis());
		return filterDao.addFilter(filter);
	}

	@Override
	public CallResult deleteFilter(int filterID) {
		Figures.filterTimestamp = new Timestamp(System.currentTimeMillis());
		return filterDao.deleteFilter(filterID);
	}

	@Override
	public CallResult getFilters(int filterSetID) {
		return filterDao.getFilters(filterSetID);
	}

	@Override
	public CallResult getMaxFilterSequence() {
		return filterDao.getMaxFilterSequence();
	}

	@Override
	public CallResult updateFilter(Filter filter) {
		Figures.filterTimestamp = new Timestamp(System.currentTimeMillis());
		return filterDao.updateFilter(filter);
	}

	@Override
	public CallResult addFilterSet(FilterSet filterSet) {
		Figures.filterSetTimestamp = new Timestamp(System.currentTimeMillis());
		return filterSetDao.addSet(filterSet);
	}

	@Override
	public CallResult deleteFilterSet(FilterSet filterSet) {
		Figures.filterSetTimestamp = new Timestamp(System.currentTimeMillis());
		return filterSetDao.deleteSet(filterSet.getID());
	}

	@SuppressWarnings("unchecked")
	@Override
	public CallResult getFilterSets() {
		CallResult result = new CallResult();
		List<FilterSet> filterSets = new ArrayList<FilterSet>();
		result = filterSetDao.getSets();
		if (result.isBad()) return result;
		filterSets = (List<FilterSet>)result.getReturnedObject();
		for (FilterSet filterSet : filterSets) {
			result = filterDao.getFilters(filterSet.getID());
			if (result.isBad()) return result;
			filterSet.setFilters((List<Filter>)result.getReturnedObject());
		}
		result.setReturnedObject(filterSets);
		return result;
	}

	@Override
	public CallResult getFilterSet(int filterSetID) {
		return filterSetDao.getSet(filterSetID);
	}

	@Override
	public CallResult getLastFilterSet() {
		return filterSetDao.getLastSet();
	}

	@Override
	public CallResult updateFilterSet(FilterSet filterSet) {
		Figures.filterSetTimestamp = new Timestamp(System.currentTimeMillis());
		return filterSetDao.updateSet(filterSet);
	}
	
	@Override
	public CallResult getTransactions(Account account) {
		return transactionDao.getTransactions(account);
	}

	@Override
	public CallResult addTransactions(List<Transaction> transactions, Account account) {
		for (Transaction transaction : transactions) {
			if (transaction.getAddedCategories().isEmpty()) {
				transaction.setCategory(new TransactionCategory(0, transaction.getAmount()));
			}
		}
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return transactionDao.addTransactions(transactions, account);
	}
	
	@Override
	public CallResult updateTransaction(Transaction transaction) {
		CallResult result = persistenceAdminDao.isTransactionActive();
		if (result.isBad()) return result;
		boolean dbTransactionStartedElsewhere = (Boolean)result.getReturnedObject();
		if (!dbTransactionStartedElsewhere) {
			result = persistenceAdminDao.startTransaction();
			if (result.isBad()) return result;
		}
		if (transaction.getAddedCategories().isEmpty() && transaction.getCategories().isEmpty()) {
			transaction.setCategory(new TransactionCategory(0, transaction.getAmount()));
		}
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		result = transactionDao.updateTransaction(transaction);
		if (dbTransactionStartedElsewhere) return result;
		if (result.isBad()) {
			persistenceAdminDao.rollBackTransaction();
		}else {
			persistenceAdminDao.commitTransaction();
		}
		return result;
	}
	
	@Override
	public CallResult updateTransactions(List<Transaction> transactions) {
		CallResult result = persistenceAdminDao.isTransactionActive();
		if (result.isBad()) return result;
		boolean transactionStartedElsewhere = (Boolean)result.getReturnedObject();
		if (!transactionStartedElsewhere) {
			result = persistenceAdminDao.startTransaction();
			if (result.isBad()) return result;
		}
		for (Transaction transaction : transactions) {
			if (transaction.getAddedCategories().isEmpty() && transaction.getCategories().isEmpty()) {
				transaction.setCategory(new TransactionCategory(0, transaction.getAmount()));
			}
		}
		result = transactionDao.updateTransactions(transactions);
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		if (transactionStartedElsewhere) return result;
		if (result.isBad()) {
			persistenceAdminDao.rollBackTransaction();
		}else {
			persistenceAdminDao.commitTransaction();
		}
		return result;
	}
	
	@Override
	public CallResult deleteTransactions(Account account) {
		Figures.transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return transactionDao.deleteTransactions(account);
	}

	@Override
	public CallResult getDistinctValues(Account account, String field, boolean newTransactions, boolean depositsOnly, boolean withdrawalsOnly) {
		return transactionDao.getDistinctValues(account, field, newTransactions, depositsOnly, withdrawalsOnly);
	}
	
	public PersistenceAdminDao getPersistenceAdminDao() {
		return persistenceAdminDao;
	}

	public void setPersistenceAdminDao(PersistenceAdminDao persistenceDao) {
		this.persistenceAdminDao = persistenceDao;
	}

	@Override
	public CategoryDao getCategoryDao() {
		return categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	@Override
	public AccountDao getAccountDao() {
		return accountDao;
	}

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	@Override
	public FilterDao getFilterDao() {
		return filterDao;
	}

	public void setFilterDao(FilterDao filterDao) {
		this.filterDao = filterDao;
	}

	@Override
	public FilterSetDao getFilterSetDao() {
		return filterSetDao;
	}

	public void setFilterSetDao(FilterSetDao filterSetDao) {
		this.filterSetDao = filterSetDao;
	}
	
	@Override
	public TransactionDao getTransactionDao() {
		return transactionDao;
	}

	public void setTransactionDao(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	@Override
	public SummaryDao getSummaryDao() {
		return summaryDao;
	}

	@Override
	public void setSummaryDao(SummaryDao summaryDao) {
		this.summaryDao = summaryDao;
		
	}

	@Override
	public boolean isOpen() {
		return opened;
	}

	@Override
	public void reset() {
	}

	@Override
	public CallResult getReports() {
		return summaryDao.getReports();
	}

	@Override
	public CallResult saveReport(SummaryReport report) {
		CallResult result = persistenceAdminDao.startTransaction();
		if (result.isBad()) return result;
		Figures.reportTimestamp = new Timestamp(System.currentTimeMillis());
		result = summaryDao.saveReport(report);
		if (result.isBad()) {
			persistenceAdminDao.rollBackTransaction();
		}else {
			persistenceAdminDao.commitTransaction();
		}
		return result;

	}

	@Override
	public CallResult updateReport(SummaryReport report) { 
		CallResult result = persistenceAdminDao.isTransactionActive();
		if (result.isBad()) return result;
		boolean transactionStartedElsewhere = (Boolean)result.getReturnedObject();
		if (!transactionStartedElsewhere) {
			result = persistenceAdminDao.startTransaction();
			if (result.isBad()) return result;
		}
		Figures.reportTimestamp = new Timestamp(System.currentTimeMillis());
		result =  summaryDao.updateReport(report);
		if (transactionStartedElsewhere) return result;
		if (result.isBad()) {
			persistenceAdminDao.rollBackTransaction();
		}else {
			persistenceAdminDao.commitTransaction();
		}
		return result;
	}

	@Override
	public CallResult deleteReport(SummaryReport report) {
		CallResult result = persistenceAdminDao.isTransactionActive();
		if (result.isBad()) return result;
		boolean transactionStartedElsewhere = (Boolean)result.getReturnedObject();
		if (!transactionStartedElsewhere) {
			result = persistenceAdminDao.startTransaction();
			if (result.isBad()) return result;
		}
		Figures.reportTimestamp = new Timestamp(System.currentTimeMillis());
		result = summaryDao.deleteReport(report);
		if (transactionStartedElsewhere) return result;
		if (result.isBad()) {
			persistenceAdminDao.rollBackTransaction();
		}else {
			persistenceAdminDao.commitTransaction();
		}
		return result;
	}

	@Override
	public CallResult getSummaries(SummaryReport report) {
		return summaryDao.getSummaries(report);
	}

	@Override
	public CallResult startTransaction() {
		return persistenceAdminDao.startTransaction();
	}

	@Override
	public CallResult commitTransaction() {
		return persistenceAdminDao.commitTransaction();
	}

	@Override
	public CallResult rollBackTransaction() {
		return persistenceAdminDao.rollBackTransaction();
	}

	@Override
	public CallResult isTransactionActive() {
		return persistenceAdminDao.isTransactionActive();
	}

	@Override
	public CallResult optimize() {
		return persistenceAdminDao.optimize();
	}

}
