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
import static org.junit.Assert.fail;

import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.test.AbstractDomainTest;
import org.bluewindows.figures.filter.ResultMemo;
import org.bluewindows.figures.filter.SourceLiteral;
import org.bluewindows.figures.filter.SourceOriginalMemo;
import org.junit.Before;
import org.junit.Test;

public class ResultMemoTest extends AbstractDomainTest {
	
	private ResultMemo result;
	
	@Before
	public void setUp() {
		result = new ResultMemo();
	}

	@Test
	public void testIsOkToExecute() {
		Transaction transaction = new Transaction();
		transaction.setMemo("Memo");
		transaction.setUserChangedMemo(false);
		SourceLiteral sourceLiteral = new SourceLiteral("Literal");
		result.setResultSource(sourceLiteral);
		assertTrue(result.isOkToExecute(transaction));
		transaction.setUserChangedMemo(true);
		assertFalse(result.isOkToExecute(transaction));
		transaction.setUserChangedMemo(false);
		transaction.setMemo("Literal");
		assertFalse(result.isOkToExecute(transaction));
	}
	
	@Test
	public void testExecute() {
		result.setResultSource(new SourceLiteral("New Memo"));
		Transaction transaction = new Transaction();
		transaction.setMemo("Old Memo");
		result.execute(transaction);
		assertEquals("New Memo", transaction.getMemo());
	}
	
	@Test
	public void testExecuteSourceMemo() {
		try {
			result.setResultSource(new SourceOriginalMemo());
			fail("Should throw IllegalArgument exception.");
		} catch (Exception e) {
		}
	}

}
