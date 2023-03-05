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

import static org.bluewindows.figures.domain.persistence.Persistence.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.FilterSetDao;
import org.bluewindows.figures.dao.admin.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.persistence.Persistence;
import org.bluewindows.figures.filter.Filter;
import org.bluewindows.figures.service.ServiceFactory;

public class FilterSetDaoImplSqlite extends AbstractDaoImplSqlite implements FilterSetDao {

	public FilterSetDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		super(persistenceJdbc);
	}

	@Override
	public CallResult getSets() {
		CallResult result = persistenceAdmin.executeQueryStatement("Select * From " + FILTER_SET_STORE_NAME + " ORDER BY " + NAME);
		if (result.isGood()) {
			ResultSet resultSet = (ResultSet) result.getReturnedObject();
			result = mapFilterSets(resultSet);
			closeResultSet(resultSet);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CallResult getSet(int filterSetID) {
		CallResult result = persistenceAdmin.executeQueryStatement("Select * From " + FILTER_SET_STORE_NAME + " Where " + ID + " = " + filterSetID);
		if (result.isBad()) return result;
		ResultSet resultSet = (ResultSet) result.getReturnedObject();
		result = mapFilterSets(resultSet);
		closeResultSet(resultSet);
		if (result.isBad()) return result;
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		if (filterSets.isEmpty()) return result.setReturnedObject(FilterSet.NONE);
		result.setReturnedObject(((List<FilterSet>)result.getReturnedObject()).get(0));
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CallResult getLastSet() {
		CallResult result = persistenceAdmin.executeQueryStatement("Select * From " + FILTER_SET_STORE_NAME + " Where " + ID +
			" = (Select Max(" + ID + ") From " + FILTER_SET_STORE_NAME + ")");
		if (result.isBad()) return result;
		ResultSet resultSet = (ResultSet) result.getReturnedObject();
		result = mapFilterSets(resultSet);
		closeResultSet(resultSet);
		if (result.isBad()) return result;
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		if (result.isBad()) return result;
		if (filterSets.isEmpty()) return result.setCallBad("Filter Set Retrieval Failure", "Filter set not found.");
		result.setReturnedObject(((List<FilterSet>)result.getReturnedObject()).get(0));
		return result;
	}

	@Override
	public CallResult addSet(FilterSet filterSet) {
		CallResult result = new CallResult();
		try {
			PreparedStatement pStmt = persistenceAdmin.prepareStatement("INSERT INTO " + FILTER_SET_STORE_NAME + " " +
					"(" + NAME + ", " +
					DEFAULT_FIELD + ", " +
					DEFAULT_EXPRESSION + ", " +
					DEFAULT_RESULT + ") VALUES(?,?,?,?)");
			pStmt.setString(1, filterSet.getName());
			pStmt.setString(2, filterSet.getDefaultColumn());
			pStmt.setString(3, filterSet.getDefaultExpression());
			pStmt.setString(4, filterSet.getDefaultResult());
			pStmt.executeUpdate();
			pStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Add Filter Set Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult deleteSet(int filterSetID) {
		ServiceFactory.getInstance().getPersistenceSvc().startTransaction();
		CallResult result = persistenceAdmin.executeUpdateStatement("DELETE FROM " + FILTER_STORE_NAME + " " +
				"WHERE " + FILTER_SET_ID + " = " + filterSetID);
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		result = persistenceAdmin.executeUpdateStatement("DELETE FROM " + FILTER_SET_STORE_NAME + " " +
			"WHERE " + ID + " = " + filterSetID);
		if (result.isBad()) {
			ServiceFactory.getInstance().getPersistenceSvc().rollBackTransaction();
			return result;
		}
		ServiceFactory.getInstance().getPersistenceSvc().commitTransaction();
		return result;
	}

	@Override
	public CallResult updateSet(FilterSet filterSet) {
		return persistenceAdmin.executeUpdateStatement("UPDATE " + FILTER_SET_STORE_NAME + " " +
			"SET " + NAME + " = '" + filterSet.getName() + "', " +
			DEFAULT_FIELD + " = '" + filterSet.getDefaultColumn() + "', " + 
			DEFAULT_EXPRESSION + " = '" + filterSet.getDefaultExpression() + "', " + 
			DEFAULT_RESULT + " = '" + filterSet.getDefaultResult() + "' " + 
			"WHERE " + ID + " = " + filterSet.getID());
	}
	
	@Override
	public CallResult checkSet(int id){
		CallResult result = persistenceAdmin.executeQueryStatement("Select 1 From " + FILTER_SET_STORE_NAME +
			" Where " + Persistence.ID + " = " + id);
		if (result.isBad()) return result;
		try {
			ResultSet rs = (ResultSet)result.getReturnedObject();
			if (rs.next()) {
				closeResultSet(rs);
				return result;
			}
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Check Filter Set Failure", e.getLocalizedMessage());
		}
		return result.setCallBad("Filter Set Lookup Failure", "Filter set not found.");
	}

	private CallResult mapFilterSets(ResultSet rs) {
		CallResult result = new CallResult();
		List<FilterSet> filterSets = new ArrayList<FilterSet>();
		try {
			while (rs.next()) {
				result = makeFilterSet(rs);
				if (result.isBad()) return result;
				filterSets.add((FilterSet) result.getReturnedObject());
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Map Filter Sets Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(filterSets);
	}
	
	@SuppressWarnings("unchecked")
	private CallResult makeFilterSet(ResultSet rs){
		CallResult result = new CallResult();
		FilterSet fs = null;
		try {
			int Id = rs.getInt(ID);
			String name = rs.getString(NAME);
			String defaultField = rs.getString(DEFAULT_FIELD);
			String defaultExpression = rs.getString(DEFAULT_EXPRESSION);
			String defaultResult = rs.getString(DEFAULT_RESULT);
			fs = new FilterSet(Id, name, defaultField, defaultExpression, defaultResult);
			CallResult filterResult = ServiceFactory.getInstance().getPersistenceSvc().getFilters(Id);
			if (filterResult.isBad()) return filterResult;
			fs.setFilters((List<Filter>) filterResult.getReturnedObject());
			result.setReturnedObject(fs);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Map Filter Set Failure", e.getLocalizedMessage());
		}
		return result;
	}


}
