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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.dao.impl.sqlite.AccountDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.TransactionDaoImplSqlite;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.domain.persistence.Persistence;
import org.bluewindows.figures.enums.AccountType;
import org.junit.Before;
import org.junit.Test;

public class TransactionDaoImplSqliteTest extends AbstractDaoImplSqliteTestCase {
	
	private TransactionDaoImplSqlite transactionDao;
	private AccountDaoImplSqlite accountDao;

	@Before
	public void before(){
		super.before();
		transactionDao = (TransactionDaoImplSqlite)persistence.getTransactionDao();
		accountDao = (AccountDaoImplSqlite)persistence.getAccountDao();
	}
	
	@Test
	public void testGetTransactionDateRange() throws Exception {
		Account account = new Account(0, "TestChecking", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		accountDao.addAccount(account);
		CallResult result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		account = accounts.get(0);
		Transaction transaction1 = makeTestTransaction();
		transaction1.setDate("20010101");
		saveTestTransaction(account, transaction1);
		Transaction transaction2 = makeTestTransaction();
		transaction2.setID(2);
		transaction2.setDate("20011231");
		saveTestTransaction(account, transaction2);
		result = transactionDao.getTransactionDateRange(account);
		assertTrue(result.isGood());
		DateRange dateRange = (DateRange)result.getReturnedObject();
		assertEquals("01/01/2001", dateRange.getStartDate().toString());
		assertEquals("12/31/2001", dateRange.getEndDate().toString());
	}
	
	@Test
	public void testGetTransactions() throws Exception {
		Account account1 = new Account(3, "TestChecking", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		CallResult result = accountDao.addAccount(account1);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		Account account2 = new Account(4, "TestSavings", AccountType.SAVINGS, new FilterSet("Savings"), new Money("0.00"), new TransactionDate("20010101"));
		result = accountDao.addAccount(account2);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		account1 = accounts.get(0);
		assertEquals(1, account1.getID());
		Transaction transaction1 = makeTestTransaction();
		transaction1.setID(1);
		transaction1.setDate("20011231");
		saveTestTransaction(account1, transaction1);
		Transaction transaction2 = makeTestTransaction();
		transaction2.setID(2);
		transaction2.setAmount(new Money("234.56"));
		transaction2.setDate("20011230");
		saveTestTransaction(account1, transaction2);
		account2 = accounts.get(1);
		assertEquals(2, account2.getID());
		Transaction transaction3 = makeTestTransaction();
		transaction3.setDate("20011231");
		transaction3.setID(3);
		saveTestTransaction(account2, transaction3);
		result = transactionDao.getTransactions(account1);
		assertTrue(result.isGood());
		assertEquals(2, account1.getTransactionCount());
		assertEquals(transaction1, account1.getTransactions().get(0));
		assertEquals(transaction2, account1.getTransactions().get(1));
	}

	@Test
	public void testAddTransactions() throws Exception {
		Account account = new Account(1, "TestChecking", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		accountDao.addAccount(account);
		CallResult result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		account = accounts.get(0);
		Transaction transaction1 = makeTestTransaction();
		transaction1.setID(1);
		transaction1.setDate("20011231");
		Transaction transaction2 = makeTestTransaction();
		transaction2.setID(2);
		transaction2.setAmount(new Money("234.56"));
		transaction2.setDate("20011230");
		List<Transaction> transactionList = new ArrayList<Transaction>();
		transactionList.add(transaction1);
		transactionList.add(transaction2);
		result = transactionDao.addTransactions(transactionList, account);
		assertTrue(result.isGood());
		assertEquals(0, account.getTransactionCount());
		result = transactionDao.getTransactions(account);
		assertTrue(result.isGood());
		assertEquals(2, account.getTransactionCount());
		assertEquals(transaction1, account.getTransactions().get(0));
		assertEquals(transaction2, account.getTransactions().get(1));
	}

	@Test
	public void testGetDistinctValues() throws Exception {
		Account account = new Account(1, "TestChecking", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"),
			new TransactionDate("20010101"));
		accountDao.addAccount(account);
		CallResult result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		assertEquals(1, accounts.size());
		account = accounts.get(0);
		// Included transactions
		Transaction transaction1 = makeTestTransaction();
		transaction1.setNumber("");
		transaction1.setDate(new TransactionDate("20010102"));
		transaction1.setDescription("Description 1");
		transaction1.setOriginalDescription("Description 1");
		transaction1.setMemo("Memo 1");
		transaction1.setOriginalMemo("Memo 1");
		transaction1.setCategory(new TransactionCategory(0, transaction1.getAmount()));
		Transaction transaction2 = makeTestTransaction();
		transaction2.setNumber("");
		transaction2.setDate(new TransactionDate("20010102"));
		transaction2.setDescription("Description 1");
		transaction2.setOriginalDescription("Description 1");
		transaction2.setMemo("Memo 1");
		transaction2.setOriginalMemo("Memo 2");
		transaction2.setCategory(new TransactionCategory(0, transaction1.getAmount()));
		// Excluded transactions
		// This transaction will be excluded because it has a category assigned
		Transaction transaction3 = makeTestTransaction();
		transaction3.setNumber("");
		transaction3.setDate(new TransactionDate("20010102"));
		transaction3.setDescription("Description 3");
		transaction3.setOriginalDescription("Description 3");
		transaction3.setMemo("Memo 3");
		transaction3.setOriginalMemo("Memo 3");
		// This transaction will be excluded because its date is prior to the last loaded date
		// on the account
		Transaction transaction4 = makeTestTransaction();
		transaction4.setNumber("");
		transaction4.setDate("19991231");
		transaction4.setDescription("Description 4");
		transaction4.setOriginalDescription("Description 4");
		transaction4.setMemo("Memo 4");
		transaction4.setOriginalMemo("Memo 4");
		transaction4.setCategory(new TransactionCategory(0, transaction1.getAmount()));
		// This transaction will be excluded because it has a number
		Transaction transaction5 = makeTestTransaction();
		transaction5.setNumber("1234");
		transaction5.setDate("19991231");
		transaction5.setDescription("Description 5");
		transaction5.setOriginalDescription("Description 5");
		transaction5.setMemo("Memo 5");
		transaction5.setOriginalMemo("Memo 5");
		transaction5.setCategory(new TransactionCategory(0, transaction1.getAmount()));
		// Add deposit
		Transaction transaction6 = makeTestTransaction();
		transaction6.setNumber("");
		transaction6.setDate(new TransactionDate("20010102"));
		transaction6.setDescription("Description 6");
		transaction6.setOriginalDescription("Description 6");
		transaction6.setMemo("Memo 6");
		transaction6.setOriginalMemo("Memo 6");
		transaction6.setAmount(new Money("123.45"));
		transaction6.setCategory(new TransactionCategory(0, transaction1.getAmount()));

		List<Transaction> transactionList = new ArrayList<Transaction>();
		transactionList.add(transaction1);
		transactionList.add(transaction2);
		transactionList.add(transaction3);
		transactionList.add(transaction4);
		transactionList.add(transaction5);
		transactionList.add(transaction6);
		result = transactionDao.addTransactions(transactionList, account);
		assertTrue(result.isGood());
		result = transactionDao.getDistinctValues(account, Persistence.DESCRIPTION, true, false, false);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<String> returnedDescStrings1 = (List<String>)result.getReturnedObject();
		assertEquals(2, returnedDescStrings1.size());
		assertEquals("Description 1", returnedDescStrings1.get(0));
		assertEquals("Description 6", returnedDescStrings1.get(1));
		result = transactionDao.getDistinctValues(account, Persistence.MEMO, true, false, false);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<String> returnedMemoStrings1 = (List<String>)result.getReturnedObject();
		assertEquals(2, returnedMemoStrings1.size());
		assertEquals("Memo 1", returnedMemoStrings1.get(0));
		assertEquals("Memo 6", returnedMemoStrings1.get(1));
		
		// Now included the older transaction (transaction4)
		result = transactionDao.getDistinctValues(account, Persistence.DESCRIPTION, false, false, false);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<String> returnedDescStrings2 = (List<String>)result.getReturnedObject();
		assertEquals(3, returnedDescStrings2.size());
		assertEquals("Description 1", returnedDescStrings2.get(0));
		assertEquals("Description 4", returnedDescStrings2.get(1));
		assertEquals("Description 6", returnedDescStrings2.get(2));
		result = transactionDao.getDistinctValues(account, Persistence.MEMO, false, false, false);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<String> returnedMemoStrings2 = (List<String>)result.getReturnedObject();
		assertEquals(3, returnedMemoStrings2.size());
		assertEquals("Memo 1", returnedMemoStrings2.get(0));
		assertEquals("Memo 4", returnedMemoStrings2.get(1));
		assertEquals("Memo 6", returnedMemoStrings2.get(2));
		
		// Deposits only
		result = transactionDao.getDistinctValues(account, Persistence.DESCRIPTION, true, true, false);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<String> returnedDescStrings3 = (List<String>)result.getReturnedObject();
		assertEquals(1, returnedDescStrings3.size());
		assertEquals("Description 6", returnedDescStrings3.get(0));
		
		// Withdrawals only
		result = transactionDao.getDistinctValues(account, Persistence.DESCRIPTION, true, false, true);
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<String> returnedDescStrings4 = (List<String>)result.getReturnedObject();
		assertEquals(1, returnedDescStrings4.size());
		assertEquals("Description 1", returnedDescStrings4.get(0));
	}

	@Test
	public void testUpdateTransaction() throws Exception {
		Account account = new Account(0, "TestChecking", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		CallResult result = accountDao.addAccount(account);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		account = accounts.get(0);
		Transaction transaction = makeTestTransaction();
		List<Transaction> transactionList = new ArrayList<Transaction>();
		transactionList.add(transaction);
		result = transactionDao.addTransactions(transactionList, account);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		result = transactionDao.getTransactions(account);
		assertTrue(result.isGood());
		transaction = account.getTransactions().get(0);
		transaction.setNumber("1234");
		transaction.setDate("19690724");
		transaction.setAmount(new Money("456.78"));
		transaction.setDescription("New Description");
		transaction.setUserChangedDesc(true);
		transaction.setMemo("New Memo");
		transaction.setUserChangedMemo(true);
		transaction.setDeductible(true);
		transaction.setUserChangedDeductible(true);
		transaction.setBalance(new Money("89.98"));
		List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
		categories.add(new TransactionCategory(1, 9, "Gifts", new Money("23.45")));
		categories.add(new TransactionCategory(2, 10, "Groceries", new Money("100.00")));
		transaction.setCategories(categories);
		transaction.setUserChangedCategory(true);
		transactionDao.updateTransaction(transaction);
		Transaction updatedTrans = account.getTransactions().get(0);
		// The updateTransaction method will not change the ID, Number, Date, or Amount
		assertEquals(transaction.getID(), updatedTrans.getID());
		assertEquals(transaction.getNumber(), updatedTrans.getNumber());
		assertEquals(transaction.getDate(), updatedTrans.getDate());
		assertEquals(transaction.getAmount(), updatedTrans.getAmount());
		assertEquals("New Description", updatedTrans.getDescription());
		assertTrue(updatedTrans.isUserChangedDeductible());
		assertEquals("New Memo", transaction.getMemo());
		assertTrue(updatedTrans.isUserChangedMemo());
		assertTrue(updatedTrans.isDeductible());
		assertTrue(updatedTrans.isUserChangedDeductible());
		categories = transaction.getAddedCategories();
		assertEquals(2, categories.size());
		assertEquals("Gifts", categories.get(0).getName());
		assertEquals(new Money("23.45"), categories.get(0).getAmount());
		assertEquals("Groceries", categories.get(1).getName());
		assertEquals(new Money("100.00"), categories.get(1).getAmount());
		assertTrue(updatedTrans.isUserChangedCategory());
		assertEquals(new Money("89.98"), updatedTrans.getBalance());
	}
	
	@Test
	public void testDeleteTransactions() throws Exception {
		Account account1 = new Account(1, "TestChecking", AccountType.CHECKING, new FilterSet("Checking"), new Money("0.00"), new TransactionDate("20010101"));
		CallResult result = accountDao.addAccount(account1);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		Account account2 = new Account(2, "TestSavings", AccountType.SAVINGS, new FilterSet("Savings"), new Money("0.00"), new TransactionDate("20010101"));
		result = accountDao.addAccount(account2);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		assertEquals(2, accounts.size());
		account1 = accounts.get(0);
		assertEquals(1, account1.getID());
		Transaction transaction1 = makeTestTransaction();
		transaction1.setID(1);
		transaction1.setDate("20011231");
		transaction1.setCategory(new TransactionCategory(++transactionCategoryID, 1, "Appliances", transaction1.getAmount()));
		saveTestTransaction(account1, transaction1);
		Transaction transaction2 = makeTestTransaction();
		transaction2.setID(2);
		transaction2.setAmount(new Money("234.56"));
		transaction2.setDate("20011230");
		transaction2.setCategory(new TransactionCategory(++transactionCategoryID, 1, "Appliances", transaction2.getAmount()));
		saveTestTransaction(account1, transaction2);
		List<Transaction> account1Transactions = new ArrayList<Transaction>();
		account1Transactions.add(transaction1);
		account1Transactions.add(transaction2);
		account1.setTransactions(account1Transactions);
		account2 = accounts.get(1);
		assertEquals(2, account2.getID());
		Transaction transaction3 = makeTestTransaction();
		transaction3.setDate("20011231");
		transaction3.setID(3);
		transaction3.setCategory(new TransactionCategory(++transactionCategoryID, 1, "Appliances", transaction3.getAmount()));
		saveTestTransaction(account2, transaction3);
		List<Transaction> account2Transactions = new ArrayList<Transaction>();
		account2Transactions.add(transaction3);
		account2.setTransactions(account2Transactions);
		assertEquals(1, countTransactionCategoryRows(transaction1.getID()));
		assertEquals(1, countTransactionCategoryRows(transaction2.getID()));
		assertEquals(1, countTransactionCategoryRows(transaction3.getID()));
		result = transactionDao.getTransactions(account1);
		assertTrue(result.isGood());
		result = transactionDao.deleteTransactions(account1);
		assertTrue(result.isGood());
		result = transactionDao.getTransactions(account1);
		assertTrue(result.isGood());
		assertEquals(0, account1.getTransactionCount());
		assertEquals(0, countTransactionCategoryRows(transaction1.getID()));
		assertEquals(0, countTransactionCategoryRows(transaction2.getID()));
		result = transactionDao.getTransactions(account2);
		assertTrue(result.isGood());
		assertEquals(1, account2.getTransactionCount());
		assertEquals(transaction3, account2.getTransactions().get(0));
		assertEquals(1, countTransactionCategoryRows(transaction3.getID()));
	}

}
