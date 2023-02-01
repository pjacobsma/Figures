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
package org.bluewindows.figures.dao.impl.sqlite.test;

import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY;
import static org.bluewindows.figures.domain.persistence.Persistence.DESCRIPTION;
import static org.bluewindows.figures.domain.persistence.Persistence.ID;
import static org.bluewindows.figures.domain.persistence.Persistence.MONTH;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.YEAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bluewindows.figures.dao.impl.sqlite.AccountDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.CategoryDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.SummaryDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.TransactionDaoImplSqlite;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Summary;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.figures.enums.CategoryInclusion;
import org.bluewindows.figures.enums.CheckInclusion;
import org.bluewindows.figures.enums.DeductibleInclusion;
import org.bluewindows.figures.enums.TransactionInclusion;
import org.junit.Before;
import org.junit.Test;

public class SummaryDaoImplSqliteTest extends AbstractDaoImplSqliteTestCase {
	
	private AccountDaoImplSqlite accountDao;
	private CategoryDaoImplSqlite categoryDao;
	private TransactionDaoImplSqlite transactionDao;
	private SummaryDaoImplSqlite summaryDao;
	private Account account1;
	private Account account2;
	private Account account3;
	private List<Account> accounts;
	private List<Category> categories;
	
	@Before
	public void before(){
		super.before();
		accountDao = (AccountDaoImplSqlite)persistence.getAccountDao();
		categoryDao = (CategoryDaoImplSqlite)persistence.getCategoryDao();
		transactionDao = (TransactionDaoImplSqlite)persistence.getTransactionDao();
		summaryDao = (SummaryDaoImplSqlite)persistence.getSummaryDao();
	}
	
