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

package org.bluewindows.figures.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.filter.Filter;

public interface PersistenceService {
	
	public PersistenceAdminDao getPersistenceAdminDao();
	public void setPersistenceAdminDao(PersistenceAdminDao persistencDao);
	public AccountDao getAccountDao();
	public void setAccountDao(AccountDao accountDao);
	public CategoryDao getCategoryDao();
	public void setCategoryDao(CategoryDao categoryDao);
	public FilterDao getFilterDao();
	public void setFilterDao(FilterDao filterDao);
	public FilterSetDao getFilterSetDao();
	public void setFilterSetDao(FilterSetDao filterSetDao);
	public TransactionDao getTransactionDao();
	public void setTransactionDao(TransactionDao transactionDao);
	public SummaryDao getSummaryDao();
	public void setSummaryDao(SummaryDao summaryDao);

	public boolean isPersistenceValid(File file) throws IOException;
	public CallResult openExisting(String fileName, String password);
	public CallResult openNew(String fileName, String password);
	public CallResult initialize(String fileName, String password, boolean isNew);
	public CallResult createDefaultData();
	public CallResult close();
	
	public CallResult getAccounts();
	public CallResult getAccount(int accountID);
	public CallResult getLastAccount();
	public CallResult addAccount(Account account);
	public CallResult updateAccount(Account account);
	public CallResult deleteAccount(Account account);
	public CallResult getAccountDateRange(Account account);

	public CallResult getCategories();
	public CallResult getLastCategory();
	public CallResult addCategory(String categoryName);
	public CallResult updateCategory(Category category);
	public CallResult deleteCategory(int categoryID);
	public CallResult checkCategoryUsage(int categoryID);
	
	public CallResult getFilters(int filterSetID);
	public CallResult getMaxFilterSequence();
	public CallResult addFilter(Filter filter);
	public CallResult updateFilter(Filter filter);
	public CallResult deleteFilter(int filterID);

	public CallResult getFilterSets();
	public CallResult getFilterSet(int filterSetID);
	public CallResult getLastFilterSet();
	public CallResult addFilterSet(FilterSet filterSet);
	public CallResult updateFilterSet(FilterSet filterSet);
	public CallResult deleteFilterSet(FilterSet filterSet);
	
	public CallResult getTransactions(Account account);
	public CallResult addTransactions(List<Transaction> transactions, Account account);
	public CallResult updateTransaction(Transaction transaction);
	public CallResult updateTransactions(List<Transaction> transactions);
	public CallResult deleteTransactions(Account account);
	public CallResult getDistinctValues(Account account, String field, boolean newTransactions, boolean depositsOnly, boolean withdrawalsOnly);
	
	public CallResult getReports();
	public CallResult saveReport(SummaryReport report);
	public CallResult updateReport(SummaryReport report);
	public CallResult deleteReport(SummaryReport report);
	public CallResult getSummaries(SummaryReport report);
	
	public CallResult startTransaction();
	public CallResult commitTransaction();
	public CallResult rollBackTransaction();
	public CallResult isTransactionActive();
	public CallResult optimize();
	
	public boolean isOpen();
	public void reset();
		
}
