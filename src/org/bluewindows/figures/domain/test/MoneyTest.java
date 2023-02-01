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

import java.math.BigDecimal;

import org.bluewindows.figures.domain.Money;
import org.junit.Test;

public class MoneyTest extends AbstractDomainTest {

	@Test
	public void testConstructors() throws Exception {
		try {
			@SuppressWarnings("unused")
			Money money = new Money("1234.56");
		} catch (Exception e) {
			fail();
		}
		
		String moneyValue = "123.456";
		try {
			@SuppressWarnings("unused")
			Money money = new Money(moneyValue);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("Money value with more than 2 decimal places.", e.getLocalizedMessage());
		}
		
		try {
			String nullValue = null;
			@SuppressWarnings("unused")
			Money money = new Money(nullValue);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("Money value empty or null.", e.getLocalizedMessage());
		}

		try {
			BigDecimal nullValue = null;
			@SuppressWarnings("unused")
			Money money = new Money(nullValue);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

	}
	
	@Test
	public void testMissing() throws Exception {
		Money money = new Money();
		assertTrue(money.isMissing());
		assertFalse(money.isNotMissing());
	}
	
	@Test
	public void testGetValue() throws Exception {
		Money money = new Money();
		try {
			money.getValue();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
			assertEquals("Attempt to use uninitialized Money value.", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testToString() throws Exception {
		Money money = new Money("123");
		assertEquals("$123.00", money.toString());

		money = new Money("123.5");
		assertEquals("$123.50", money.toString());
	
		money = new Money("1234.56");
		assertEquals("$1,234.56", money.toString());
	
		money = new Money();
		assertEquals("", money.toString());
	}

	@Test
	public void testToStringNumber() throws Exception {
		Money money = new Money("123");
		assertEquals("123.00", money.toStringNumber());

		money = new Money("123.5");
		assertEquals("123.50", money.toStringNumber());
	
		money = new Money("1234.56");
		assertEquals("1234.56", money.toStringNumber());
	
		money = new Money();
		try {
			money.toStringNumber();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Attempt to use uninitialized Money value.", e.getLocalizedMessage());
		}
	}
	
	@Test
	public void testIsCredit(){
		Money money = new Money("1.00");
		assertTrue(money.isCredit());
		money = new Money("0.00");
		assertFalse(money.isCredit());
		money = new Money("-1.00");
		assertFalse(money.isCredit());
		
	}

}
