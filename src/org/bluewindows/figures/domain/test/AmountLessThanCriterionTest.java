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

import org.bluewindows.figures.domain.AmountLessThanCriterion;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.junit.Test;

public class AmountLessThanCriterionTest extends AbstractDomainTest {
	
	@Test
	public void testConstructor() {
		try {
			@SuppressWarnings("unused")
			AmountLessThanCriterion criterion = new AmountLessThanCriterion(null);
			fail("Should throw exception.");
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testMatchNullTransAmount() {
		Transaction transaction = new Transaction();
		transaction.setAmount(null);
		AmountLessThanCriterion criterion = new AmountLessThanCriterion(new Money("100.00"));
		assertFalse(criterion.matches(transaction));
	}
	
	@Test
	public void testMatch() {
		Transaction transaction = new Transaction();
		transaction.setAmount(new Money("100.01"));
		AmountLessThanCriterion criterion = new AmountLessThanCriterion(new Money("100.00"));
		assertFalse(criterion.matches(transaction));
		criterion = new AmountLessThanCriterion(new Money("100.01"));
		assertFalse(criterion.matches(transaction));
		criterion = new AmountLessThanCriterion(new Money("100.02"));
		assertTrue(criterion.matches(transaction));
	}

}
