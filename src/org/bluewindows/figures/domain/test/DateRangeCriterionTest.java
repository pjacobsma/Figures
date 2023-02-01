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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bluewindows.figures.domain.DateRangeCriterion;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionDate;
import org.junit.Test;

public class DateRangeCriterionTest extends AbstractDomainTest {
	
	public void testConstructor() throws Exception {
		try {
			@SuppressWarnings("unused")
			DateRangeCriterion criterion = new DateRangeCriterion(null, new TransactionDate("20190101"));
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			@SuppressWarnings("unused")
			DateRangeCriterion criterion = new DateRangeCriterion(new TransactionDate("20190101"), null);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
		}

	}
	
	@Test
	public void testDateRange() throws Exception  {
		TransactionDate startDate = new TransactionDate("20190101");
		TransactionDate endDate = new TransactionDate("20190103");
		DateRangeCriterion criterion = new DateRangeCriterion(startDate, endDate);
		Transaction tran = new Transaction();
		tran.setDate("20181231");
		assertFalse(criterion.matches(tran));
		tran.setDate("20190101");
		assertTrue(criterion.matches(tran));
		tran.setDate("20190102");
		assertTrue(criterion.matches(tran));
		tran.setDate("20190103");
		assertTrue(criterion.matches(tran));
		tran.setDate("20190104");
		assertFalse(criterion.matches(tran));
		
	}
}
