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
package org.bluewindows.figures.filter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.domain.test.AbstractDomainTest;
import org.bluewindows.figures.filter.ResultCategory;
import org.bluewindows.figures.filter.SourceCategory;
import org.junit.Before;
import org.junit.Test;

public class ResultCategoryTest extends AbstractDomainTest {
	
	private ResultCategory result;
	private Transaction transaction;
	
	@Before
	public void setUp() {
		result = new ResultCategory();
		transaction = makeTransaction();
	}

	@Test
	public void testCategoryIsOkToExecute() {
		transaction.setUserChangedCategory(false);
		assertTrue(result.isOkToExecute(transaction));
		transaction.setUserChangedCategory(true);
		assertFalse(result.isOkToExecute(transaction));
		transaction.setUserChangedCategory(false);
		result.setResultSource(new SourceCategory(2));
		List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
		TransactionCategory category1 = new TransactionCategory(1, transaction.getAmount());
		categories.add(category1);
		transaction.initializeCategories(categories);
		assertTrue(result.isOkToExecute(transaction));
		transaction = makeTransaction();
		TransactionCategory category2 = new TransactionCategory(2, transaction.getAmount());
		categories.add(category2);
		transaction.initializeCategories(categories);
		assertFalse(result.isOkToExecute(transaction));
		TransactionCategory category3 = new TransactionCategory(3, transaction.getAmount());
		transaction.setCategory(category3);
		result.setResultSource(new SourceCategory(3));
		assertFalse(result.isOkToExecute(transaction));
	}
	
	@Test
	public void testExecute() {
		result.setResultSource(new SourceCategory(1));
		Transaction transaction = new Transaction();
		Money amount = new Money("123.45");
		transaction.setAmount(new Money("123.45"));
		List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
		categories.add(new TransactionCategory(2, transaction.getAmount()));
		transaction.initializeCategories(categories);
		result.execute(transaction);
		assertEquals(1, transaction.getAddedCategories().size());
		assertEquals(1, transaction.getAddedCategories().get(0).getCategoryID());
		assertEquals(amount, transaction.getAddedCategories().get(0).getAmount());
		assertEquals(1, transaction.getDeletedCategories().size());
		assertEquals(2, transaction.getDeletedCategories().get(0).getCategoryID());
	}
	
	private Transaction makeTransaction() {
		transaction = new Transaction();
		transaction.setAmount(new Money("123.45"));
		return transaction;
	}

}
