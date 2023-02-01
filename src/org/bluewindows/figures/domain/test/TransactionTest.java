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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.junit.Before;
import org.junit.Test;


public class TransactionTest extends AbstractDomainTest {

	private Transaction tran;
	
	@Before
	public void before() {
		makeNewTransaction();
	}
	
	private void makeNewTransaction() {
		tran = new Transaction();
		tran.setID(1);
		tran.setDescription("Description");
		tran.setMemo("Memo");
		tran.setAmount(new Money("1.00"));
		try {
			tran.setDate("20090101");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tran.setUpdated(false);
	}
	
	@Test
	public void testSetNumber() {
		tran.setNumber("01");
		assertEquals("1", tran.getNumber());
		tran.setNumber("101");
		assertEquals("101", tran.getNumber());
		tran.setNumber("10");
		assertEquals("10", tran.getNumber());
	}
	
	@Test
	public void testIsChanged() throws Exception{
		tran.setUpdated(false);
		assertFalse(tran.isUpdated());
		tran.setAmount(new Money());
		assertTrue(tran.isUpdated());

		tran.setUpdated(false);
		tran.setDate("20020202");
		assertTrue(tran.isUpdated());
		
		tran.setUpdated(false);
		tran.setDeductible(true);
		assertTrue(tran.isUpdated());
		
		tran.setUpdated(false);
		tran.setDescription("");
		assertTrue(tran.isUpdated());
		
		tran.setUpdated(false);
		tran.setMemo("");
		assertTrue(tran.isUpdated());
	}

	@Test
	public void testInitializeCategories() {
		assertTrue(tran.getAddedCategories().isEmpty());
		assertTrue(tran.getDeletedCategories().isEmpty());
		assertFalse(tran.isUpdated());
		assertFalse(tran.isCategoryUpdated());
		List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
		TransactionCategory category = new TransactionCategory(0, new Money("1.00"));
		categories.add(category);
		tran.initializeCategories(categories);
		assertTrue(tran.getAddedCategories().isEmpty());
		assertTrue(tran.getDeletedCategories().isEmpty());
		assertFalse(tran.isUpdated());
		assertFalse(tran.isCategoryUpdated());
		assertEquals(1, tran.getCategories().size());
		assertEquals(category, tran.getCategories().get(0));
		try {
			tran.initializeCategories(categories);
			fail("Should throw UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
			assertEquals("Cannot use this method to update a transaction already containing categories", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testSetCategories() {
		assertTrue(tran.getAddedCategories().isEmpty());
		assertTrue(tran.getDeletedCategories().isEmpty());
		assertFalse(tran.isUpdated());
		assertFalse(tran.isCategoryUpdated());
		List<TransactionCategory> existingCategories = new ArrayList<TransactionCategory>();
		existingCategories.add(new TransactionCategory(0, new Money(".45")));
		existingCategories.add(new TransactionCategory(2, new Money(".55")));
		tran.initializeCategories(existingCategories);
		TransactionCategory newCategory = new TransactionCategory(1, new Money("1.00"));
		List<TransactionCategory> newCategories = new ArrayList<TransactionCategory>();
		newCategories.add(newCategory);
		tran.setCategories(newCategories);
		assertTrue(tran.isUpdated());
		assertTrue(tran.isCategoryUpdated());
		assertEquals(0, tran.getCategories().size());
		assertEquals(1, tran.getAddedCategories().size());
		assertEquals(2, tran.getDeletedCategories().size());
		assertEquals(newCategory, tran.getAddedCategories().get(0));
	}
	
	@Test
	public void testSetCategory() {
		assertTrue(tran.getAddedCategories().isEmpty());
		assertTrue(tran.getDeletedCategories().isEmpty());
		assertFalse(tran.isUpdated());
		assertFalse(tran.isCategoryUpdated());
		List<TransactionCategory> existingCategories = new ArrayList<TransactionCategory>();
		existingCategories.add(new TransactionCategory(1, new Money(".45")));
		existingCategories.add(new TransactionCategory(2, new Money(".55")));
		tran.initializeCategories(existingCategories);
		TransactionCategory newCategory = new TransactionCategory(3, new Money("1.00"));
		tran.setCategory(newCategory);
		assertTrue(tran.isUpdated());
		assertTrue(tran.isCategoryUpdated());
		assertEquals(0, tran.getCategories().size());
		assertEquals(1, tran.getAddedCategories().size());
		assertEquals(2, tran.getDeletedCategories().size());
		assertEquals(newCategory, tran.getAddedCategories().get(0));
	}
	
}
