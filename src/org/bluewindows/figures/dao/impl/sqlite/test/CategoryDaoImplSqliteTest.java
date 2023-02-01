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

import org.bluewindows.figures.dao.impl.sqlite.CategoryDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.junit.Before;
import org.junit.Test;

public class CategoryDaoImplSqliteTest extends AbstractDaoImplSqliteTestCase {

	private CategoryDaoImplSqlite categoryDao;

	@Before
	public void before(){
		super.before();
		categoryDao = (CategoryDaoImplSqlite)persistence.getCategoryDao();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAddAndGetCategories() throws Exception {
		String category = "A new category";
		CallResult result = categoryDao.addCategory(category);
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		List<Category> categories = (List<Category>)result.getReturnedObject();
		assertEquals(1, categories.size());
	}

	@Test
	public void testGetLastCategory() throws Exception {
		String category1 = "AaPostage";
		CallResult result = categoryDao.addCategory(category1);
		assertTrue(result.isGood());
		String category2 = "AaGroceries";
		result = categoryDao.addCategory(category2);
		assertTrue(result.isGood());
		result = categoryDao.getLastCategory();
		assertTrue(result.isGood());
		assertEquals("AaGroceries", ((Category)result.getReturnedObject()).getName());
		assertEquals(2, ((Category)result.getReturnedObject()).getID());
	}

	@Test
	public void testGetLastCategoryNoResult() throws Exception {
		CallResult result = categoryDao.getCategories();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>)result.getReturnedObject();
		for (Category category : categories) {
			categoryDao.deleteCategory(category.getID());
		}
		result = categoryDao.getLastCategory();
		assertTrue(result.isBad());
		assertEquals("No categories found", result.getErrorMessage());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testUpdateCategory() throws Exception {
		CallResult result = categoryDao.addCategory("Test Category");
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		List<Category> categories = (List<Category>)result.getReturnedObject();
		assertEquals(1, categories.size());
		result = categoryDao.addCategory("AaTest");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		categories = (List<Category>)result.getReturnedObject();
		assertEquals(2, categories.size());
		Category returnedCategory = categories.get(0);
		assertEquals("AaTest", returnedCategory.getName());
		returnedCategory.setName("Aaaaatest");
		result = categoryDao.updateCategory(returnedCategory);
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		categories = (List<Category>)result.getReturnedObject();
		assertEquals(2, categories.size());
		assertEquals("Aaaaatest", categories.get(0).getName());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testDeleteCategory() throws Exception {
		CallResult result = categoryDao.addCategory("Test Category");
		assertTrue(result.isGood());
		result = categoryDao.getCategories();
		assertTrue(result.isGood());
		List<Category> categories = (List<Category>)result.getReturnedObject();
		assertEquals(1, categories.size());
		result = categoryDao.deleteCategory(categories.get(0).getID());
		assertTrue(result.isGood());
		assertEquals(1, ((Integer)result.getReturnedObject()).intValue()); // Check delete count
		result = categoryDao.getCategories();
		categories = (List<Category>)result.getReturnedObject();
		assertEquals(0, categories.size());

	}

}
