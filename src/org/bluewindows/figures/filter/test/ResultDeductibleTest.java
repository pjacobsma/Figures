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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.test.AbstractDomainTest;
import org.bluewindows.figures.filter.ResultDeductible;
import org.bluewindows.figures.filter.SourceOriginalDescription;
import org.junit.Before;
import org.junit.Test;

public class ResultDeductibleTest extends AbstractDomainTest {
	
	private ResultDeductible result;
	
	@Before
	public void setUp() {
		result = new ResultDeductible();
	}

	@Test
	public void testSetResult() {
		try {
			result.setResultSource(new SourceOriginalDescription());
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void testIsOkToExecute() {
		Transaction transaction = new Transaction();
		transaction.setUserChangedDeductible(false);
		assertTrue(result.isOkToExecute(transaction));
		transaction.setUserChangedDeductible(true);
		assertFalse(result.isOkToExecute(transaction));
		transaction.setUserChangedDeductible(false);
		transaction.setDeductible(true);
		assertFalse(result.isOkToExecute(transaction));
	}
	
	@Test
	public void testExecute() {
		Transaction transaction = new Transaction();
		result.execute(transaction);
		assertTrue(transaction.isDeductible());
	}

}
