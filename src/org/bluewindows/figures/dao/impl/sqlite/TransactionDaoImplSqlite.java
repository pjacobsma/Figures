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

import static org.bluewindows.figures.domain.persistence.Persistence.ACCOUNT_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.BALANCE;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.DEDUCTIBLE;
import static org.bluewindows.figures.domain.persistence.Persistence.DESCRIPTION;
import static org.bluewindows.figures.domain.persistence.Persistence.END_DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.ID;
import static org.bluewindows.figures.domain.persistence.Persistence.MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.NUMBER;
import static org.bluewindows.figures.domain.persistence.Persistence.ORIG_DESC;
import static org.bluewindows.figures.domain.persistence.Persistence.ORIG_MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.START_DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_CATEGORY;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_DEDUCTIBLE;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_DESC;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_MEMO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.TransactionDao;
import org.bluewindows.figures.dao.admin.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;

public class TransactionDaoImplSqlite extends AbstractDaoImplSqlite implements TransactionDao  {
	
	public final static int BATCH_SIZE = 5000;
	
	public TransactionDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		super(persistenceJdbc);
	}

	@Override
	public CallResult getTransactionDateRange(Account account) {
		CallResult result = persistenceAdmin.executeQueryStatement("Select Min(" + DATE + ") as " + START_DATE + 
			", Max(" + DATE + ") as " + END_DATE + " From " + TRANSACTION_STORE_NAME + " " +
			"Where " + ACCOUNT_ID + " = " + account.getID()); 
		if (result.isGood()) {
			ResultSet resultSet = (ResultSet) result.getReturnedObject();
			result =  persistenceAdmin.mapDateRange(resultSet);
			closeResultSet(resultSet);
		}
		return result;
	}
	
	@Override
	public CallResult getTransactions(Account account) {
		String query = buildTransactionQuery(account);
		CallResult result = persistenceAdmin.executeQueryStatement(query);
		if (result.isBad()){
			return result;
		}
		List<Transaction> transactionList = new ArrayList<Transaction>();
		try {
			ResultSet resultSet = (ResultSet) result.getReturnedObject();
			Transaction transaction = null;
			List<TransactionCategory> categories = null;
			Integer transactionID = Integer.MIN_VALUE;
			while(resultSet.next()){
				Integer thisID = resultSet.getInt(ID);
				if (!thisID.equals(transactionID)) { //Start of new transaction
					if (transaction != null) {
						transaction.initializeCategories(categories);
						transaction.setUpdated(false);
						transactionList.add(transaction);
					}
					transactionID = thisID;
					transaction = new Transaction();
					categories = new ArrayList<TransactionCategory>();
					transaction.setID(resultSet.getInt(ID));
					transaction.setNumber(resultSet.getString(NUMBER));
					result = persistenceAdmin.mapDate(resultSet, DATE);
					if (result.isBad()) return result;
					transaction.setDate((TransactionDate)result.getReturnedObject());
					transaction.setDescription(resultSet.getString(DESCRIPTION));
					transaction.setAmount(new Money(resultSet.getBigDecimal(AMOUNT)));
					transaction.setDeductible(resultSet.getBoolean(DEDUCTIBLE));
					transaction.setMemo(resultSet.getString(MEMO));
					transaction.setOriginalDescription(resultSet.getString(ORIG_DESC));
					transaction.setOriginalMemo(resultSet.getString(ORIG_MEMO));
					transaction.setUserChangedDesc(resultSet.getBoolean(USER_CHANGED_DESC));
					transaction.setUserChangedMemo(resultSet.getBoolean(USER_CHANGED_MEMO));
					transaction.setUserChangedCategory(resultSet.getBoolean(USER_CHANGED_CATEGORY));
					transaction.setUserChangedDeductible(resultSet.getBoolean(USER_CHANGED_DEDUCTIBLE));
					transaction.setBalance(new Money(resultSet.getBigDecimal(BALANCE)));
				}
				TransactionCategory transactionCategory = 
						new TransactionCategory(resultSet.getInt(TRANSACTION_CATEGORY_ID), resultSet.getInt(CATEGORY_ID), resultSet.getString(NAME), new Money(resultSet.getString(CATEGORY_AMOUNT)));
				categories.add(transactionCategory);
			}
			if (transaction != null) { // Add last transaction
				transaction.initializeCategories(categories);
				transaction.setUpdated(false);
				transactionList.add(transaction);
			}
			account.setTransactions(transactionList);
			closeResultSet(resultSet);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Transaction Retrieval Failure", e.getLocalizedMessage());
		}catch (IllegalArgumentException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Transaction Retrieval Failure", e.getLocalizedMessage());
		}
		return result;
	}

	private String buildTransactionQuery(Account account) {
		return "SELECT * FROM " + TRANSACTION_STORE_NAME + " t " +
			   "LEFT OUTER JOIN " + TRANSACTION_CATEGORY_STORE_NAME + " tc " +
			   "ON t." + ID + " = tc." + TRANSACTION_ID + " " +
			   "LEFT OUTER JOIN " + CATEGORY_STORE_NAME + " c " +
			   "ON tc." + CATEGORY_ID + " = c." + ID + " " +
			   "WHERE t." + ACCOUNT_ID + " = " + account.getID() + " " +
			   "ORDER BY t." + DATE + " DESC, " + " " +
			   "t." + ID + " DESC, c." + NAME + " ASC "; // Name sorts categories within a transaction
	}

	// Used for adding new transactions to the database
	@Override
	public CallResult addTransactions(List<Transaction> transactions, Account account) {
		CallResult result = new CallResult();
		if (transactions.size() == 0){
			return result.setCallBad("Transaction Insert Failure", "No transactions to insert.");
		}
		try {
			PreparedStatement transactionInsertStatement = persistenceAdmin.prepareStatement(getTransactionInsertStatement());
			PreparedStatement categoryInsertStatement = persistenceAdmin.prepareStatement(getCategoryInsertStatement());
			if (result.isBad()) return result;
			for (Transaction transaction : transactions) {
				setTransactionParameters(transactionInsertStatement, account, transaction);
				transactionInsertStatement.executeUpdate();
				// Get the transaction ID so we can use it on the TransactionCategory table
				CallResult rowIdResult = persistenceAdmin.executeQueryStatement("select seq from sqlite_sequence where name = '" + TRANSACTION_STORE_NAME + "'");
				if (rowIdResult.isBad()) return rowIdResult;
				ResultSet resultSet = (ResultSet)rowIdResult.getReturnedObject();
				transaction.setID(resultSet.getInt(1));
				for (TransactionCategory transactionCategory : transaction.getAddedCategories()) {
					setCategoryInsertParameters(categoryInsertStatement, transaction, transactionCategory);
					categoryInsertStatement.executeUpdate();
				}
				closeResultSet(resultSet);
			}
			transactionInsertStatement.close();
			categoryInsertStatement.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Transaction Insert Failure", e.getLocalizedMessage());
		}
		return result;
	}

	// Find distinct values of description or memo fields as potential filter targets.
	// Include only uncategorized transactions
	// Include only transactions from the startDate forward
	// Optionally include only deposits or withdrawals
	@Override
	public CallResult getDistinctValues(Account account, String field, TransactionDate startDate, boolean depositsOnly, boolean withdrawalsOnly) {
		String whereClause = "Where t." + ACCOUNT_ID + " = " + account.getID() + " " +
			"And tc." + CATEGORY_ID + " = 0 ";
		// Exclude checks for checking accounts
		if (account.getType().equals(AccountType.CHECKING)){
			whereClause = whereClause + "And t." + NUMBER + " = '' ";
		}
		whereClause = whereClause + "And " + DATE + " >= '" + startDate.value().format(JDBC_DATE_FORMAT) + "' ";
		if (depositsOnly) {
			whereClause = whereClause + "And " + AMOUNT + " > 0 ";
		}
		if (withdrawalsOnly) {
			whereClause = whereClause + "And " + AMOUNT + " < 0 ";
		}
		CallResult result = persistenceAdmin.executeQueryStatement("Select Distinct " + field + " " +
 			"From " + TRANSACTION_STORE_NAME + " t Left Outer Join " + TRANSACTION_CATEGORY_STORE_NAME + " tc " +
			"On t." + ID + " = tc." + TRANSACTION_ID + " " +
			whereClause + " Order by 1 ");
		List<String> strings = new ArrayList<String>();
		if (result.isGood()) {
			ResultSet resultSet = (ResultSet) result.getReturnedObject();
			try {
				while(resultSet.next()) {
					strings.add(resultSet.getString(1));
				}
			} catch (SQLException e) {
				Figures.logStackTrace(e);
				result.setCallBad("Data Retrieval Failure", e.getLocalizedMessage());
			}
			closeResultSet(resultSet);
		}
		result.setReturnedObject(strings);
		return result;
	}
	
	@Override
	public CallResult updateTransaction(Transaction transaction) {
		CallResult result = new CallResult();
		try {
			PreparedStatement transactionUpdateStmt = persistenceAdmin.prepareStatement(getTransactionUpdateStatement());
			setTransactionUpdateParameters(transactionUpdateStmt, transaction);
			transactionUpdateStmt.executeUpdate();
			PreparedStatement categoryInsertStmt = persistenceAdmin.prepareStatement(getCategoryInsertStatement());
			if (transaction.isCategoryUpdated()) {
				for (TransactionCategory category : transaction.getDeletedCategories()) {
					result = persistenceAdmin.executeUpdateStatement("Delete from " + TRANSACTION_CATEGORY_STORE_NAME + " " +
						"Where " + TRANSACTION_CATEGORY_ID + " = " + category.getTransactionCategoryID());
					if (result.isBad()) {
						return result;
					}
				}
				for (TransactionCategory category : transaction.getAddedCategories()) {
					setCategoryInsertParameters(categoryInsertStmt, transaction, category);
					categoryInsertStmt.executeUpdate();
				}
			}
			transactionUpdateStmt.close();
			categoryInsertStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Transaction Update Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult updateTransactions(List<Transaction> transactions) {
		CallResult result = new CallResult();
		int updateCount = 0;
		try {
			PreparedStatement transactionUpdateStmt = persistenceAdmin.prepareStatement(getTransactionUpdateStatement());
			PreparedStatement categoryInsertStmt = persistenceAdmin.prepareStatement(getCategoryInsertStatement());
			PreparedStatement categoryDeleteStmt = persistenceAdmin.prepareStatement(getCategoryDeleteStatement());
			int tranCount = 0;
			while (tranCount < transactions.size()){
				int batchCount = 0;
				while (batchCount < BATCH_SIZE && tranCount < transactions.size()){
					Transaction transaction = transactions.get(tranCount);
					tranCount++;
					batchCount++;
					updateCount++;
					setTransactionUpdateParameters(transactionUpdateStmt, transaction);
					transactionUpdateStmt.executeUpdate();
					if (transaction.isCategoryUpdated()) {
						for (TransactionCategory transactionCategory : transaction.getDeletedCategories()) {
							setCategoryDeleteParameters(categoryDeleteStmt, transactionCategory);
							categoryDeleteStmt.executeUpdate();
						}
						for (TransactionCategory transactionCategory : transaction.getAddedCategories()) {
							setCategoryInsertParameters(categoryInsertStmt, transaction, transactionCategory);
							categoryInsertStmt.executeUpdate();
						}
					}
				}
			}
			transactionUpdateStmt.close();
			categoryInsertStmt.close();
			categoryDeleteStmt.close();
		} catch (Exception e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Transaction Update Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(Integer.valueOf(updateCount));
	}
	
	@Override
	public CallResult deleteTransactions(Account account) {
		CallResult result = new CallResult();
		try {
			PreparedStatement transactionDeleteStmt = persistenceAdmin.prepareStatement(getAccountTransactionDeleteStatement());
			PreparedStatement categoryDeleteStmt = persistenceAdmin.prepareStatement(getCategoryDeleteStatement());
			for (Transaction transaction : account.getTransactions()) {
				for (TransactionCategory transactionCategory : transaction.getCategories()) {
					setCategoryDeleteParameters(categoryDeleteStmt, transactionCategory);
					categoryDeleteStmt.executeUpdate();
				}
			}
			categoryDeleteStmt.close();
			transactionDeleteStmt.setInt(1, account.getID());
			transactionDeleteStmt.executeUpdate();
			transactionDeleteStmt.close();
		} catch (Exception e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Account Transaction Delete Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	private String getTransactionInsertStatement() {
		String transactionInsert = "INSERT INTO " + TRANSACTION_STORE_NAME + " " +
		"(" + 
		ACCOUNT_ID + ", " +
		NUMBER + ", " +
		DATE + ", " + 
		DESCRIPTION + ", " + 
		AMOUNT + ", " +
		MEMO + ", " + 
		DEDUCTIBLE + ", " + 
		ORIG_DESC + ", " + 
		ORIG_MEMO + ", " + 
		USER_CHANGED_DESC + ", " + 
		USER_CHANGED_MEMO + ", " + 
		USER_CHANGED_CATEGORY + ", " + 
		USER_CHANGED_DEDUCTIBLE+ ", " +  
		BALANCE +  
		") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return transactionInsert;
	}
	
	private void setTransactionParameters(PreparedStatement transactionStatement, Account account, Transaction transaction) throws SQLException {
		transactionStatement.setInt   (1, account.getID());
		transactionStatement.setString(2, transaction.getNumber());
		transactionStatement.setString(3, transaction.getDate().value().format(JDBC_DATE_FORMAT));
		transactionStatement.setString(4, transaction.getDescription());
		transactionStatement.setDouble(5, Double.valueOf(transaction.getAmount().toStringNumber()));
		transactionStatement.setString(6, transaction.getMemo());
		transactionStatement.setInt   (7, transaction.isDeductible()? 1: 0);
		transactionStatement.setString(8, transaction.getOriginalDescription());
		transactionStatement.setString(9, transaction.getOriginalMemo());
		transactionStatement.setInt   (10, transaction.isUserChangedDesc()? 1: 0);
		transactionStatement.setInt   (11, transaction.isUserChangedMemo()? 1: 0);
		transactionStatement.setInt   (12, transaction.isUserChangedCategory()? 1: 0);
		transactionStatement.setInt   (13, transaction.isUserChangedDeductible()? 1: 0);
		transactionStatement.setDouble(14, Double.valueOf(transaction.getBalance().toStringNumber()));
	}
	
	private String getAccountTransactionDeleteStatement() {
		String transactionDelete = "DELETE FROM " + TRANSACTION_STORE_NAME + " " +
			"WHERE " + ACCOUNT_ID + " = ? ";
		return transactionDelete;
	}

	private String getCategoryInsertStatement() {
		String categoryInsert = "INSERT INTO " + TRANSACTION_CATEGORY_STORE_NAME + " " +
			"(" + 
			TRANSACTION_ID + ", " +
			CATEGORY_ID + ", " +
			CATEGORY_AMOUNT +
			") VALUES(?,?,?)";
		return categoryInsert;
	}
	
	private void setCategoryInsertParameters(PreparedStatement categoryInsertStatement, Transaction transaction,
			TransactionCategory transactionCategory) throws SQLException {
		categoryInsertStatement.setInt(1, transaction.getID());
		categoryInsertStatement.setInt(2, transactionCategory.getCategoryID());
		categoryInsertStatement.setDouble(3, Double.valueOf(transactionCategory.getAmount().toStringNumber()));
	}
	
	private String getCategoryDeleteStatement() {
		String categoryDelete = "DELETE FROM " + TRANSACTION_CATEGORY_STORE_NAME + 
			" WHERE " + TRANSACTION_CATEGORY_ID + " = ? ";
		return categoryDelete;
	}

	private void setCategoryDeleteParameters(PreparedStatement categoryDeleteStatement, TransactionCategory transactionCategory) throws SQLException {
		categoryDeleteStatement.setInt(1, transactionCategory.getTransactionCategoryID());
	}

	private String getTransactionUpdateStatement() {
		String statement = "UPDATE " + TRANSACTION_STORE_NAME + " " +
		"SET " + DESCRIPTION + " = ?" +
		", " +   MEMO + " = ?" +
		", " +   DEDUCTIBLE + " = ?" +
		", " +   USER_CHANGED_DESC + " = ?" +
		", " +   USER_CHANGED_MEMO + " = ? " +
		", " +   USER_CHANGED_CATEGORY + " = ? " +
		", " +   USER_CHANGED_DEDUCTIBLE + " = ? " +
		", " +   BALANCE + " = ? " +
		"WHERE " + ID + " = ? ";
		return statement;
	}

	private void setTransactionUpdateParameters(PreparedStatement pStmt, Transaction tran) throws SQLException {
		pStmt.setString (1, tran.getDescription());
		pStmt.setString (2, tran.getMemo());
		pStmt.setInt    (3, tran.isDeductible()? 1: 0);
		pStmt.setInt    (4, tran.isUserChangedDesc()? 1: 0);
		pStmt.setInt    (5, tran.isUserChangedMemo()? 1: 0);
		pStmt.setInt    (6, tran.isUserChangedCategory()? 1: 0);
		pStmt.setInt    (7, tran.isUserChangedDeductible()? 1: 0);
		pStmt.setDouble (8, Double.valueOf(tran.getBalance().toStringNumber()));
		pStmt.setInt    (9, tran.getID());
	}

}
