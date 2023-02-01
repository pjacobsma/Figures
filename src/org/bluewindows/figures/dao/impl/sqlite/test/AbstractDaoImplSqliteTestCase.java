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

import static org.bluewindows.figures.domain.persistence.Persistence.ACCOUNT_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.DEFAULT_EXPRESSION;
import static org.bluewindows.figures.domain.persistence.Persistence.DEFAULT_FIELD;
import static org.bluewindows.figures.domain.persistence.Persistence.DEFAULT_RESULT;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_SET_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.ID;
import static org.bluewindows.figures.domain.persistence.Persistence.NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_STORE_NAME;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.app.PersistenceType;
import org.bluewindows.figures.dao.impl.sqlite.AbstractDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterResult;
import org.bluewindows.figures.service.PersistenceService;
import org.bluewindows.figures.service.ServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class AbstractDaoImplSqliteTestCase {
	
	protected static PersistenceService persistence;
	protected static PersistenceAdminDaoImplSqlite persistenceAdmin;
	protected static Properties properties;
	protected static DaoImplJdbc daoImplJdbc;
	protected static int transactionCategoryID = 1;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		ServiceFactory.getInstance().setUpPersistenceSvcSqlite();
		persistence = ServiceFactory.getInstance().getPersistenceSvc();
		persistenceAdmin = (PersistenceAdminDaoImplSqlite)persistence.getPersistenceAdminDao();
		properties = new Properties();
		properties.setProperty(Figures.PERSISTENCE_TYPE, PersistenceType.SQLITE.name());
		properties.setProperty(PersistenceType.SQLITE.name(), "org.sqlite.JDBC");
		properties.setProperty(Figures.DATE_FORMAT_NAME, Figures.DEFAULT_DATE_FORMAT);
		Figures.dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		Figures.setProperties(properties);
		Figures.configureLogging();
	}
	
	@Before
	public void before(){
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().openNew(":memory:", null);
		assertTrue(result.isGood());
		daoImplJdbc = new AbstractDaoImplSqliteTestCase.DaoImplJdbc(persistenceAdmin);
	}
	
	@After
	public void after(){
		ServiceFactory.getInstance().getPersistenceSvc().close();
	}
	
	protected Account saveTestAccount(String name, int accountID) throws Exception{
		CallResult result = daoImplJdbc.executeUpdateStatement("INSERT INTO " + ACCOUNT_STORE_NAME + 
			" values(" + accountID + ",'" + name + "', '"  + AccountType.CHECKING.toString() + "',0,0,'" +
			19990101 + "','C:\\Imports')"); 
		assertTrue(result.isGood());
		Account account = new Account(accountID);
		account.setName(name);
		return account;
	}

	protected void saveTestCategory(String name){
		CallResult result = daoImplJdbc.executeUpdateStatement("INSERT INTO " + CATEGORY_STORE_NAME + 
			" (" + NAME + ") VALUES('" + name + "')");
		assertTrue(result.isGood());
	}
	
	protected int countAccountRows(int accountID) throws SQLException {
		Statement stmt = persistenceAdmin.getConnection().createStatement();
		int rowCount = 0;
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + ACCOUNT_STORE_NAME + 
			" WHERE " + ID + " = " + accountID);
		if (rs.next()){
			rowCount = rs.getInt(1);
		}
		rs.getStatement().close();
		return rowCount;
	}
	
	protected int countSummaryAccountRows(int summaryID, int accountID) throws SQLException {
		Statement stmt = persistenceAdmin.getConnection().createStatement();
		int rowCount = 0;
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + SUMMARY_ACCOUNT_STORE_NAME + 
			" WHERE " + SUMMARY_ID + " = " + summaryID + " AND " +
			ACCOUNT_ID + " = " + accountID);
		if (rs.next()){
			rowCount = rs.getInt(1);
		}
		rs.getStatement().close();
		return rowCount;
	}
	
	protected int countTransactionRows(int transactionID) throws SQLException {
		Statement stmt = persistenceAdmin.getConnection().createStatement();
		int rowCount = 0;
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TRANSACTION_STORE_NAME + 
			" WHERE " + ID + " = " + transactionID);
		if (rs.next()){
			rowCount = rs.getInt(1);
		}
		rs.getStatement().close();
		return rowCount;
	}
	
	protected int countTransactionCategoryRows(int transactionID) throws SQLException {
		Statement stmt = persistenceAdmin.getConnection().createStatement();
		int rowCount = 0;
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TRANSACTION_CATEGORY_STORE_NAME + 
			" WHERE " + TRANSACTION_ID + " = " + transactionID);
		if (rs.next()){
			rowCount = rs.getInt(1);
		}
		rs.getStatement().close();
		return rowCount;
	}

	protected FilterSet saveTestFilterSet(int filterSetID, String name, String defaultField, String defaultExpression, String defaultResult){
		CallResult result = daoImplJdbc.executeUpdateStatement("INSERT INTO " + FILTER_SET_STORE_NAME + 
			" (" + ID + ", " + NAME + ", " + DEFAULT_FIELD + ", " + DEFAULT_EXPRESSION + ", " + DEFAULT_RESULT +") "+
			"VALUES(" + filterSetID + ", '" + name + "', '" + defaultField + "', '" + defaultExpression + "', '" + defaultResult + "')");
		assertTrue(result.isGood());
		return new FilterSet(filterSetID, name, defaultField, defaultExpression, defaultResult);
	}
	
	protected void saveTestFilter(int filterSetID, int sequence){
		CallResult result = daoImplJdbc.executeUpdateStatement("Insert into " + FILTER_STORE_NAME + 
				" values(null," + filterSetID + "," + sequence + ",'Description','" + FilterExpression.EQUALS + "','Value','" + FilterResult.REPLACE_DESCRIPTION + "','Replacement',1,1)");
		assertTrue(result.isGood());
		assertTrue(result.isGood());
	}
	
	protected Transaction makeTestTransaction() throws Exception {
		Transaction transaction = new Transaction();
		transaction.setNumber("1");
		transaction.setDate("20011231");
		Money amount = new Money("-123.45");
		transaction.setAmount(amount);
		transaction.setDescription("Description");
		transaction.setMemo("Memo");
		transaction.setCategory(new TransactionCategory(++transactionCategoryID, 1, "Appliances", amount));
		transaction.setBalance(amount);
		return transaction;
	}
	
	protected void saveTestTransaction(Account account, Transaction transaction) throws SQLException{
		CallResult result = daoImplJdbc.executeUpdateStatement("Insert into " + TRANSACTION_STORE_NAME + " values(" + 
				transaction.getID() + "," +
				account.getID() + ", " +
				parameterize(transaction.getNumber()) + "," +
				parameterize(transaction.getDate().value().format(AbstractDaoImplSqlite.JDBC_DATE_FORMAT)) + "," +
				parameterize(transaction.getDescription()) + "," +
				parameterize(transaction.getAmount().toStringNumber()) + "," +
				parameterize(transaction.getMemo()) + "," +
				parameterize(transaction.getDeductible()) + "," +
				parameterize(transaction.getOriginalDescription()) + "," +
				parameterize(transaction.getOriginalMemo()) + "," +
				(transaction.isUserChangedDesc()? "'1'": "'0'") + "," +
				(transaction.isUserChangedMemo()? "'1'": "'0'") + "," +
				(transaction.isUserChangedCategory()? "'1'": "'0'") + "," +
				(transaction.isUserChangedDeductible()? "'1'": "'0'") + "," +
				parameterize(transaction.getBalance().toStringNumber()) + ")");
		assertTrue(result.isGood());
		for (TransactionCategory category : transaction.getAddedCategories()) {
			result = daoImplJdbc.executeUpdateStatement("Insert into " + TRANSACTION_CATEGORY_STORE_NAME + "(" +
				TRANSACTION_ID + "," + CATEGORY_ID + "," + CATEGORY_AMOUNT + ") values (" +
				transaction.getID() + "," +
				category.getCategoryID() + "," +
				parameterize(category.getAmount().toStringNumber()) + ")");
			assertTrue(result.isGood());
			// Get the category table ID so we can use it on the TransactionCategory table
			CallResult rowIdResult = executeQueryStatement("select seq from sqlite_sequence where name = '" + TRANSACTION_CATEGORY_STORE_NAME + "'");
			assertTrue(rowIdResult.isGood());
			ResultSet rs = (ResultSet)rowIdResult.getReturnedObject();
			category.setTransactionCategoryID(rs.getInt(1));
		}
		
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
	
	protected String parameterize(String parameter){
		return "'" + parameter + "'";
	}
	
	protected class DaoImplJdbc extends AbstractDaoImplSqlite {
		public DaoImplJdbc(PersistenceAdminDaoImplSqlite persistenceJdbc) {
			super(persistenceJdbc);
		}
		
		public CallResult executeUpdateStatement(String sql){
			return super.executeUpdateStatement(sql);
		}
		
		public CallResult executeQueryStatement(String sql){
			return super.executeQueryStatement(sql);
		}
	}
}
