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

import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.test.AbstractDomainTest;
import org.bluewindows.figures.filter.SearchStarts;
import org.bluewindows.figures.filter.SourceOriginalDescription;
import org.bluewindows.figures.filter.SourceFieldInterface;
import org.junit.Before;
import org.junit.Test;


public class SearchStartsTest extends AbstractDomainTest {
	
	private SearchStarts searchStarts = new SearchStarts();
	private Transaction transaction;
	private SourceFieldInterface sourceField = new SourceOriginalDescription();

	
	@Before
	public void setUp() {
		transaction = new Transaction();
	}
	
	@Test
	public void testSearchStringFound(){
		transaction.setOriginalDescription(" abc ");
		assertFalse(searchStarts.found(transaction, sourceField, "abc"));
		assertTrue(searchStarts.found(transaction, sourceField, " a"));
		assertFalse(searchStarts.found(transaction, sourceField, "c "));
		assertFalse(searchStarts.found(transaction, sourceField, "z"));
		assertTrue(searchStarts.found(transaction, sourceField, " abc "));
	}
	
	@Test
	public void testEmptyString(){
		assertFalse(searchStarts.found(transaction, sourceField, "b"));
	}
	
	@Test
	public void testDescriptionShorterThanSearchString(){
		transaction.setOriginalDescription("Short");
		assertFalse(searchStarts.found(transaction, sourceField, "Short Description"));
	}
	

}
