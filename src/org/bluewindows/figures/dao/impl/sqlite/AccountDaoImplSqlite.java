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
import org.bluewindows.figures.dao.AccountDao;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.figures.service.ServiceFactory;

public class AccountDaoImplSqlite extends AbstractDaoImplSqlite implements AccountDao  {
	
	public static int BATCH_SIZE = 5000;
	
	public AccountDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		super(persistenceJdbc);
	}

	@Override
	public CallResult getAccounts() {
		CallResult result = executeQueryStatement("Select * From " + ACCOUNT_STORE_NAME + " " +
	    	"ORDER BY " + NAME);
		ResultSet resultSet = (ResultSet)result.getReturnedObject();
		if (result.isGood()){
			result = mapAccounts(resultSet);
		}
		closeResultSet(resultSet);
		return result;
	}
	
	@Override
	public CallResult getAccount(int accountID) {
		CallResult result = executeQueryStatement("Select * From " + ACCOUNT_STORE_NAME + " " +
	    	"Where " + ID + " = " + accountID);
		if (result.isBad()) return result;
		ResultSet resultSet = (ResultSet)result.getReturnedObject();
		result = mapAccounts(resultSet);
		if (result.isBad()) return result;
		closeResultSet(resultSet);
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>) result.getReturnedObject();
		if (accounts.size() == 1) {
			result.setReturnedObject(accounts.get(0));
		}else {
			result.setCallBad("Error Retrieving Account", "No Account with ID " + accountID + " found.");
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public CallResult getLastAccount() {
		CallResult result = executeQueryStatement("Select * From " + ACCOUNT_STORE_NAME +
			" Where ID = (Select Max(" + ID + ") From " + ACCOUNT_STORE_NAME + ")");
		if (result.isBad()) return result;
		ResultSet resultSet = (ResultSet)result.getReturnedObject();
		result = mapAccounts(resultSet);
		closeResultSet(resultSet);
		if (result.isBad()) return result;
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		if (accounts.isEmpty()) return result.setCallBad("Account Retrieval Failure","Account Not found");
		result.setReturnedObject(((List<Account>)result.getReturnedObject()).get(0));
		return result;
	}
	
	@Override
	public CallResult addAccount(Account account) {
		CallResult result = new CallResult();
		if (account.getFilterSet() != null && !account.getFilterSet().equals(FilterSet.NONE)){
			result = checkFilterSet(account.getFilterSet().getID());
			if (result.isBad() && result.getErrorMessage().equals("Filter set not found.")) {
				return result.setCallBad("Account Update Failed", "Invalid filter set ID: " + account.getFilterSet().getID());
			}
		}else {
			account.setFilterSet(FilterSet.NONE);
		}
		try {
			PreparedStatement pStmt = prepareStatement("INSERT INTO " + ACCOUNT_STORE_NAME + 
				" (" + NAME + ", " + TYPE + ", " +
				FILTER_SET_ID + ", " + INITIAL_BALANCE + ", " + LAST_LOAD_DATE + ", " + LAST_FILTER_DATE + ") VALUES(?,?,?,?,?,?)");
			pStmt.setString(1,account.getName());
			pStmt.setString(2, account.getType().toString());
			pStmt.setInt(3, account.getFilterSet().getID());
			pStmt.setDouble(4, Double.valueOf(account.getInitialBalance().toStringNumber()));
			pStmt.setString(5, account.getLastLoadedDate().value().format(JDBC_DATE_FORMAT));
			pStmt.setString(6, account.getLastFilteredDate().value().format(JDBC_DATE_FORMAT));
			pStmt.executeUpdate();
			pStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Account Update Failed", e.getLocalizedMessage());
		}
		account.setID(getNewAccountID());
		return result;
	}

	@Override
	public CallResult updateAccount(Account account) {
		return executeUpdateStatement("UPDATE " + ACCOUNT_STORE_NAME + " " +
			"SET " + NAME + " = '" + account.getName() + "' " +
			", " + FILTER_SET_ID + " = " + account.getFilterSet().getID() + " " +
			", " + INITIAL_BALANCE + " = " + account.getInitialBalance().getValue() + " " +
			", " + LAST_LOAD_DATE + " = '" + account.getLastLoadedDate().value().format(JDBC_DATE_FORMAT) + "' " +
			", " + LAST_FILTER_DATE + " = '" + account.getLastFilteredDate().value().format(JDBC_DATE_FORMAT) + "' " +
			", " + IMPORT_FOLDER + " = '" + account.getImportFolder() + "' " +
			"WHERE " + ID + " = " + account.getID());
	}

	@Override
	public CallResult deleteAccount(Account account) {
		return executeUpdateStatement("DELETE FROM " + ACCOUNT_STORE_NAME + " WHERE " + ID + " = " + account.getID());
	}

	private CallResult mapAccounts(ResultSet rs) {
		CallResult result = new CallResult();
		List<Account> accounts = new ArrayList<Account>();
		try {
			while (rs.next()) {
				int filterSetID = rs.getInt(FILTER_SET_ID);
				result = ServiceFactory.getInstance().getPersistenceSvc().getFilterSet(filterSetID);
				if (result.isBad()) return result;
				FilterSet filterSet = result.getReturnedObject() == null? FilterSet.NONE: (FilterSet)result.getReturnedObject();
				AccountType type = AccountType.valueOf(rs.getString(TYPE));
				result = mapDate(rs, LAST_LOAD_DATE);
				if (result.isBad()) return result;
				TransactionDate lastLoadDate = (TransactionDate)result.getReturnedObject();
				result = mapDate(rs, LAST_FILTER_DATE);
				if (result.isBad()) return result;
				Account account = new Account(rs.getInt(ID), rs.getString(NAME), type, filterSet, 
						new Money(rs.getString(INITIAL_BALANCE)), lastLoadDate, (TransactionDate)result.getReturnedObject());
				if (rs.getString(IMPORT_FOLDER) != null && !rs.getString(IMPORT_FOLDER).isEmpty()) {
					account.setImportFolder(rs.getString(IMPORT_FOLDER));
				}
				accounts.add(account);
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Account Retrieval Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(accounts);
	}

	private int getNewAccountID(){
		CallResult result = executeQueryStatement("Select max(" + ID + ") as ID From " + ACCOUNT_STORE_NAME);
		if (result.isBad()) return 0;
		ResultSet rs = (ResultSet)result.getReturnedObject();
		result = mapInteger(rs, ID);
		closeResultSet(rs);
		if (result.isBad()) return 0;
		return ((Integer)result.getReturnedObject()).intValue();
	}
	
}
