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

import java.util.List;

import org.bluewindows.figures.dao.impl.sqlite.AccountDaoImplSqlite;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;
import org.junit.Before;
import org.junit.Test;

public class AccountDaoImplSqliteTest extends AbstractDaoImplSqliteTestCase {
	
	private AccountDaoImplSqlite accountDao;
	
	@Before
	public void before(){
		super.before();
		accountDao = (AccountDaoImplSqlite)persistence.getAccountDao();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetAccountsSortedByName() throws Exception {
		saveTestAccount("B Account", 1);
		saveTestAccount("A Account", 2);
		CallResult result = accountDao.getAccounts();
		assertTrue(result.isGood());
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		assertEquals(2, accounts.size());
		Account account1 = accounts.get(0);
		assertEquals(2, account1.getID());
		assertEquals("A Account", account1.getName());
		assertEquals(AccountType.CHECKING, account1.getType());
		assertEquals(FilterSet.NONE, account1.getFilterSet());
		assertEquals("C:\\Imports", account1.getImportFolder());
		Account account2 = accounts.get(1);
		assertEquals(1, account2.getID());
		assertEquals("B Account", account2.getName());
		assertEquals("C:\\Imports", account2.getImportFolder());
	}

	@Test
	public void testGetLastAccount() throws Exception {
		Account account1 = new Account(0, "First Third", AccountType.CHECKING, FilterSet.NONE, new Money("0.00"), new TransactionDate("19990101"));
		CallResult result = accountDao.addAccount(account1);
		Account account2 = new Account(0, "Able Bank", AccountType.CHECKING, FilterSet.NONE, new Money("0.00"), new TransactionDate("19990101"));
		result = accountDao.addAccount(account2);
		assertTrue(result.isGood());
		result = accountDao.getLastAccount();
		assertTrue(result.isGood());
		assertEquals("Able Bank", ((Account)result.getReturnedObject()).getName());
	}

	@Test
	public void testGetLastAccountWithNoResult() throws Exception {
		CallResult result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		for (Account account : accounts) {
			accountDao.deleteAccount(account);
		}
		result = accountDao.getLastAccount();
		assertTrue(result.isBad());
		assertEquals("Account Not found", result.getErrorMessage());
	}

	@Test
	public void testAddAccount() throws Exception {
		Account account = new Account(1, "TestChecking", AccountType.CHECKING, FilterSet.NONE, new Money("0.00"), new TransactionDate("19990101"));
		CallResult result = accountDao.addAccount(account);
		assertTrue(result.isGood());
		assertTrue(account.getID() != 0);
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		assertEquals(1, accounts.size());
		Account returnedAccount = accounts.get(0);
		assertEquals(account.getID(), returnedAccount.getID());
		assertEquals(account.getName(), returnedAccount.getName());
		assertEquals(account.getType(), returnedAccount.getType());
		assertEquals(account.getFilterSet(), returnedAccount.getFilterSet());
		assertEquals(account.getInitialBalance(), returnedAccount.getInitialBalance());
		assertEquals(account.getLastLoadedDate(), returnedAccount.getLastLoadedDate());
		assertTrue(account.getImportFolder().isEmpty());
	}

	@Test
	public void testAddAccountWithInvalidFilterSetID() throws Exception {
		//Verify referential integrity check for Filter Set
		FilterSet filterSet = new FilterSet(100, "Bad Set", "", "", "");
		Account account = new Account(100, "TestChecking", AccountType.CHECKING, filterSet, new Money("0.00"), new TransactionDate("19990101"));
		CallResult result = accountDao.addAccount(account);
		assertTrue(result.isBad());
		assertEquals("Invalid filter set ID: 100", result.getErrorMessage());
	}
	
	@Test
	public void testUpdateAccount() throws Exception {
		Account account = new Account(0, "TestChecking", AccountType.CHECKING, FilterSet.NONE, new Money("0.00"), new TransactionDate("19990101"));
		account.setImportFolder("C:\\Imports");
		CallResult result = accountDao.addAccount(account);
		assertTrue(result.isGood());
		saveTestFilterSet(100, "Filter Set 1", "", "", "");
		account.setFilterSet(new FilterSet(100, "Filter Set 1", "", "", ""));
		account.setName("Joint Checking");
		account.setImportFolder("D:\\Imports");
		result = accountDao.updateAccount(account);
		assertTrue(result.isGood());
		result = accountDao.getLastAccount();
		assertTrue(result.isGood());
		Account updatedAccount = (Account)result.getReturnedObject();
		assertEquals("Joint Checking", updatedAccount.getName());
		assertEquals(100, updatedAccount.getFilterSet().getID());
		assertEquals("D:\\Imports", updatedAccount.getImportFolder());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAccount() throws Exception {
		Account account = new Account(1, "TestChecking", AccountType.CHECKING, FilterSet.NONE, new Money("0.00"), new TransactionDate("19990101"));
		CallResult result = accountDao.addAccount(account);
		assertTrue(result.isGood());
		result = accountDao.deleteAccount(account);
		assertTrue(result.isGood());
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		assertEquals(0, ((List<Account>)result.getReturnedObject()).size());
	}
}
