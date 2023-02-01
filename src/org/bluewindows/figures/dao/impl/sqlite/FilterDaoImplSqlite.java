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
import static org.bluewindows.figures.domain.persistence.Persistence.DEDUCTIBLE;
import static org.bluewindows.figures.domain.persistence.Persistence.EXPRESSION;
import static org.bluewindows.figures.domain.persistence.Persistence.FIELD;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_SET_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.ID;
import static org.bluewindows.figures.domain.persistence.Persistence.REPLACEMENT;
import static org.bluewindows.figures.domain.persistence.Persistence.RESULT;
import static org.bluewindows.figures.domain.persistence.Persistence.SEQUENCE;
import static org.bluewindows.figures.domain.persistence.Persistence.VALUE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.FilterDao;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.enums.Deductible;
import org.bluewindows.figures.filter.Filter;

public class FilterDaoImplSqlite extends AbstractDaoImplSqlite implements FilterDao {

	public FilterDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		super(persistenceJdbc);
	}

	@Override
	public CallResult getFilters(int filterSetID) {
		CallResult result = executeQueryStatement("Select * From " + FILTER_STORE_NAME + " " +
			"WHERE " + FILTER_SET_ID + " = " + filterSetID + " " +
			"ORDER BY " + SEQUENCE);
		if (result.isBad()) {
			decorateResult(result);
		}
		ResultSet resultSet = (ResultSet) result.getReturnedObject();
		if (result.isGood()) result = mapFilters(resultSet);
		closeResultSet(resultSet);
		return result;
	}
	
	@Override
	public CallResult getMaxFilterSequence() {
		CallResult result = executeQueryStatement("Select Max(" + SEQUENCE + ") From " + FILTER_STORE_NAME);
		if (result.isBad()) return result;
		ResultSet resultSet = (ResultSet)result.getReturnedObject();
		Integer maxSequence = null;
		try {
			maxSequence = Integer.valueOf(resultSet.getInt(1));
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Max Filter Retrieval Failure", e.getLocalizedMessage());
		}
		closeResultSet(resultSet);
		return result.setReturnedObject(maxSequence);
	}

	@Override
	public CallResult addFilter(Filter filter) {
		int maxSequence = 1;
		CallResult result = new CallResult();
		CallResult maxSeqResult = getMaxSequence();
		if (maxSeqResult.isGood()){
			maxSequence = (Integer) maxSeqResult.getReturnedObject() + 1;
		}else{
			return result;
		}
		try {
			PreparedStatement pStmt = prepareStatement("INSERT INTO " + FILTER_STORE_NAME + " (" + 
				FILTER_SET_ID + ", " +
				SEQUENCE + ", " + 
				FIELD + ", " + 
				EXPRESSION + ", " + 
				VALUE + ", " + 
				RESULT + ", " + 
				REPLACEMENT + ", " + 
				DEDUCTIBLE + ", " +
				CATEGORY_ID +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			pStmt.setInt(1, filter.getFilterSetID());
			pStmt.setInt(2, maxSequence);
			pStmt.setString(3, filter.getField());
			pStmt.setString(4, filter.getExpression());
			pStmt.setString(5, filter.getSearchValue());
			pStmt.setString(6, filter.getResultAction());
			pStmt.setString(7, filter.getReplacementValue());
			pStmt.setInt(8, filter.getDeductible().equals(Deductible.YES)? 1:0);
			pStmt.setInt(9, filter.getCategoryID());
			pStmt.executeUpdate();
			pStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Add Filter Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult deleteFilter(int filterID) {
		return executeUpdateStatement("DELETE FROM " + FILTER_STORE_NAME + " " + "WHERE " + ID + " = " + filterID);
	}

	@Override
	public CallResult updateFilter(Filter filter) {
		CallResult result = new CallResult();
		try {
			PreparedStatement pStmt = prepareStatement("UPDATE " + FILTER_STORE_NAME + " " +
				"SET " + SEQUENCE + " = ?" +
				", " + FIELD + " = ?" +
				", " + EXPRESSION + " = ?" +
				", " + VALUE + " = ?" +
				", " + RESULT + " = ?" +
				", " + REPLACEMENT + " = ?"+
				", " + DEDUCTIBLE + " = ?" +
				", " + CATEGORY_ID + " = ?" +
				" WHERE " + ID + " = ? ");
			pStmt.setInt(1, filter.getSequence().intValue());
			pStmt.setString(2, filter.getField());
			pStmt.setString(3, filter.getExpression());
			pStmt.setString(4, filter.getSearchValue());
			pStmt.setString(5, filter.getResultAction());
			pStmt.setString(6, filter.getReplacementValue());
			pStmt.setInt(7, filter.getDeductible().equals(Deductible.YES)? 1:0);
			if (filter.getCategoryID() != null) {
				pStmt.setInt(8, filter.getCategoryID());
			}else {
				pStmt.setNull(8, Types.INTEGER);
			}
			pStmt.setInt(9, filter.getID());
			pStmt.executeUpdate();
			pStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Update Filter Failure", e.getLocalizedMessage());
		}
		return result;
	}


	@Override
	public CallResult getMaxSequence() {
		CallResult callResult = executeQueryStatement("Select Max(" + SEQUENCE + ") as MaxSeq From " + FILTER_STORE_NAME);
		if (callResult.isGood()) {
			ResultSet resultSet = (ResultSet) callResult.getReturnedObject();
			try {
				if (resultSet.next()){
					callResult.setReturnedObject(Integer.valueOf(resultSet.getInt("MaxSeq")));
				}else{
					callResult.setReturnedObject(Integer.valueOf(0));
				}
				resultSet.close();
			} catch (SQLException e) {
				Figures.logStackTrace(e);
				callResult.setCallBad("Max Sequence Retrieval Failure", e.getLocalizedMessage());
			}
			closeResultSet(resultSet);
		}
		return callResult;
	}

	private CallResult mapFilters(ResultSet rs) {
		CallResult callResult = new CallResult();
		List<Filter> filters = new ArrayList<Filter>();
		try {
			while (rs.next()) {
				int Id = rs.getInt(ID);
				int filterSetID = rs.getInt(FILTER_SET_ID);
				int sequence = rs.getInt(SEQUENCE);
				String field = rs.getString(FIELD);
				String expression = rs.getString(EXPRESSION);
				String searchValue = rs.getString(VALUE);
				String resultAction = rs.getString(RESULT);
				String replacementValue = rs.getString(REPLACEMENT);
				boolean deductible = rs.getInt(DEDUCTIBLE) == 0? false: true;
				String categoryString = rs.getString(CATEGORY_ID);
				Integer categoryID = null;
				if (categoryString != null && !categoryString.isEmpty()) {
					categoryID = rs.getInt(CATEGORY_ID);
				}
				filters.add(new Filter(Id, filterSetID, sequence, field, expression, searchValue,
					resultAction, replacementValue, deductible, categoryID));
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			callResult.setCallBad("Map Filters Error", e.getLocalizedMessage());
		}
		return callResult.setReturnedObject(filters);
	}

}
