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

import java.time.LocalDate;

import org.bluewindows.figures.domain.TransactionDate;
import org.junit.Test;

public class TransactionDateTest extends AbstractDomainTest {
	
	@Test
	public void testNullConstrutor(){
		TransactionDate date = new TransactionDate();
		assertEquals(LocalDate.of(1969, 12, 31), date.value());
	}
	
	@Test
	public void testConstructor() throws Exception {
		TransactionDate date = new TransactionDate("20090102");
		assertEquals(LocalDate.of(2009, 01, 02), date.value());
		LocalDate nullDate = null;
		try {
			date = new TransactionDate(nullDate);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}
	
	@Test
	public void testToString() throws Exception {
		TransactionDate date = new TransactionDate("20090102");
		assertEquals("01/02/2009", date.toString());
	}
	
	@Test
	public void testIsBetween() throws Exception {
		TransactionDate startDate = new TransactionDate("20090102");
		TransactionDate endDate = new TransactionDate("20090104");
		assertFalse(new TransactionDate("20090101").isBetween(startDate, endDate));
		assertTrue(new TransactionDate("20090102").isBetween(startDate, endDate));
		assertTrue(new TransactionDate("20090103").isBetween(startDate, endDate));
		assertTrue(new TransactionDate("20090104").isBetween(startDate, endDate));
		assertFalse(new TransactionDate("20090105").isBetween(startDate, endDate));
	}

}
