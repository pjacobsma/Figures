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

import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.test.AbstractDomainTest;
import org.bluewindows.figures.filter.ResultDescription;
import org.bluewindows.figures.filter.SourceLiteral;
import org.junit.Before;
import org.junit.Test;

public class ResultDescriptionTest extends AbstractDomainTest {
	
	private ResultDescription result;
	
	@Before
	public void setUp() {
		result = new ResultDescription();
	}

	@Test
	public void testIsOkToExecute() {
		Transaction transaction = new Transaction();
		transaction.setDescription("Description");
		transaction.setUserChangedDesc(false);
		SourceLiteral sourceLiteral = new SourceLiteral("Literal");
		result.setResultSource(sourceLiteral);
		assertTrue(result.isOkToExecute(transaction));
		transaction.setUserChangedDesc(true);
		assertFalse(result.isOkToExecute(transaction));
		transaction.setUserChangedDesc(false);
		transaction.setDescription("Literal");
		assertFalse(result.isOkToExecute(transaction));
	}
	
	@Test
	public void testExecute() {
		result.setResultSource(new SourceLiteral("New Description"));
		Transaction transaction = new Transaction();
		transaction.setDescription("Old Description");
		result.execute(transaction);
		assertEquals("New Description", transaction.getDescription());
	}

}
