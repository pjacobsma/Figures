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

package org.bluewindows.figures.dao.impl.sqlite;

import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.ID;
import static org.bluewindows.figures.domain.persistence.Persistence.NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_STORE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.CategoryDao;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;

public class CategoryDaoImplSqlite extends AbstractDaoImplSqlite implements CategoryDao {

	public CategoryDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		super(persistenceJdbc);
	}

	@Override
	public CallResult getCategories() {
		CallResult result = executeQueryStatement("Select * From " + CATEGORY_STORE_NAME + 
			" Order By " + NAME);
		ResultSet resultSet = (ResultSet) result.getReturnedObject();
		if (result.isGood()) result = mapCategories(resultSet);
		closeResultSet(resultSet);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CallResult getLastCategory() {
		CallResult result = executeQueryStatement("Select * From " + CATEGORY_STORE_NAME + 
			" Where ID = (Select Max(" + ID + ") From " + CATEGORY_STORE_NAME + ")");
		ResultSet resultSet = (ResultSet) result.getReturnedObject();
		if (result.isGood()) result = mapCategories(resultSet);
		closeResultSet(resultSet);
		if (result.isBad()) return result;
		List<Category> categories = (List<Category>)result.getReturnedObject();
		if (categories.isEmpty()) return result.setCallBad("Category Retrieval Failure", "No categories found");
		return result.setReturnedObject(((List<Category>)result.getReturnedObject()).get(0));
	}
	
	@Override
	public CallResult insertCategory(Category category) {
		CallResult result = new CallResult();
		if (result.isBad()) return result;
		try {
			String insertStmt = "INSERT INTO " + CATEGORY_STORE_NAME + 
				" (" + ID + ", " + NAME + ") VALUES(?,?)";
			PreparedStatement pStmt = prepareStatement(insertStmt);
			pStmt.setInt(1, category.getID());
			pStmt.setString(2, category.getName());
			pStmt.executeUpdate();
			pStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Insert Category Failure", e.getLocalizedMessage());
		}
		return result;
	}


	@Override
	public CallResult addCategory(String categoryName) {
		CallResult result = new CallResult();
		if (result.isBad()) return result;
		try {
			String insertStmt = "INSERT INTO " + CATEGORY_STORE_NAME + 
				" (" + NAME + ") VALUES(?)";
			PreparedStatement pStmt = prepareStatement(insertStmt);
			pStmt.setString(1, categoryName);
			pStmt.executeUpdate();
			pStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Add Category Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult updateCategory(Category category) {
		String updateStmt = "UPDATE " + CATEGORY_STORE_NAME + " " +
			"SET " + NAME + " = '" + category.getName() + "' ";
		updateStmt = updateStmt + "WHERE " + ID + " = " + category.getID();
		return executeUpdateStatement(updateStmt);
	}

	@Override
	public CallResult deleteCategory(int categoryID) {
		return executeUpdateStatement("DELETE FROM " + CATEGORY_STORE_NAME + " WHERE " + ID + " = " + categoryID);
	}

	private CallResult mapCategories(ResultSet rs) {
		CallResult result = new CallResult();
		List<Category> categories = new ArrayList<Category>();
		try {
			while (rs.next()) {
				int Id = rs.getInt(ID);
				String name = rs.getString(NAME);
				categories.add(new Category(Id, name));
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Category Mapping Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(categories);
	}

	@Override
	public CallResult checkCategoryUsage(int categoryID) {
		CallResult result = executeQueryStatement("Select count(*) From " + TRANSACTION_CATEGORY_STORE_NAME + 
			" Where " + CATEGORY_ID + " = " + categoryID);
		Integer rowCount = null;
		if (result.isGood()) {
			ResultSet resultSet = (ResultSet) result.getReturnedObject();
			try {
				rowCount = resultSet.getInt(1);
			} catch (SQLException e) {
				Figures.logStackTrace(e);
				return result.setCallBad("Category Retrieval Failure", e.getLocalizedMessage());
			}
			closeResultSet(resultSet);
		}
		return result.setReturnedObject(rowCount);
	}
	
}