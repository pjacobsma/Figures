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

package org.bluewindows.figures.dao.impl.sqlite.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bluewindows.figures.dao.impl.sqlite.FilterDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.FilterResult;
import org.bluewindows.figures.filter.Filter;
import org.junit.Before;
import org.junit.Test;

public class FilterDaoImplSqliteTest extends AbstractDaoImplSqliteTestCase {

	private FilterDaoImplSqlite filterDao;

	@Before
	public void before(){
		super.before();
		filterDao = (FilterDaoImplSqlite)persistence.getFilterDao();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetFilters() throws Exception {
		saveTestFilter(100, 100);
		CallResult result = filterDao.getFilters(100);
		assertTrue(result.isGood());
		List<Filter> filters = (List<Filter>)result.getReturnedObject();
		assertEquals(1, filters.size());
		Filter filter = filters.get(0);
		assertEquals(Integer.valueOf(100), filter.getSequence());
		assertTrue(filter.isDeductible());
		assertEquals(FilterExpression.EQUALS.toString(), filter.getExpression());
		assertEquals(FilterField.DESCRIPTION.toString(), filter.getFieldName());
		assertEquals(100, filter.getFilterSetID());
		assertEquals(1, filter.getID());
		assertEquals("Replacement", filter.getReplacementValue());
		assertEquals(FilterResult.REPLACE_DESCRIPTION.toString(), filter.getResultAction());
		assertEquals("Value", filter.getSearchValue());
		assertEquals(100, filter.getSequence().intValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetFiltersNoneFound() throws Exception {
		CallResult result = filterDao.getFilters(1);
		assertTrue(result.isGood());
		assertEquals(0, ((List<Filter>)result.getReturnedObject()).size());
	}

	@Test
	public void testMaxFilterSequence() throws Exception {
		saveTestFilter(100, 100);
		saveTestFilter(200, 200);
		CallResult result = filterDao.getMaxFilterSequence();
		assertTrue(result.isGood());
		assertEquals(Integer.valueOf(200), ((Integer)result.getReturnedObject()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteFilter() throws Exception {
		saveTestFilter(100, 100);
		CallResult result = filterDao.getFilters(100);
		assertTrue(result.isGood());
		assertEquals(1, ((List<Filter>)result.getReturnedObject()).size());
		result = filterDao.deleteFilter(1);
		assertTrue(result.isGood());
		result = filterDao.getFilters(1);
		assertEquals(0, ((List<Filter>)result.getReturnedObject()).size());
	}

	@Test
	public void testGetMaxSequenceHappyPath() throws Exception {
		CallResult result = filterDao.getMaxSequence();
		assertTrue(result.isGood());
		assertEquals(Integer.valueOf(0), result.getReturnedObject());
		saveTestFilter(100, 100);
		result = filterDao.getMaxSequence();
		assertTrue(result.isGood());
		assertEquals(Integer.valueOf(100), result.getReturnedObject());
	}

}
