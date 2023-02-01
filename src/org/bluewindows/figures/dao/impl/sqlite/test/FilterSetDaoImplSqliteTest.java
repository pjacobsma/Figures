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
import static org.bluewindows.figures.domain.persistence.Persistence.*;

import java.sql.ResultSet;
import java.util.List;

import org.bluewindows.figures.dao.impl.sqlite.FilterSetDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.FilterResult;
import org.junit.Before;
import org.junit.Test;

public class FilterSetDaoImplSqliteTest extends AbstractDaoImplSqliteTestCase {

	private FilterSetDaoImplSqlite filterSetDao;

	@Before
	public void before(){
		super.before();
		filterSetDao = (FilterSetDaoImplSqlite)persistence.getFilterSetDao();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetSetsSortedByName() throws Exception {
		saveTestFilterSet(1, "B Test Filter Set", "","","");
		saveTestFilterSet(2, "A Test Filter Set",  "","","");
		CallResult result = filterSetDao.getSets();
		assertTrue(result.isGood());
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		assertEquals(2, filterSets.size());
		FilterSet filterSet1 = filterSets.get(0);
		assertEquals(2, filterSet1.getID());
		assertEquals("A Test Filter Set", filterSet1.getName());
		FilterSet filterSet2  = filterSets.get(1);
		assertEquals(1, filterSet2.getID());
		assertEquals("B Test Filter Set", filterSet2.getName());
	}

	@Test
	public void testGetSet() throws Exception {
		saveTestFilterSet(100, "Filter Set 1", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), FilterResult.REPLACE_DESCRIPTION.toString());
		CallResult result = filterSetDao.getSet(100);
		assertTrue(result.isGood());
		FilterSet filterSet = (FilterSet)result.getReturnedObject();
		assertEquals(100, filterSet.getID());
		assertEquals("Filter Set 1", filterSet.getName());
		assertEquals(FilterField.DESCRIPTION.toString(), filterSet.getDefaultColumn());
		assertEquals(FilterExpression.CONTAINS.toString(), filterSet.getDefaultExpression());
		assertEquals(FilterResult.REPLACE_DESCRIPTION.toString(), filterSet.getDefaultResult());
	}
	
	@Test
	public void testGetSetNotFound() throws Exception {
		CallResult result = filterSetDao.getSet(100);
		assertTrue(result.isGood());
		assertEquals(FilterSet.NONE, result.getReturnedObject());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAddSet() throws Exception {
		CallResult result = filterSetDao.getSets();
		assertTrue(result.isGood());
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		assertEquals(0, filterSets.size());
		FilterSet addedFilterSet = new FilterSet(1, "ZZZ Filter Set", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), FilterResult.REPLACE_DESCRIPTION.toString());
		result = filterSetDao.addSet(addedFilterSet);
		assertTrue(result.isGood());
		result = filterSetDao.getSets();
		assertTrue(result.isGood());
		filterSets = (List<FilterSet>)result.getReturnedObject();
		assertEquals(1, filterSets.size());
		FilterSet returnedFilterSet = filterSets.get(0);
		assertEquals(addedFilterSet.getID(), returnedFilterSet.getID());
		assertEquals(addedFilterSet.getDefaultColumn(), returnedFilterSet.getDefaultColumn().toString());
		assertEquals(addedFilterSet.getDefaultExpression(), returnedFilterSet.getDefaultExpression().toString());
		assertEquals(addedFilterSet.getDefaultResult(), returnedFilterSet.getDefaultResult().toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteSet() throws Exception {
		FilterSet filterSet1 = new FilterSet(1, "Filter Set 1", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), FilterResult.REPLACE_DESCRIPTION.toString());
		CallResult result = filterSetDao.addSet(filterSet1);
		assertTrue(result.isGood());
		FilterSet filterSet2 = new FilterSet(1, "Filter Set 2", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), FilterResult.REPLACE_DESCRIPTION.toString());
		result = filterSetDao.addSet(filterSet2);
		assertTrue(result.isGood());
		result = filterSetDao.getSets();
		assertTrue(result.isGood());
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		assertEquals(2, filterSets.size());
		saveTestFilter(filterSets.get(0).getID(), 1);
		saveTestFilter(filterSets.get(0).getID(), 2);
		result = filterSetDao.getSet(filterSets.get(0).getID());
		assertTrue(result.isGood());
		FilterSet filterSet = (FilterSet)result.getReturnedObject();
		assertEquals(2, filterSet.getFilters().size());
		assertTrue(result.isGood());
		result = filterSetDao.deleteSet(filterSet.getID());
		assertTrue(result.isGood());
		result = filterSetDao.getSets();
		assertTrue(result.isGood());
		assertEquals(1, ((List<FilterSet>)result.getReturnedObject()).size());
		result = executeQueryStatement("Select count(*) from " + FILTER_STORE_NAME + " Where " + FILTER_SET_ID + " = " + filterSet.getID());
		assertTrue(result.isGood());
		ResultSet rs = (ResultSet)result.getReturnedObject();
		int filterCount = rs.getInt(1);
		assertEquals(0, filterCount);
	}
	
	@Test
	public void testUpdateSet() throws Exception {
		FilterSet filterSet = new FilterSet(1, "Filter Set 1", FilterField.DESCRIPTION.toString(), FilterExpression.CONTAINS.toString(), FilterResult.REPLACE_DESCRIPTION.toString());
		CallResult result = filterSetDao.addSet(filterSet);
		assertTrue(result.isGood());
		result = filterSetDao.getSet(1);
		assertTrue(result.isGood());
		filterSet.setName("New Name");
		filterSet.setDefaultColumn(FilterField.MEMO.toString());
		filterSet.setDefaultExpression(FilterExpression.EQUALS.toString());
		filterSet.setDefaultResult(FilterResult.COPY_TO_DESCRIPTION.toString());
		result = filterSetDao.updateSet(filterSet);
		assertTrue(result.isGood());
		result = filterSetDao.getSet(1);
		assertTrue(result.isGood());
		FilterSet updatedFilterSet = (FilterSet)result.getReturnedObject();
		assertEquals("New Name", updatedFilterSet.getName());
		assertEquals(FilterField.MEMO.toString(), updatedFilterSet.getDefaultColumn());
		assertEquals(FilterExpression.EQUALS.toString(), updatedFilterSet.getDefaultExpression());
		assertEquals(FilterResult.COPY_TO_DESCRIPTION.toString(), updatedFilterSet.getDefaultResult());
	}
	
}