	@Test
	public void testGetReports() throws Exception {
		CallResult result = summaryDao.getReports();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<SummaryReport> noReports = (List<SummaryReport>)result.getReturnedObject();
		assertEquals(0, noReports.size());
		SummaryReport report = new SummaryReport();
		report.setID(100);
		report.setName("Test Report");
		report.setSummaryFields(YEAR);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.INCLUDED);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		List<Account> accounts = new ArrayList<Account>();
		Account account1 = saveTestAccount("Account 1", 100);
		accounts.add(account1);
		Account account2 = saveTestAccount("Account 2", 200);
		accounts.add(account2);
		report.setAccounts(accounts);
		result = categoryDao.addCategory("Appliances");
		assertTrue(result.isGood());
		result = categoryDao.addCategory("Car Payment");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>)result.getReturnedObject();
		report.setCategories(categories);
		saveTestReport(report);
		result = summaryDao.getReports();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<SummaryReport> reports = (List<SummaryReport>)result.getReturnedObject();
		assertEquals(1, reports.size());
		SummaryReport returnedReport = reports.get(0);
		assertEquals(100, returnedReport.getID());
		assertEquals("Test Report", returnedReport.getName());
		assertEquals(YEAR, returnedReport.getSummaryFields());
		assertEquals(TransactionInclusion.ALL, returnedReport.getTransactionInclusion());
		assertEquals(CategoryInclusion.INCLUDED, returnedReport.getCategoryInclusion());
		assertEquals(2, returnedReport.getAccounts().size());
		assertEquals(100, returnedReport.getAccounts().get(0).getID());
		assertEquals(200, returnedReport.getAccounts().get(1).getID());
		assertEquals(2, returnedReport.getCategories().size());
		assertEquals(1, returnedReport.getCategories().get(0).getID());
		assertEquals("Appliances", returnedReport.getCategories().get(0).getName());
		assertEquals(2, returnedReport.getCategories().get(1).getID());
		assertEquals("Car Payment", returnedReport.getCategories().get(1).getName());
	}
	
	@Test
	public void testSaveReport() throws Exception {
		SummaryReport report = new SummaryReport();
		report.setID(1);
		report.setName("Test Report");
		report.setSummaryFields(YEAR);
		report.setTransactionInclusion(TransactionInclusion.WITHDRAWALS);
		report.setCategoryInclusion(CategoryInclusion.INCLUDED);
		report.setCategorizedOnly(true);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		List<Account> accounts = new ArrayList<Account>();
		Account account1 = saveTestAccount("Account 1", 100); 
		accounts.add(account1);
		Account account2 = saveTestAccount("Account 2", 200); 
		accounts.add(account2);
		report.setAccounts(accounts);
		CallResult result = categoryDao.addCategory("Category 1");
		assertTrue(result.isGood());
		result = categoryDao.addCategory("Category 2");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>)result.getReturnedObject();
		report.setCategories(categories);
		result = summaryDao.saveReport(report);
		assertTrue(result.isGood());
		result = summaryDao.getReports();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<SummaryReport> reports = (List<SummaryReport>)result.getReturnedObject();
		assertEquals(1, reports.size());
		SummaryReport returnedReport = reports.get(0);
		assertEquals(1, returnedReport.getID());
		assertEquals("Test Report", returnedReport.getName());
		assertEquals(YEAR, returnedReport.getSummaryFields());
		assertEquals(TransactionInclusion.WITHDRAWALS, returnedReport.getTransactionInclusion());
		assertEquals(CategoryInclusion.INCLUDED, returnedReport.getCategoryInclusion());
		assertTrue(report.isCategorizedOnly());
		assertEquals(DeductibleInclusion.EXCLUDED, returnedReport.getDeductibleInclusion());
		assertEquals(CheckInclusion.EXCLUDED, returnedReport.getCheckInclusion());
		assertEquals(2, returnedReport.getAccounts().size());
		assertEquals(100, returnedReport.getAccounts().get(0).getID());
		assertEquals(200, returnedReport.getAccounts().get(1).getID());
		assertEquals(2, returnedReport.getCategories().size());
		assertEquals(1, returnedReport.getCategories().get(0).getID());
		assertEquals(2, returnedReport.getCategories().get(1).getID());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateReport() throws Exception {
		SummaryReport report = new SummaryReport();
		report.setID(99);
		report.setName("Test Report");
		report.setSummaryFields(YEAR);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.INCLUDED);
		report.setCategorizedOnly(true);
		report.setDeductibleInclusion(DeductibleInclusion.NONE);
		report.setCheckInclusion(CheckInclusion.NONE);
		List<Account> accounts = new ArrayList<Account>();
		Account account1 = saveTestAccount("Account 1", 100);  
		accounts.add(account1);
		Account account2 = saveTestAccount("Account 2", 200); 
		accounts.add(account2);
		report.setAccounts(accounts);
		CallResult result = categoryDao.addCategory("Category 1");
		assertTrue(result.isGood());
		result = categoryDao.addCategory("Category 2");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		List<Category> categories = (List<Category>)result.getReturnedObject();
		report.setCategories(categories);
		saveTestReport(report);
		report.setName("Updated Report");
		report.setSummaryFields(MONTH);
		report.setTransactionInclusion(TransactionInclusion.DEPOSITS);
		report.setCategoryInclusion(CategoryInclusion.EXCLUDED);
		report.setCategorizedOnly(false);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		report.deleteAccount(account1);
		Account account3 = saveTestAccount("Account 3", 300); 
		report.addAccount(account3);
		report.deleteCategory(categories.get(0));
		result = categoryDao.addCategory("Category 3");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		categories = (List<Category>)result.getReturnedObject();
		report.addCategory(categories.get(2));
		result = summaryDao.updateReport(report);
		assertTrue(result.isGood());
		result = summaryDao.getReports();
		assertTrue(result.isGood());
		List<SummaryReport> reports = (List<SummaryReport>)result.getReturnedObject();
		assertEquals(1, reports.size());
		SummaryReport returnedReport = reports.get(0);
		assertEquals(99, returnedReport.getID());
		assertEquals("Updated Report", returnedReport.getName());
		assertEquals(MONTH, returnedReport.getSummaryFields());
		assertEquals(TransactionInclusion.DEPOSITS, returnedReport.getTransactionInclusion());
		assertEquals(CategoryInclusion.EXCLUDED, returnedReport.getCategoryInclusion());
		assertFalse(returnedReport.isCategorizedOnly());
		assertEquals(DeductibleInclusion.EXCLUDED, returnedReport.getDeductibleInclusion());
		assertEquals(CheckInclusion.EXCLUDED, returnedReport.getCheckInclusion());
		assertEquals(2, returnedReport.getAccounts().size());
		assertEquals(200, returnedReport.getAccounts().get(0).getID());
		assertEquals(300, returnedReport.getAccounts().get(1).getID());
		assertEquals(2, returnedReport.getCategories().size());
		assertEquals(2, returnedReport.getCategories().get(0).getID());
		assertEquals(3, returnedReport.getCategories().get(1).getID());
	}
	
	@Test
	public void testDeleteReport() throws Exception {
		SummaryReport report = new SummaryReport();
		report.setID(1);
		report.setName("Test Report");
		report.setSummaryFields(YEAR);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.INCLUDED);
		report.setDeductibleInclusion(DeductibleInclusion.NONE);
		report.setCheckInclusion(CheckInclusion.NONE);
		List<Account> accounts = new ArrayList<Account>();
		Account account1 = new Account(1);
		accounts.add(account1);
		Account account2 = new Account(2);
		accounts.add(account2);
		report.setAccounts(accounts);
		List<Category> categories = new ArrayList<Category>();
		Category category1 = new Category(1);
		categories.add(category1);
		Category category2 = new Category(2);
		categories.add(category2);
		report.setCategories(categories);
		CallResult result = summaryDao.saveReport(report);
		assertTrue(result.isGood());
		int summaryCount = countSummaryRows(report.getID());
		assertEquals(1, summaryCount);
		int accountCount = countAccountRows(report.getID());
		assertEquals(2, accountCount);
		int categoryCount = countTransactionCategoryRows(report.getID());
		assertEquals(2, categoryCount);
		result = summaryDao.deleteReport(report);
		assertTrue(result.isGood());
		summaryCount = countSummaryRows(report.getID());
		assertEquals(0, summaryCount);
		accountCount = countAccountRows(report.getID());
		assertEquals(0, accountCount);
		categoryCount = countTransactionCategoryRows(report.getID());
		assertEquals(0, categoryCount);
	}

	@SuppressWarnings("unchecked")
	@Test 
	public void testGetSummariesByCategory() throws Exception {
		loadTestData();
		// Select only account 1 transactions
		List<Account> testAccounts = new ArrayList<Account>();
		testAccounts.add(accounts.get(0));
//		printTransactionsByCategory(testAccounts);
		SummaryReport report = new SummaryReport();
		report.setAccounts(testAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20010301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(CATEGORY);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(5, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Appliances", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("Car Insurance", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());

		summary = summaries.get(2);
		assertEquals("Gas", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());

		summary = summaries.get(3);
		assertEquals("Healthcare", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.02), summaries.get(3).getDepositsAmount());
		
		summary = summaries.get(4);
		assertEquals("Uncategorized", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.09), summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testGetSummariesByIncludedCategory() throws Exception {
		loadTestData();
		// Select only account 1 transactions
		List<Account> testAccounts = new ArrayList<Account>();
		testAccounts.add(accounts.get(0));
//		printTransactionsByCategory(testAccounts);
		SummaryReport report = new SummaryReport();
		report.setAccounts(testAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20010301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(CATEGORY);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.INCLUDED);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		List<Category> includedCategories = new ArrayList<Category>();
		includedCategories.add(categories.get(0));
		report.setCategories(includedCategories);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(1, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Appliances", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testGetSummariesByExcludedCategory() throws Exception {
		loadTestData();
		// Select only account 1 transactions
		List<Account> testAccounts = new ArrayList<Account>();
		testAccounts.add(accounts.get(0));
//		printTransactionsByCategory(testAccounts);
		SummaryReport report = new SummaryReport();
		report.setAccounts(testAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20010301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(CATEGORY);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.EXCLUDED);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		List<Category> excludedCategories = new ArrayList<Category>();
		excludedCategories.add(categories.get(1));
		excludedCategories.add(categories.get(2));
		excludedCategories.add(categories.get(3));
		report.setCategories(excludedCategories);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(2, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Appliances", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("Uncategorized", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.09), summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testGetSummariesByExcludedCategoryCategorizedOnly() throws Exception {
		loadTestData();
		// Select only account 1 transactions
		List<Account> testAccounts = new ArrayList<Account>();
		testAccounts.add(accounts.get(0));
//		printTransactionsByCategory(testAccounts);
		SummaryReport report = new SummaryReport();
		report.setAccounts(testAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20010301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(CATEGORY);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategorizedOnly(true);
		report.setCategoryInclusion(CategoryInclusion.EXCLUDED);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		List<Category> excludedCategories = new ArrayList<Category>();
		excludedCategories.add(categories.get(1));
		excludedCategories.add(categories.get(2));
		excludedCategories.add(categories.get(3));
		report.setCategories(excludedCategories);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(1, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Appliances", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesByYear() throws Exception {
		loadTestData();
//		printTransactionsByYear(accounts);
		SummaryReport report = new SummaryReport();
		report.setAccounts(accounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(YEAR);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(3, summaries.size());
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(30.07), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(3), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(175.12), summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(30.07), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(150.07), summary.getDepositsAmount());
		
		summary = summaries.get(2);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(3), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(39.12), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(3), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(175.12), summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesByYearAndCategory() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account1);
		reportAccounts.add(account2);
		report.setAccounts(accounts);
//		printTransactionsByYearAndCategory(reportAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20020301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(YEAR + "," + CATEGORY);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(10, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(2, summary.getSummaryKeys().size());
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("Appliances", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("Car Insurance", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(2);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("Gas", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());
		
		summary = summaries.get(3);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("Healthcare", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.02), summary.getDepositsAmount());
		
		summary = summaries.get(4);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.09), summary.getDepositsAmount());
		
		summary = summaries.get(5);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("Appliances", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(6);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("Car Insurance", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(7);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("Gas", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());
		
		summary = summaries.get(8);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("Healthcare", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.02), summary.getDepositsAmount());
		
		summary = summaries.get(9);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(50.04), summary.getDepositsAmount());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesByMonth() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account1);
		reportAccounts.add(account3);
//		printTransactionsByMonth(reportAccounts);
		report.setAccounts(reportAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030303"));
		report.setDateRange(dateRange);
		report.setSummaryFields(YEAR + "," + MONTH);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(6, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(2, summary.getSummaryKeys().size());
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(10.03), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(100.03), summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(20.04), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(50.04), summary.getDepositsAmount());
		
		summary = summaries.get(2);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.05), summary.getDepositsAmount());
		
		summary = summaries.get(3);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(30.07), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(4);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(100.03), summary.getDepositsAmount());
		
		summary = summaries.get(5);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(9.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.09), summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesByMonthAndCategory() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account3);
//		printTransactionsByMonthAndCategory(reportAccounts);
		report.setAccounts(reportAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030303"));
		report.setDateRange(dateRange);
		report.setSummaryFields(YEAR + "," + MONTH + "," + CATEGORY);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(5, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(3, summary.getSummaryKeys().size());
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Appliances", summary.getSummaryKeys().get(2));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Car Insurance", summary.getSummaryKeys().get(2));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(2);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Gas", summary.getSummaryKeys().get(2));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());
		
		summary = summaries.get(3);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Healthcare", summary.getSummaryKeys().get(2));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.02), summary.getDepositsAmount());
		
		summary = summaries.get(4);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(9.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.09), summary.getDepositsAmount());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesCategorizedOnly() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20030101"));
		dateRange.setEndDate(new TransactionDate("20030303"));
		report.setDateRange(dateRange);
		report.setSummaryFields(YEAR);
		report.setCategorizedOnly(true);
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account3);
		report.setAccounts(reportAccounts);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(1, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(30.07), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(100.03), summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesByDescription() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account3);
