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

import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.TransactionDate;
import org.junit.Test;

public class DateRangeTest {
	
	@Test
	public void testConstructor() throws Exception {
		try {
			new DateRange(null, new TransactionDate("20200101"));
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			new DateRange(new TransactionDate("20200101"), null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			new DateRange(new TransactionDate("20200102"), new TransactionDate("20200101"));
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			new DateRange(new TransactionDate("20200102"), new TransactionDate("20200102"));
		} catch (IllegalArgumentException e) {
			fail("Should not throw IllegalArgumentException");
		}
	}
	
	@Test
	public void testIsAfter() throws Exception  {
		DateRange range1 = new DateRange(new TransactionDate("20200101"), new TransactionDate("20200102"));
		DateRange range2 = new DateRange(new TransactionDate("20200103"), new TransactionDate("20200104"));
		assertTrue(range2.isAfter(range1));
		assertFalse(range1.isAfter(range2));
		
		range2 = new DateRange(new TransactionDate("20200102"), new TransactionDate("20200103"));
		assertFalse(range2.isAfter(range1));
	}
}
