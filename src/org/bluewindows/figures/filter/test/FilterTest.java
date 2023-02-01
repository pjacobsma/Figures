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

import static org.junit.Assert.*;

import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.test.AbstractDomainTest;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterResult;
import org.bluewindows.figures.filter.Filter;
import org.junit.Before;
import org.junit.Test;


public class FilterTest extends AbstractDomainTest {
	
	private Transaction transaction;
	private static String CONTAINS = FilterExpression.CONTAINS.toString();
	private static String REPLACE_DESCRIPTION = FilterResult.REPLACE_DESCRIPTION.toString();
	private static String COPY_TO_DESCRIPTION = FilterResult.COPY_TO_DESCRIPTION.toString();
	private static String REPLACE_MEMO = FilterResult.REPLACE_MEMO.toString();
	private static String COPY_TO_MEMO = FilterResult.COPY_TO_MEMO.toString();
	private static String NONE = FilterResult.NONE.toString();
	
	@Before
	public void setUp() {
		transaction = new Transaction();
	}

	@Test
	public void testDescriptionReplace(){
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "McDon", REPLACE_DESCRIPTION, "McDonald's", false, null);
		transaction.setOriginalDescription("McDon");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("McDonald's", transaction.getDescription());
	}

	@Test
	public void testDescriptionReplaceBasedOnMemo(){
		Filter filter = new Filter(1, 1, 1, "Memo", CONTAINS, "McDon", REPLACE_DESCRIPTION, "McDonald's", false, null);
		transaction.setOriginalMemo("McDon");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("McDonald's", transaction.getDescription());
	}
	
	@Test
	public void testDescriptionCopyFromMemo(){
		Filter filter = new Filter(1, 1, 1, "Memo", CONTAINS, "McDonald's", COPY_TO_DESCRIPTION, null, false, null);
		transaction.setOriginalMemo("McDonald's");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("McDonald's", transaction.getDescription());
	}
	
	@Test
	public void testMemoReplace(){
		Filter filter = new Filter(1, 1, 1, "Memo", CONTAINS, "McDon", REPLACE_MEMO, "McDonald's", false, null);
		transaction.setOriginalMemo("McDon");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("McDonald's", transaction.getMemo());
	}
	
	@Test
	public void testMemoReplaceBasedOnDescription(){
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "McDon", REPLACE_MEMO, "McDonald's", false, null);
		transaction.setOriginalDescription("McDon");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("McDonald's", transaction.getMemo());
	}
	
	@Test
	public void testMemoCopyFromDescription(){
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "McDonald's", COPY_TO_MEMO, null, false, null);
		transaction.setOriginalDescription("McDonald's");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("McDonald's", transaction.getMemo());
	}

	@Test
	public void testSetDeductible() {
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "Red Cross", NONE, null, true, null);
		transaction.setOriginalDescription("Red Cross");
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertTrue(transaction.isDeductible());
	}

	@Test
	public void testSetCategory() {
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "McDonald's", NONE, null, false, 99);
		transaction.setOriginalDescription("McDonald's");
		Money amount = new Money("12.95");
		transaction.setAmount(amount);
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals(1, transaction.getAddedCategories().size());
		assertEquals(99, transaction.getAddedCategories().get(0).getCategoryID());
		assertEquals(amount, transaction.getAddedCategories().get(0).getAmount());
	}

	@Test
	public void testAllThreeResults() {
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "Red Cr", REPLACE_DESCRIPTION, "Red Cross", true, 99);
		transaction.setOriginalDescription("Red Cr");
		Money amount = new Money("200.00");
		transaction.setAmount(amount);
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("Red Cross", transaction.getDescription());
		assertEquals(1, transaction.getAddedCategories().size());
		assertEquals(99, transaction.getAddedCategories().get(0).getCategoryID());
		assertEquals(amount, transaction.getAddedCategories().get(0).getAmount());
		assertTrue(transaction.isDeductible());
	}
	
	@Test
	public void testAllThreeResultsDisallowed() {
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "Red Cr", REPLACE_DESCRIPTION, "Red Cross", true, 99);
		transaction.setOriginalDescription("Red Cr");
		transaction.setDescription("Red Cr");
		// If the user changes any of these fields, the filter should not override them
		transaction.setUserChangedDesc(true);
		transaction.setUserChangedDeductible(true);
		transaction.setUserChangedCategory(true);
		boolean filterExecuted = filter.execute(transaction);
		assertFalse(filterExecuted);
		assertEquals("Red Cr", transaction.getDescription());
		assertFalse(transaction.isDeductible());
		assertEquals(0, transaction.getCategories().size());
	}
	
	@Test
	public void testDescriptionChangedByUser() {
		Filter filter = new Filter(1, 1, 1, "Description", CONTAINS, "Red Cross", NONE, "", true, 99);
		transaction.setOriginalDescription("Red Cr");
		transaction.setDescription("Red Cross");
		transaction.setUserChangedDesc(true);
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("Red Cross", transaction.getDescription());
		assertEquals(1, transaction.getAddedCategories().size());
		assertEquals(99, transaction.getAddedCategories().get(0).getCategoryID());
	}
	
	@Test
	public void testMemoChangedByUser() {
		Filter filter = new Filter(1, 1, 1, "Memo", CONTAINS, "Red Cross", NONE, "", true, 99);
		transaction.setOriginalMemo("Red Cr");
		transaction.setMemo("Red Cross");
		transaction.setUserChangedMemo(true);
		boolean filterExecuted = filter.execute(transaction);
		assertTrue(filterExecuted);
		assertEquals("Red Cross", transaction.getMemo());
		assertEquals(1, transaction.getAddedCategories().size());
		assertEquals(99, transaction.getAddedCategories().get(0).getCategoryID());
	}

}