//		printTransactionsByDescription(reportAccounts);
		report.setAccounts(reportAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(DESCRIPTION);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(4, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Description 1", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(30.07), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("Description 2", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(2), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(150.07), summary.getDepositsAmount());
		
		summary = summaries.get(2);
		assertEquals("Description 3", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.05), summary.getDepositsAmount());

		summary = summaries.get(3);
		assertEquals("Description 4", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(9.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesDeductibleOnly() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account1);
//		printTransactionsByDescription(reportAccounts);
		report.setAccounts(reportAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(DESCRIPTION);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.INCLUDED);
		report.setCheckInclusion(CheckInclusion.NONE);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(1, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Description 4", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(88.88), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesChecksOnly() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
		List<Account> reportAccounts = new ArrayList<Account>();
		reportAccounts.add(account1);
//		printTransactionsByDescription(reportAccounts);
		report.setAccounts(reportAccounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030301"));
		report.setDateRange(dateRange);
		report.setSummaryFields(DESCRIPTION);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.NONE);
		report.setCheckInclusion(CheckInclusion.INCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(1, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(1, summary.getSummaryKeys().size());
		assertEquals("Description 3", summary.getSummaryKeys().get(0));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(99.99), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetSummariesByMonthCategoryDescription() throws Exception {
		loadTestData();
		SummaryReport report = new SummaryReport();
//		printTransactionsByMonthCategoryDescription(accounts);
		report.setAccounts(accounts);
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(new TransactionDate("20010101"));
		dateRange.setEndDate(new TransactionDate("20030303"));
		report.setDateRange(dateRange);
		report.setSummaryFields(YEAR + "," + MONTH + "," + CATEGORY + "," + DESCRIPTION);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.EXCLUDED);
		report.setCheckInclusion(CheckInclusion.EXCLUDED);
		CallResult result = summaryDao.getSummaries(report);
		assertTrue(result.isGood());
		List<Summary> summaries = (List<Summary>)result.getReturnedObject();
		assertEquals(20, summaries.size());
		
		Summary summary = summaries.get(0);
		assertEquals(4, summary.getSummaryKeys().size());
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Appliances", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(8.01), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(1);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Car Insurance", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(2);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Gas", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());
		
		summary = summaries.get(3);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Healthcare", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.02), summary.getDepositsAmount());

		summary = summaries.get(4);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Appliances", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(20.04), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(5);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(50.04), summary.getDepositsAmount());

		summary = summaries.get(6);
		assertEquals("2001", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals("Description 3", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.05), summary.getDepositsAmount());
		
		summary = summaries.get(7);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Appliances", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(8.01), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(8);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Car Insurance", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(9);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Gas", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());
		
		summary = summaries.get(11);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Appliances", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(20.04), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
		
		summary = summaries.get(12);
		assertEquals("2002", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(50.04), summary.getDepositsAmount());
		
		summary = summaries.get(13);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Appliances", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(2), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(28.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());

		summary = summaries.get(14);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("01", summary.getSummaryKeys().get(1));
		assertEquals("Car Insurance", summary.getSummaryKeys().get(2));
		assertEquals("Description 1", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(2.02), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());

		summary = summaries.get(15);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Gas", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.01), summary.getDepositsAmount());

		summary = summaries.get(16);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("02", summary.getSummaryKeys().get(1));
		assertEquals("Healthcare", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(75.02), summary.getDepositsAmount());

		summary = summaries.get(17);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals("Description 2", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(50.04), summary.getDepositsAmount());

		summary = summaries.get(18);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals("Description 3", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(0), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.ZERO, summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(1), summary.getDepositsCount());
		assertEquals(BigDecimal.valueOf(25.05), summary.getDepositsAmount());

		summary = summaries.get(19);
		assertEquals("2003", summary.getSummaryKeys().get(0));
		assertEquals("03", summary.getSummaryKeys().get(1));
		assertEquals("Uncategorized", summary.getSummaryKeys().get(2));
		assertEquals("Description 4", summary.getSummaryKeys().get(3));
		assertEquals(Long.valueOf(1), summary.getWithdrawalsCount());
		assertEquals(BigDecimal.valueOf(9.05), summary.getWithdrawalsAmount());
		assertEquals(Long.valueOf(0), summary.getDepositsCount());
		assertEquals(BigDecimal.ZERO, summary.getDepositsAmount());
	}
	
	@SuppressWarnings("unused")
	private void printSummaries(List<Summary> summaries) {
		StringBuilder sb = new StringBuilder();
		for (Summary summary : summaries) {
			sb.setLength(0);
			for (String string : summary.getSummaryKeys()) {
				sb.append(string + ",");
			}
			System.out.println("Key: " + sb.toString() + " Withdrawals: " + summary.getWithdrawalsCount() +
				", W_Amount: " + summary.getWithdrawalsAmount() + ", Deposits: " + summary.getDepositsCount() +
				", D_Amount: " + summary.getDepositsAmount());
		}
	}
	
	protected void saveTestReport(SummaryReport report) throws Exception{
		CallResult result = daoImplJdbc.executeUpdateStatement("INSERT INTO " + SUMMARY_STORE_NAME + 
			" values(" + report.getID() + ",'" + report.getName() + "','" + report.getSummaryFields() + "','" +
			report.getTransactionInclusion() + "', '" + report.getCategoryInclusion() + "', " + 
			(report.isCategorizedOnly()?1:0) + ", '" +
			report.getDeductibleInclusion().name() + "', '" + report.getCheckInclusion().name() + "', " + 
			report.getDateRange().getStartDate() + ", " + report.getDateRange().getEndDate() +")"); 
		assertTrue(result.isGood());
		int i=999;
		for (Account account : report.getAccounts()) {
			i++;
			result = daoImplJdbc.executeUpdateStatement("INSERT INTO " + SUMMARY_ACCOUNT_STORE_NAME + " values(" + i + ", " + report.getID() + ", " + account.getID() + ")"); 
			assertTrue(result.isGood());
		}
		i=0;
		for (Category category : report.getCategories()) {
			i++;
			result = daoImplJdbc.executeUpdateStatement("INSERT INTO " + SUMMARY_CATEGORY_STORE_NAME + " values(" + i + ", " + report.getID() + ", " + category.getID() + ")"); 
			assertTrue(result.isGood());
		}
	}
	
	protected int countSummaryRows(int summaryID) throws SQLException {
		CallResult result = daoImplJdbc.executeQueryStatement("Select count(*) from " + SUMMARY_STORE_NAME + " Where " + ID + " = " + summaryID);
		assertTrue(result.isGood());
		ResultSet rs = (ResultSet)result.getReturnedObject();
		int rowCount = rs.getInt(1);
		rs.getStatement().close();
		return rowCount;
	}

	protected int countAccountRows(int summaryID) throws SQLException {
		CallResult result = daoImplJdbc.executeQueryStatement("Select count(*) from " + SUMMARY_ACCOUNT_STORE_NAME + " Where " + SUMMARY_ID + " = " + summaryID);
		assertTrue(result.isGood());
		ResultSet rs = (ResultSet)result.getReturnedObject();
		int rowCount = rs.getInt(1);
		rs.getStatement().close();
		return rowCount;
	}
	
	protected int countTransactionCategoryRows(int summaryID) throws SQLException {
		CallResult result = daoImplJdbc.executeQueryStatement("Select count(*) from " + SUMMARY_CATEGORY_STORE_NAME + " Where " + SUMMARY_ID + " = " + summaryID);
		assertTrue(result.isGood());
		ResultSet rs = (ResultSet)result.getReturnedObject();
		int rowCount = rs.getInt(1);
		rs.getStatement().close();
		return rowCount;
	}
	
	@SuppressWarnings("unchecked")
	private void loadTestData() throws Exception {
		// Make three accounts, each with two withdrawals and two deposits
		account1 = new Account(1, "Bank A", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		assertTrue(accountDao.addAccount(account1).isGood());
		account2 = new Account(2, "Bank B", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		assertTrue(accountDao.addAccount(account2).isGood());
		account3 = new Account(3, "Bank C", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		assertTrue(accountDao.addAccount(account3).isGood());
		CallResult result = accountDao.getAccounts();
		assertTrue(result.isGood());
		accounts = (List<Account>)result.getReturnedObject();
		account1 = accounts.get(0);
		account2 = accounts.get(1);
		account3 = accounts.get(2);
		result = categoryDao.addCategory("Appliances");
		assertTrue(result.isGood());
		result = categoryDao.addCategory("Car Insurance");
		assertTrue(result.isGood());
		result = categoryDao.addCategory("Gas");
		assertTrue(result.isGood());
		result = categoryDao.addCategory("Healthcare");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		categories = (List<Category>)result.getReturnedObject(); 
		List<TransactionCategory> withdrawal1Categories = new ArrayList<TransactionCategory>();
		TransactionCategory withdrawal1Cat1 = makeTransactionCategory(1, categories.get(0), new Money("-8.01"));
		TransactionCategory withdrawal1Cat2 = makeTransactionCategory(2, categories.get(1), new Money("-2.02"));
		withdrawal1Categories.add(withdrawal1Cat1);
		withdrawal1Categories.add(withdrawal1Cat2);
		Transaction withdrawal1 = makeTransaction(new TransactionDate("20010101"), new Money("-10.03"), withdrawal1Categories, "Description 1");
		List<TransactionCategory> withdrawal2Categories = new ArrayList<TransactionCategory>();
		TransactionCategory withdrawal2Cat1 = makeTransactionCategory(3, categories.get(0), new Money("-20.04"));
		withdrawal2Categories.add(withdrawal2Cat1);
		Transaction withdrawal2 = makeTransaction(new TransactionDate("20010201"), new Money("-20.04"), withdrawal2Categories, "Description 1");
		Transaction withdrawal3 = makeTransaction(new TransactionDate("20010201"), new Money("-99.99"), null, "Description 3");
		withdrawal3.setNumber("1234");
		Transaction withdrawal4 = makeTransaction(new TransactionDate("20010201"), new Money("-88.88"), null, "Description 4");
		withdrawal4.setDeductible(true);
		List<TransactionCategory> deposit1Categories = new ArrayList<TransactionCategory>();
		TransactionCategory deposit1Cat1 = makeTransactionCategory(4, categories.get(2), new Money("25.01"));
		TransactionCategory deposit1Cat2 = makeTransactionCategory(5, categories.get(3), new Money("75.02"));
		deposit1Categories.add(deposit1Cat1);
		deposit1Categories.add(deposit1Cat2);
		Transaction deposit1 = makeTransaction(new TransactionDate("20010101"), new Money("100.03"), deposit1Categories, "Description 2");
		Transaction deposit2 = makeTransaction(new TransactionDate("20010201"), new Money("50.04"), null, "Description 2");
		Transaction deposit3 = makeTransaction(new TransactionDate("20010301"), new Money("25.05"), null, "Description 3");
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(withdrawal1);
		transactions.add(withdrawal2);
		transactions.add(withdrawal3);
		transactions.add(withdrawal4);
		transactions.add(deposit1);
		transactions.add(deposit2);
		transactions.add(deposit3);
		result = transactionDao.addTransactions(transactions, account1);
		assertTrue(result.isGood());
		withdrawal1.setDate("20020101");
		withdrawal2.setDate("20020201");
		deposit1.setDate("20020101");
		deposit2.setDate("20020202");
		transactions.remove(deposit3);
		result = transactionDao.addTransactions(transactions, account2);
		assertTrue(result.isGood());
		withdrawal1.setDate("20030101");
		withdrawal2.setDate("20030101");
		deposit1.setDate("20030201");
		deposit2.setDate("20030301");
		deposit3.setDate("20030301");
		transactions.add(deposit3);
		withdrawal3 = makeTransaction(new TransactionDate("20030301"), new Money("-9.05"), null, "Description 4");
		transactions.add(withdrawal3);
		result = transactionDao.addTransactions(transactions, account3);
		assertTrue(result.isGood());
	}
	
	private TransactionCategory makeTransactionCategory(int transactionCategoryID, Category category, Money amount) {
		TransactionCategory tranCat = new TransactionCategory(transactionCategoryID, category.getID(), category.getName(), amount);
		return tranCat;
	}
	
	private Transaction makeTransaction(TransactionDate date, Money amount, List<TransactionCategory> categories, String description) throws Exception {
		Transaction transaction = new Transaction();
		transaction.setDate(date);
		transaction.setAmount(amount);
		transaction.setDescription(description);
		transaction.setMemo("Memo");
		if (categories != null) {
			transaction.setCategories(categories);
		}
		transaction.setBalance(amount);
		return transaction;	
	}
	
	@SuppressWarnings("unused")
	private void printTransactionsByCategory(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		Account account = accounts.get(0);
		transactionDao.getTransactions(account);
		for (Transaction transaction : account.getTransactions()) {
			if (!transaction.getCategories().isEmpty()) {
				for (TransactionCategory category : transaction.getCategories()) {
					Transaction newTrans = new Transaction();
					newTrans.setAmount(category.getAmount());
					List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
					categories.add(category);
					newTrans.initializeCategories(categories);
					transactions.add(newTrans);
				}
			}else {
				List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
				categories.add(new TransactionCategory(0, transaction.getAmount()));
				transaction.initializeCategories(categories);
				transactions.add(transaction);
			}
		}
		Collections.sort(transactions, (t1, t2) -> {
			return t1.getCategories().get(0).getCategoryID() - t2.getCategories().get(0).getCategoryID();
		});
		for (Transaction transaction : transactions) {
			System.out.println("Category: " + transaction.getCategories().get(0).getCategoryID() +
				", Name: " + transaction.getCategories().get(0).getName() +
				", Amount: " + transaction.getCategories().get(0).getAmount().toStringNegative());
		}
	}
	
	@SuppressWarnings("unused")
	private void printTransactionsByDescription(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (Account account : accounts) {
			transactionDao.getTransactions(account);
			for (Transaction transaction : account.getTransactions()) {
				transactions.add(transaction);
			}
		}
		Collections.sort(transactions, (t1, t2) -> {
			return t1.getDescription().compareTo(t2.getDescription());
		});
		for (Transaction transaction : transactions) {
			System.out.println("Description: " + transaction.getDescription() +
					", Amount: " + transaction.getAmount().toStringNegative());
		}
	}

	@SuppressWarnings("unused")
	private void printTransactionsByMonthCategoryDescription(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (Account account : accounts) {
			transactionDao.getTransactions(account);
			for (Transaction transaction : account.getTransactions()) {
				if (!transaction.getCategories().isEmpty()) {
					for (TransactionCategory category : transaction.getCategories()) {
						Transaction newTrans = new Transaction();
						newTrans.setDate(transaction.getDate());
						newTrans.setAmount(category.getAmount());
						newTrans.setDescription(transaction.getDescription());
						List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
						categories.add(category);
						newTrans.initializeCategories(categories);
						transactions.add(newTrans);
					}
				}else {
					List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
					categories.add(new TransactionCategory(0, transaction.getAmount()));
					transaction.initializeCategories(categories);
					transactions.add(transaction);
				}
			}
		}
		SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
		SimpleDateFormat formatMonth = new SimpleDateFormat("MM");
		Collections.sort(transactions, (t1, t2) -> {
			String sortKey1 = formatYear.format(t1.getDate()) + formatMonth.format(t1.getDate()) + 
				String.format("%02d", t1.getCategories().get(0).getCategoryID()) + t1.getDescription();
			String sortKey2 = formatYear.format(t2.getDate()) + formatMonth.format(t2.getDate()) + 
				String.format("%02d", t1.getCategories().get(0).getCategoryID()) + t2.getDescription();
			return sortKey1.compareTo(sortKey2);
		});
		for (Transaction transaction : transactions) {
			System.out.println("Year: " + formatYear.format(transaction.getDate()) + ", Month: " + formatMonth.format(transaction.getDate()) +
				", Category: " + transaction.getCategories().get(0).getCategoryID() +
				", Name: " + transaction.getCategories().get(0).getName() +
				", Description: " + transaction.getDescription() +
				", Amount: " + transaction.getCategories().get(0).getAmount().toStringNegative());
		}
	}

	
	@SuppressWarnings("unused")
	private void printTransactionsByYear(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (Account account : accounts) {
			transactionDao.getTransactions(account);
			for (Transaction transaction : account.getTransactions()) {
				transaction.setMemo(String.valueOf(account.getID()));
				transactions.add(transaction);
			}
		}
		SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
		Collections.sort(transactions, (t1, t2) -> {
			return (formatYear.format(t1.getDate()) + t1.getAmount().getValue()).compareTo(formatYear.format(t2.getDate()) + t2.getAmount().getValue());
		});
		for (Transaction transaction : transactions) {
			System.out.println("Year: " + formatYear.format(transaction.getDate()) + 
				", Amount: " + transaction.getAmount().toStringNegative());
		}
	}
	
	@SuppressWarnings("unused")
	private void printTransactionsByYearAndCategory(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (int i = 0; i < 2; i++) {
			Account account = accounts.get(i);
			transactionDao.getTransactions(account);
			for (Transaction transaction : account.getTransactions()) {
				if (!transaction.getCategories().isEmpty()) {
					for (TransactionCategory category : transaction.getCategories()) {
						Transaction newTrans = new Transaction();
						newTrans.setDate(transaction.getDate());
						newTrans.setAmount(category.getAmount());
						List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
						categories.add(category);
						newTrans.initializeCategories(categories);
						transactions.add(newTrans);
					}
				}else {
					List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
					categories.add(new TransactionCategory(0, transaction.getAmount()));
					transaction.initializeCategories(categories);
					transactions.add(transaction);
				}
			}
		}
		SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
		Collections.sort(transactions, (t1, t2) -> {
			String sort1 = formatYear.format(t1.getDate()) + String.format("%02d", t1.getCategories().get(0).getCategoryID()) + t1.getAmount().getValue();
			String sort2 = formatYear.format(t2.getDate()) + String.format("%02d", t2.getCategories().get(0).getCategoryID()) + t2.getAmount().getValue();
			return (sort1.compareTo(sort2));
		});
		for (Transaction transaction : transactions) {
			System.out.println("Year: " + formatYear.format(transaction.getDate()) +
				", Category: " + transaction.getCategories().get(0).getCategoryID() +
				", Name: " + transaction.getCategories().get(0).getName() +
				", Amount: " + transaction.getCategories().get(0).getAmount().toStringNegative());
		}
	}

	@SuppressWarnings("unused")
	private void printTransactionsByMonth(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (Account account : accounts) {
			transactionDao.getTransactions(account);
			for (Transaction transaction : account.getTransactions()) {
				transactions.add(transaction);
			}
		}
		SimpleDateFormat formatYearMonth = new SimpleDateFormat("yyyyMM");
		Collections.sort(transactions, (t1, t2) -> {
			return (formatYearMonth.format(t1.getDate()) + t1.getAmount().getValue()).compareTo(formatYearMonth.format(t2.getDate()) + t2.getAmount().getValue());
		});
		for (Transaction transaction : transactions) {
			System.out.println("YearMonth: " + formatYearMonth.format(transaction.getDate()) + 
				", Amount: " + transaction.getAmount().toStringNegative());
		}
	}

	@SuppressWarnings("unused")
	private void printTransactionsByMonthAndCategory(List<Account> accounts) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (Account account : accounts) {
			transactionDao.getTransactions(account);
			for (Transaction transaction : account.getTransactions()) {
				if (!transaction.getCategories().isEmpty()) {
					for (TransactionCategory category : transaction.getCategories()) {
						Transaction newTrans = new Transaction();
						newTrans.setDate(transaction.getDate());
						newTrans.setAmount(category.getAmount());
						List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
						categories.add(category);
						newTrans.initializeCategories(categories);
						transactions.add(newTrans);
					}
				}else {
					List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
					categories.add(new TransactionCategory(0, transaction.getAmount()));
					transaction.initializeCategories(categories);
					transactions.add(transaction);
				}
			}
		}
		SimpleDateFormat formatYearMonth = new SimpleDateFormat("yyyyMM");
		Collections.sort(transactions, (t1, t2) -> {
			String c1 = String.format("%02d", t1.getCategories().get(0).getCategoryID());
			String c2 = String.format("%02d", t2.getCategories().get(0).getCategoryID());
			return (formatYearMonth.format(t1.getDate()) + c1 + t1.getAmount().getValue()).compareTo(formatYearMonth.format(t2.getDate()) + c2 + t2.getAmount().getValue());
		});
		for (Transaction transaction : transactions) {
			System.out.println("YearMonth: " + formatYearMonth.format(transaction.getDate()) +
				", Category: " + transaction.getCategories().get(0).getCategoryID() +
				", Name: " + transaction.getCategories().get(0).getName() +
				", Amount: " + transaction.getAmount().toStringNegative());
		}
	}

}
