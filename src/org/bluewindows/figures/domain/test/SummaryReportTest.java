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
package org.bluewindows.figures.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.SummaryReport;
import org.junit.Test;

public class SummaryReportTest extends AbstractDomainTest {
	
	private SummaryReport report;
	
	@Test
	public void testSetAccounts() {
		report = new SummaryReport();
		List<Account> accounts = new ArrayList<Account>();
		Account account = new Account(1);
		accounts.add(account);
		report.setAccounts(accounts);
		try {
			report.setAccounts(accounts);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Cannot use this method to update an existing summary report.", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testAddAccount() {
		report = new SummaryReport();
		List<Account> accounts = new ArrayList<Account>();
		Account account = new Account(1);
		accounts.add(account);
		report.addAccount(account);
		assertEquals(1, report.getAccounts().size());
		assertEquals(1, report.getAccounts().get(0).getID());
		assertEquals(1, report.getAddedAccounts().size());
		assertEquals(1, report.getAddedAccounts().get(0).getID());
		try {
			report.addAccount(account);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Duplicate account added to summary report.", e.getLocalizedMessage());
		}
	}

	@Test
	public void testDeleteAccount() {
		report = new SummaryReport();
		List<Account> accounts = new ArrayList<Account>();
		Account account1 = new Account(1);
		Account account2 = new Account(2);
		try {
			report.deleteAccount(account1);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Can only use this method to update an existing summary report.", e.getLocalizedMessage());
		}
		accounts.add(account1);
		report.setAccounts(accounts);
		try {
			report.deleteAccount(account2);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Account not found in this summary report.", e.getLocalizedMessage());
		}
		report.addAccount(account2);
		assertEquals(2, report.getAccounts().size());
		report.deleteAccount(account1);
		assertEquals(1, report.getAccounts().size());
		assertEquals(2, report.getAccounts().get(0).getID());
		assertEquals(1, report.getDeletedAccounts().size());
		assertEquals(1, report.getDeletedAccounts().get(0).getID());
	}
	

	@Test
	public void testSetCategories() {
		report = new SummaryReport();
		List<Category> categories = new ArrayList<Category>();
		Category category = new Category(1);
		categories.add(category);
		report.setCategories(categories);
		try {
			report.setCategories(categories);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Cannot use this method to update an existing summary report.", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testAddCategory() {
		report = new SummaryReport();
		List<Category> categories = new ArrayList<Category>();
		Category category = new Category(1);
		categories.add(category);
		report.addCategory(category);
		assertEquals(1, report.getCategories().size());
		assertEquals(1, report.getCategories().get(0).getID());
		assertEquals(1, report.getAddedCategories().size());
		assertEquals(1, report.getAddedCategories().get(0).getID());
		try {
			report.addCategory(category);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Duplicate category added to summary report.", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testDeleteCategory() {
		report = new SummaryReport();
		List<Category> categories = new ArrayList<Category>();
		Category category1 = new Category(1);
		Category category2 = new Category(2);
		try {
			report.deleteCategory(category1);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Can only use this method to update an existing summary report.", e.getLocalizedMessage());
		}
		categories.add(category1);
		report.setCategories(categories);
		try {
			report.deleteCategory(category2);
			fail("Should throw exception.");
		} catch (UnsupportedOperationException e) {
			assertEquals("Category not found in this summary report.", e.getLocalizedMessage());
		}
		report.addCategory(category2);
		assertEquals(2, report.getCategories().size());
		report.deleteCategory(category1);
		assertEquals(1, report.getCategories().size());
		assertEquals(2, report.getCategories().get(0).getID());
		assertEquals(1, report.getDeletedCategories().size());
		assertEquals(1, report.getDeletedCategories().get(0).getID());
	}
	
	@Test
	public void testDeleteAllCategories() {
		report = new SummaryReport();
		Category category1 = new Category(1);
		Category category2 = new Category(2);
		report.addCategory(category1);
		report.addCategory(category2);
		assertEquals(2, report.getCategories().size());
		assertEquals(0, report.getDeletedCategories().size());
		report.deleteAllCategories();
		assertEquals(0, report.getCategories().size());
		assertEquals(2, report.getDeletedCategories().size());

	}


}
