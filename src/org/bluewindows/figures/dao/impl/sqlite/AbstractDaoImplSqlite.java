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

import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_SET_STORE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.domain.persistence.Persistence;


public abstract class AbstractDaoImplSqlite {

	protected PersistenceAdminDaoImplSqlite persistenceAdmin;
	public static DateTimeFormatter JDBC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public AbstractDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		this.persistenceAdmin = persistenceJdbc;
	}
	
	protected CallResult executeDefinitions(LinkedList<String> definitions){
		CallResult result = persistenceAdmin.checkConnection();
		if (result.isBad()) return result;
		for (Iterator<String> defs = definitions.iterator(); defs.hasNext();) {
			String definition = (String) defs.next();
			result = executeUpdateStatement(definition);
			if (result.isBad()) return result;
		}
		return result;
	}
	
	protected CallResult executeQueryStatement(String sql){
		CallResult result = persistenceAdmin.checkConnection();
		if (result.isBad()) return result;
		ResultSet rs;
		try {
			Statement stmt = persistenceAdmin.getConnection().createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Query Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(rs);
	}

	protected CallResult executeUpdateStatement(String sql){
		CallResult result = persistenceAdmin.checkConnection();
		if (result.isBad()) return result;
		try {
			Statement stmt = persistenceAdmin.getConnection().createStatement();
			int stmtResult = stmt.executeUpdate(sql);
			result.setReturnedObject(Integer.valueOf(stmtResult));
			stmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			String message = this.getClass().getSimpleName();
			message = message + ", " + e.getLocalizedMessage();
			result.setCallBad("Update Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	protected PreparedStatement prepareStatement(String sql) throws SQLException{
		CallResult result = persistenceAdmin.checkConnection();
		if (result.isBad()) throw new SQLException("Unable to connect to the database.");
		PreparedStatement pStmt = persistenceAdmin.getConnection().prepareStatement(sql);
		return pStmt;
	}
	
	protected CallResult mapInteger(ResultSet rs, String fieldName) {
		CallResult result = new CallResult();
		try {
			if (rs.next()) {
				result.setReturnedObject(Integer.valueOf(rs.getInt(fieldName)));
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Integer Map Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	protected CallResult mapDate(ResultSet rs, String fieldName) {
		CallResult result = new CallResult();
		String dateString = null;
		try {
			dateString = rs.getString(fieldName);
			if (dateString != null) dateString = StringUtils.remove(dateString, '-');
		} catch (SQLException se) {
			Figures.logStackTrace(se);
			result.setCallBad("Date Map Failure", se.getLocalizedMessage());
		}
		try {
			if (dateString == null) {
				result.setReturnedObject(null);
			}else {
				result.setReturnedObject(new TransactionDate(dateString));
			}
		} catch (ParseException pe) {
			Figures.logStackTrace(pe);
			result.setCallBad("Date Map Failure for " + dateString, pe.getLocalizedMessage());
		} catch (IllegalArgumentException iae) {
			Figures.logStackTrace(iae);
			result.setCallBad("Date Map Failure for " + dateString, iae.getLocalizedMessage());
		}
		return result;
	}
	
	protected CallResult mapDateRange(ResultSet rs) {
		CallResult result = new CallResult();
		DateRange dateRange = new DateRange();
		int rowNumber = 0;
		try {
			if (rs.next()) {
				rowNumber++;
				result = mapDate(rs, Persistence.START_DATE);
				if (result.isBad()) return decorateResult(result, rowNumber);
				dateRange.setStartDate((TransactionDate)result.getReturnedObject());
				result = mapDate(rs, Persistence.END_DATE);
				if (result.isBad()) return decorateResult(result, rowNumber);
				dateRange.setEndDate((TransactionDate)result.getReturnedObject());
				result.setReturnedObject(dateRange);
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Map Date Range Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	protected CallResult decorateResult(CallResult result, int rowNumber){
		result.setMessageDecorator("  Occured at transaction number: " + rowNumber);
		return result;
	}

	protected CallResult decorateResult(CallResult result){
		result.setMessageDecorator("  Occured in " + this.getClass().getName());
		return result;
	}

	protected CallResult checkFilterSet(int id){
		CallResult result = executeQueryStatement("Select 1 From " + FILTER_SET_STORE_NAME +
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
	
	protected void closeResultSet(ResultSet resultSet) {
		try {
			resultSet.getStatement().close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
		}
	}
	
}
