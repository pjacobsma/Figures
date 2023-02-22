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
package org.bluewindows.figures.service.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.dao.impl.sqlite.test.AbstractDaoImplSqliteTestCase;
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
import org.bluewindows.figures.service.DisplayService;
import org.bluewindows.figures.service.ImportService;
import org.bluewindows.figures.service.PersistenceService;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.MaintenanceServiceImpl;
import org.bluewindows.figures.service.impl.PersistenceServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MaintenanceServiceImplTest extends AbstractDaoImplSqliteTestCase {
	
	@Mock
	ImportService importService;
	
	@Mock
	DisplayService displayService;
	
	@Mock
	PersistenceService mockPersistenceService;
	
	@Before
	public void setUp() {
		ServiceFactory.getInstance().setImportSvc(importService);
		ServiceFactory.getInstance().setMaintenanceSvc(MaintenanceServiceImpl.getInstance());
		ServiceFactory.getInstance().setDisplaySvc(displayService);
	}
	
	@Test
	public void testImportOneFile() throws Exception {
		Account account = makeTestAccount();
		makeTestFilter(account);
		List<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction1 = makeTestTransaction("20200101", "1.00");
		Transaction transaction2 = makeTestTransaction("20200101", "2.00");
		transactions.add(transaction1);
		transactions.add(transaction2);
		CallResult importResult = new CallResult();
		importResult.setReturnedObject(transactions);
		File file = new File("");
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		assertTrue(result.isGood());
		assertEquals(2, account.getTransactionCount());
		// Transactions are retrieved in descending order
		assertEquals(new Money(2.00), account.getTransactions().get(0).getAmount());
		assertEquals(new Money(3.00), account.getTransactions().get(0).getBalance());
		assertEquals("Filtered Description", account.getTransactions().get(0).getDescription());
		assertEquals(new Money(1.00), account.getTransactions().get(1).getAmount());
		assertEquals(new Money(1.00), account.getTransactions().get(1).getBalance());
		assertEquals(transaction1.getDate(), account.getLastLoadedDate());
	}
	
	@Test
	public void testImportSameFileTwice() throws Exception {
		Account account = makeTestAccount();
		List<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction1 = makeTestTransaction("20200101", "1.00");
		transactions.add(transaction1);
		CallResult importResult = new CallResult();
		importResult.setReturnedObject(transactions);
		File file = new File("");
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult).thenReturn(importResult);
		CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Integer> importCounts = (List<Integer>)result.getReturnedObject();
		assertEquals(0, importCounts.get(0).intValue());
		assertEquals(1, importCounts.get(1).intValue());
	}
	
	@Test
	public void testImportTwoFilesInChronologicalOrder() throws Exception {
		Account account = makeTestAccount();
		CallResult importResult = new CallResult();
		File file = new File("");
		// File 1
		List<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction1 = makeTestTransaction("20200101", "1.00");
		Transaction transaction2 = makeTestTransaction("20200101", "2.00");
		transactions.add(transaction1);
		transactions.add(transaction2);
		importResult.setReturnedObject(transactions);
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		
		// File 2
		transactions = new ArrayList<Transaction>();
		Transaction transaction3 = makeTestTransaction("20200102", "3.00");
		Transaction transaction4 = makeTestTransaction("20200102", "4.00");
		transactions.add(transaction3);
		transactions.add(transaction4);
		importResult.setReturnedObject(transactions);
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		assertTrue(result.isGood());
		assertEquals(4, account.getTransactionCount());
		// Transactions are retrieved in descending order
		assertEquals(new Money(4.00), account.getTransactions().get(0).getAmount());
		assertEquals(new Money(10.00), account.getTransactions().get(0).getBalance());
		assertEquals(new Money(3.00), account.getTransactions().get(1).getAmount());
		assertEquals(new Money(6.00), account.getTransactions().get(1).getBalance());
		assertEquals(new Money(2.00), account.getTransactions().get(2).getAmount());
		assertEquals(new Money(3.00), account.getTransactions().get(2).getBalance());
		assertEquals(new Money(1.00), account.getTransactions().get(3).getAmount());
		assertEquals(new Money(1.00), account.getTransactions().get(3).getBalance());
		assertEquals(transaction1.getDate(), account.getLastLoadedDate());
	}
	
	@Test
	public void testImportTwoFilesInReverseChronologicalOrder() throws Exception {
		Account account = makeTestAccount();
		CallResult importResult = new CallResult();
		File file = new File("");
		// File 1
		List<Transaction> transactions1 = new ArrayList<Transaction>();
		Transaction transaction1 = makeTestTransaction("20200102", "3.00");
		Transaction transaction2 = makeTestTransaction("20200102", "4.00");
		transactions1.add(transaction1);
		transactions1.add(transaction2);
		importResult.setReturnedObject(transactions1);
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		
		// File 2
		List<Transaction> transactions2 = new ArrayList<Transaction>();
		Transaction transaction3 = makeTestTransaction("20200101", "1.00");
		Transaction transaction4 = makeTestTransaction("20200101", "2.00");
		transactions2.add(transaction3);
		transactions2.add(transaction4);
		importResult.setReturnedObject(transactions2);
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		assertTrue(result.isGood());
		assertEquals(4, account.getTransactionCount());
		// Transactions are retrieved in descending order
		assertEquals(new Money(4.00), account.getTransactions().get(0).getAmount());
		assertEquals(new Money(10.00), account.getTransactions().get(0).getBalance());
		assertEquals(new Money(3.00), account.getTransactions().get(1).getAmount());
		assertEquals(new Money(6.00), account.getTransactions().get(1).getBalance());
		assertEquals(new Money(2.00), account.getTransactions().get(2).getAmount());
		assertEquals(new Money(3.00), account.getTransactions().get(2).getBalance());
		assertEquals(new Money(1.00), account.getTransactions().get(3).getAmount());
		assertEquals(new Money(1.00), account.getTransactions().get(3).getBalance());
		assertEquals(transaction3.getDate(), account.getLastLoadedDate());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testImportTwoFilesWithOverlappingDates() throws Exception {
		Account account = makeTestAccount();
		CallResult importResult = new CallResult();
		File file = new File("");
		// File 1
		List<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction1 = makeTestTransaction("20200101", "1.00");
		Transaction transaction2 = makeTestTransaction("20200102", "2.00");
		Transaction transaction3 = makeTestTransaction("20200103", "3.00");
		transactions.add(transaction1);
		transactions.add(transaction2);
		transactions.add(transaction3);
		importResult.setReturnedObject(transactions);
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		List<Integer> importCounts = (List<Integer>)result.getReturnedObject();
		assertEquals(3, importCounts.get(0).intValue());
		assertEquals(0, importCounts.get(1).intValue());
		
		// File 2
		transactions = new ArrayList<Transaction>();
		transaction1 = makeTestTransaction("20191231", "31.00");
		transaction2 = makeTestTransaction("20200102", "20.00");
		transaction3 = makeTestTransaction("20200105", "5.00");
		transactions.add(transaction1);
		transactions.add(transaction2);
		transactions.add(transaction3);
		importResult.setReturnedObject(transactions);
		Mockito.when(importService.importFile(file, account)).thenReturn(importResult);
		result = ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
		assertTrue(result.isGood());
		importCounts = (List<Integer>)result.getReturnedObject();
		assertEquals(2, importCounts.get(0).intValue());
		assertEquals(1, importCounts.get(1).intValue());
		
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		assertTrue(result.isGood());
		assertEquals(5, account.getTransactionCount());
		// Transactions are retrieved in descending order
		assertEquals(new Money(5.00), account.getTransactions().get(0).getAmount());
		assertEquals(new Money(42.00), account.getTransactions().get(0).getBalance());
		assertEquals(new Money(3.00), account.getTransactions().get(1).getAmount());
		assertEquals(new Money(37.00), account.getTransactions().get(1).getBalance());
		assertEquals(new Money(2.00), account.getTransactions().get(2).getAmount());
		assertEquals(new Money(34.00), account.getTransactions().get(2).getBalance());
		assertEquals(new Money(1.00), account.getTransactions().get(3).getAmount());
		assertEquals(new Money(32.00), account.getTransactions().get(3).getBalance());
		assertEquals(new Money(31.00), account.getTransactions().get(4).getAmount());
		assertEquals(new Money(31.00), account.getTransactions().get(4).getBalance());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAccount() throws Exception {
		makeTestAccount();
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getAccounts();
		assertTrue(result.isGood());
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		result = ServiceFactory.getInstance().getPersistenceSvc().addCategory("Test Category");
		assertTrue(result.isGood());
		result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		assertTrue(result.isGood());
		List<Category> allCategories = (List<Category>)result.getReturnedObject();
		Account account = accounts.get(0);
		int accountID = account.getID();
		List<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction1 = makeTestTransaction("20200102", "3.00");
		transaction1.setBalance(new Money("3.00"));
		transaction1.setCategory(new TransactionCategory(allCategories.get(0).getID(), transaction1.getAmount()));
		Transaction transaction2 = makeTestTransaction("20200102", "4.00");
		transaction2.setBalance(new Money("7.00"));
		transaction2.setCategory(new TransactionCategory(allCategories.get(0).getID(), transaction2.getAmount()));
		transactions.add(transaction1);
		transactions.add(transaction2);
		result = ServiceFactory.getInstance().getPersistenceSvc().addTransactions(transactions, account);
		assertTrue(result.isGood());
		result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		assertTrue(result.isGood());
		assertEquals(2, account.getTransactionCount());
		int transaction1ID = account.getTransactions().get(0).getID();
		int transaction2ID = account.getTransactions().get(1).getID();
		assertEquals(1, account.getTransactions().get(0).getCategories().size());
		assertEquals(1, account.getTransactions().get(1).getCategories().size());
		SummaryReport report = new SummaryReport();
		report.addAccount(account);
		report.setName("Test Report");
		DateRange dateRange = new DateRange(new TransactionDate("20200101"), new TransactionDate("20201231"));
		report.setDateRange(dateRange);
		report.setTransactionInclusion(TransactionInclusion.ALL);
		report.setCategoryInclusion(CategoryInclusion.ALL);
		report.setDeductibleInclusion(DeductibleInclusion.NONE);
		report.setCheckInclusion(CheckInclusion.NONE);
		result = ServiceFactory.getInstance().getPersistenceSvc().saveReport(report);
		assertTrue(result.isGood());
		result = ServiceFactory.getInstance().getPersistenceSvc().getReports();
		assertTrue(result.isGood());
		int summaryID = ((List<SummaryReport>)result.getReturnedObject()).get(0).getID();
		assertEquals(1, countAccountRows(accountID));
		assertEquals(1, countSummaryAccountRows(summaryID, accountID));
		assertEquals(1, countTransactionRows(transaction1ID));
		assertEquals(1, countTransactionRows(transaction2ID));
		assertEquals(1, countTransactionCategoryRows(transaction1ID));
		assertEquals(1, countTransactionCategoryRows(transaction2ID));
		result = ServiceFactory.getInstance().getMaintenanceSvc().deleteAccount(account);
		assertTrue(result.isGood());
		assertEquals(0, countAccountRows(accountID));
		assertEquals(0, countSummaryAccountRows(summaryID, accountID));
		assertEquals(0, countTransactionRows(transaction1ID));
		assertEquals(0, countTransactionRows(transaction2ID));
		assertEquals(0, countTransactionCategoryRows(transaction1ID));
		assertEquals(0, countTransactionCategoryRows(transaction2ID));
	}
	
	@Test
	public void testResequenceFilters() throws Exception {
		ServiceFactory.getInstance().setPersistenceSvc(mockPersistenceService);
		Mockito.when(mockPersistenceService.updateFilter(any(Filter.class))).thenReturn(new CallResult());
		FilterSet filterSet = makeTestFilters();
		
		// Call with bad filter ID
		CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(filterSet, 5, 1);
		assertTrue(result.isBad());

		// Set first filter to be last
		result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(filterSet, 1, 4);
		assertTrue(result.isGood());
		assertSequence(filterSet, 2, 1);
		assertSequence(filterSet, 3, 2);
		assertSequence(filterSet, 4, 3);
		assertSequence(filterSet, 1, 4);
		
		// Set last filter to be first
		filterSet = makeTestFilters();
		result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(filterSet, 4, 1);
		assertTrue(result.isGood());
		assertSequence(filterSet, 4, 1);
		assertSequence(filterSet, 1, 2);
		assertSequence(filterSet, 2, 3);
		assertSequence(filterSet, 3, 4);
		
		// Flip middle two filters
		filterSet = makeTestFilters();
		result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(filterSet, 2, 3);
		assertTrue(result.isGood());
		assertSequence(filterSet, 1, 1);
		assertSequence(filterSet, 3, 2);
		assertSequence(filterSet, 2, 3);
		assertSequence(filterSet, 4, 4);
		
		// Use new sequences outside existing range
		filterSet = makeTestFilters();
		result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(filterSet, 4, 0);
		assertTrue(result.isGood());
		assertSequence(filterSet, 4, 1);
		assertSequence(filterSet, 1, 2);
		assertSequence(filterSet, 2, 3);
		assertSequence(filterSet, 3, 4);

		filterSet = makeTestFilters();
		result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(filterSet, 1, 5);
		assertTrue(result.isGood());
		assertSequence(filterSet, 2, 1);
		assertSequence(filterSet, 3, 2);
		assertSequence(filterSet, 4, 3);
		assertSequence(filterSet, 1, 4);

		ServiceFactory.getInstance().setPersistenceSvc(PersistenceServiceImpl.getInstance());
	}
	
	private void assertSequence(FilterSet filterSet, int id, Integer expectedSequence) {
		boolean filterFound = false;
		for (Filter filter : filterSet.getFilters()) {
			if (filter.getID() == id) {
				filterFound = true;
				assertEquals("Filter with id " + id + ", sequence ", expectedSequence, filter.getSequence());
			}
		}
		assertTrue("Filter with id " + id + " not found", filterFound);
	}
	
	private FilterSet makeTestFilters() {
		FilterSet filterSet = new FilterSet(1, "TestFilters", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(),
				FilterResult.REPLACE_DESCRIPTION.toString());
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter1 = new Filter(1, 1, 1, FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), "Desc",
				FilterResult.REPLACE_DESCRIPTION.toString(), "Filtered Description", false, Integer.valueOf(0));
		filters.add(filter1);
		Filter filter2 = new Filter(2, 1, 2, FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), "Desc",
				FilterResult.REPLACE_DESCRIPTION.toString(), "Filtered Description", false, Integer.valueOf(0));
		filters.add(filter2);
		Filter filter3 = new Filter(3, 1, 3, FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), "Desc",
				FilterResult.REPLACE_DESCRIPTION.toString(), "Filtered Description", false, Integer.valueOf(0));
		filters.add(filter3);
		Filter filter4 = new Filter(4, 1, 4, FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), "Desc",
				FilterResult.REPLACE_DESCRIPTION.toString(), "Filtered Description", false, Integer.valueOf(0));
		filters.add(filter4);
		filterSet.setFilters(filters);
		return filterSet;
	}

	
	private Account makeTestAccount() {
		Account account = new Account(1, "Checking", AccountType.CHECKING, null);
		account.setLastLoadDate(new TransactionDate());
		account.setLastFilteredDate(new TransactionDate());
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().addAccount(account);
		assertTrue(result.isGood());
		result = ServiceFactory.getInstance().getPersistenceSvc().getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		Account savedAccount = accounts.get(0);
		assertEquals(0, savedAccount.getTransactions().size());
		assertEquals(Money.ZERO, savedAccount.getInitialBalance());
		return savedAccount;
	}
	
	private Transaction makeTestTransaction(String date, String amount) throws Exception {
		Transaction transaction = new Transaction();
		transaction.setDate(date);
		transaction.setAmount(new Money(amount));
		transaction.setOriginalDescription("Description");
		transaction.setCategory(new TransactionCategory(1, transaction.getAmount()));
		return transaction;
	}
	
	private void makeTestFilter(Account account) {
		FilterSet filterSet = new FilterSet(1, "TestFilters", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(),
			FilterResult.REPLACE_DESCRIPTION.toString());
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().addFilterSet(filterSet);
		assertTrue(result.isGood());
		account.setFilterSet(filterSet);
		result = ServiceFactory.getInstance().getPersistenceSvc().updateAccount(account);
		assertTrue(result.isGood());
		Filter filter = new Filter(1, 1, 1, FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), "Desc",
			FilterResult.REPLACE_DESCRIPTION.toString(), "Filtered Description", false, Integer.valueOf(0));
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(filter);
		filterSet.setFilters(filters);
		result = ServiceFactory.getInstance().getPersistenceSvc().addFilter(filter);
		assertTrue(result.isGood());
	}
	


}
