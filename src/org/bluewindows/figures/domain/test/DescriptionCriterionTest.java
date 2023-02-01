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

import org.bluewindows.figures.domain.DescriptionCriterion;
import org.bluewindows.figures.domain.Transaction;
import org.junit.Test;



public class DescriptionCriterionTest extends AbstractDomainTest {
	
	public void testConstructor() throws Exception {
		try {
			new DescriptionCriterion(null);
			fail("Should throw exception");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void testMatch() throws Exception  {
		String searchString = new String("def");
		Transaction tran = new Transaction();
		tran.setDescription("ABCDEFGHI");
		DescriptionCriterion criterion = new DescriptionCriterion(searchString);
		assertTrue(criterion.matches(tran));

		searchString = new String("DEF");
		tran = new Transaction();
		tran.setDescription("abcdefghi");
		criterion = new DescriptionCriterion(searchString);
		assertTrue(criterion.matches(tran));
		
		searchString = new String(" def");
		criterion = new DescriptionCriterion(searchString);
		assertFalse(criterion.matches(tran));

		searchString = new String("def ");
		criterion = new DescriptionCriterion(searchString);
		assertFalse(criterion.matches(tran));
	}
}
