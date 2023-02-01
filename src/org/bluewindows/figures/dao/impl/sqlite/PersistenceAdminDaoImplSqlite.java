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
import static org.bluewindows.figures.domain.persistence.Persistence.ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.BALANCE;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORIZED_ONLY;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_INCLUSION;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.CHECK_INCLUSION;
import static org.bluewindows.figures.domain.persistence.Persistence.DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.DEDUCTIBLE;
import static org.bluewindows.figures.domain.persistence.Persistence.DEDUCTIBLE_INCLUSION;
import static org.bluewindows.figures.domain.persistence.Persistence.DEFAULT_EXPRESSION;
import static org.bluewindows.figures.domain.persistence.Persistence.DEFAULT_FIELD;
import static org.bluewindows.figures.domain.persistence.Persistence.DEFAULT_RESULT;
import static org.bluewindows.figures.domain.persistence.Persistence.DESCRIPTION;
import static org.bluewindows.figures.domain.persistence.Persistence.END_DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.EXPRESSION;
import static org.bluewindows.figures.domain.persistence.Persistence.FIELD;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_SET_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_SET_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.ID;
import static org.bluewindows.figures.domain.persistence.Persistence.IMPORT_FOLDER;
import static org.bluewindows.figures.domain.persistence.Persistence.INITIAL_BALANCE;
import static org.bluewindows.figures.domain.persistence.Persistence.LAST_LOAD_DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.NUMBER;
import static org.bluewindows.figures.domain.persistence.Persistence.ORIG_DESC;
import static org.bluewindows.figures.domain.persistence.Persistence.ORIG_MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.REPLACEMENT;
import static org.bluewindows.figures.domain.persistence.Persistence.RESULT;
import static org.bluewindows.figures.domain.persistence.Persistence.SEQUENCE;
import static org.bluewindows.figures.domain.persistence.Persistence.START_DATE;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_FIELDS;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_ID;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_INCLUSION;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TYPE;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_CATEGORY;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_DEDUCTIBLE;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_DESC;
import static org.bluewindows.figures.domain.persistence.Persistence.USER_CHANGED_MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.VALUE;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.PersistenceAdminDao;
import org.bluewindows.figures.domain.CallResult;

public class PersistenceAdminDaoImplSqlite implements PersistenceAdminDao {
	
	private static Connection connection;
	private static final String SQLITE_CLASS_NAME = "org.sqlite.JDBC";
	private static final String CIPHER = "aes256cbc";
	private static String connectString = "jdbc:sqlite:";

	public PersistenceAdminDaoImplSqlite(){
	}
	
	public static void setConnectString(String connectStr) {
		connectString = connectStr;
	}
	
	@Override
	public boolean isPersistenceValid(File file) throws IOException {
		Reader fileReader;
		fileReader = new FileReader(file);
		char[] firstSixChars = new char[6];
		fileReader.read(firstSixChars, 0, 6);
		fileReader.close();
		if (String.valueOf(firstSixChars).equals("SQLite")) {
			return true;
		}
		return false;
	}
	
	public CallResult getConnection(String fileName, String password) {
		CallResult result = new CallResult();
		try {
			Class.forName(SQLITE_CLASS_NAME);
			if (connection != null) connection.close();
			if (password == null) {
				connection = DriverManager.getConnection(connectString + fileName);
			}else {
				connection = DriverManager.getConnection(connectString + fileName +
				"?cipher=" + CIPHER + "&key=" + password);
			}
		} catch (ClassNotFoundException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Data Store Open Failure", "Could not open : " + fileName);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Data Store Open Failure: " + e.getLocalizedMessage(), fileName + " is not a valid Figures data file.");
		}
		return result;
	}
	
	@Override
	public CallResult openExisting(String fileName, String password) {
		CallResult result = new CallResult();
		result = getConnection(fileName, password);
		if (result.isBad()) return result;
		try {
			PreparedStatement ps = connection.prepareStatement("Select count(*) from " + ACCOUNT_STORE_NAME);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Data Store Open Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	@Override
	public CallResult openNew(String fileName, String password) {
		CallResult result = getConnection(fileName, password);
		if (result.isBad()) return result;
		result = executeDefinitions(getAccountTableDefs());
		if (result.isBad()) return result;
		result = executeDefinitions(getCategoryTableDefs());
		if (result.isBad()) return result;
		result = executeDefinitions(getFilterTableDefs());
		if (result.isBad()) return result;
		result = executeDefinitions(getFilterSetTableDefs());
		if (result.isBad()) return result;
		if (result.isBad()) return result;
		result = executeDefinitions(getSummaryTableDefs());
		if (result.isBad()) return result;
		result = executeDefinitions(getSummaryAccountTableDefs());
		if (result.isBad()) return result;
		result = executeDefinitions(getSummaryCategoryTableDefs());
		if (result.isBad()) return result;
		result = executeDefinitions(getTransactionTableDefs());
		if (result.isBad()) return result;
		return executeDefinitions(getTransactionCategoryTableDefs());
	}
	
	@Override
	public CallResult close() {
		CallResult result = new CallResult();
		try {
			if (connection != null) connection.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Data Store Close Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult startTransaction() {
		CallResult result = checkConnection();
		if (result.isBad()) return result;
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Data Store Transaction Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult commitTransaction() {
		CallResult result = checkConnection();
		if (result.isBad()) return result;
		try {
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			result.setCallBad("Data Store Transaction Commit Failure", e.getLocalizedMessage());
		}
		return result;
	}

	@Override
	public CallResult rollBackTransaction() {
		CallResult result = checkConnection();
		if (result.isBad()) return result;
		try {
			connection.rollback();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Data Store Transaction Rollback Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	@Override
	public CallResult isTransactionActive() {
		CallResult result = checkConnection();
		if (result.isBad()) return result;
		try {
			result.setReturnedObject(Boolean.valueOf(!connection.getAutoCommit()));
		} catch (SQLException e) {
			result.setCallBad("Data Store Transaction Check Failure", e.getLocalizedMessage());
		}
		return result;
	}

	public CallResult checkConnection() {
		CallResult result = new CallResult();
		if (connection == null){
			return result.setCallBad("Data Store Connection Failure", "Connection has not been established.");
		}
		try {
			if (connection.isClosed()){
				return result.setCallBad("Data Store Connection Failure", "Connection is not open.");
			}
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Data Store Connection Failure", e.getLocalizedMessage());
		}
		return result;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	private CallResult executeDefinitions(LinkedList<String> definitions){
		CallResult result = checkConnection();
		if (result.isBad()) return result;
		for (Iterator<String> defs = definitions.iterator(); defs.hasNext();) {
			String definition = (String) defs.next();
			result = executeUpdateStatement(definition);
			if (result.isBad()) return result;
		}
		return result;
	}

	private CallResult executeUpdateStatement(String sql){
		CallResult result = new CallResult();
		try {
			Statement stmt = getConnection().createStatement();
			int stmtResult = stmt.executeUpdate(sql);
			result.setReturnedObject(Integer.valueOf(stmtResult));
			stmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			String message = this.getClass().getSimpleName();
			message = message + ", " + e.getLocalizedMessage();
			Exception exception = new Exception(message);
			return result.setCallBad("Update Failure", exception.getMessage());
		}
		return result;
	}

	@Override
	public CallResult optimize() {
		CallResult result = checkConnection();
		if (result.isBad()) return result;
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("VACUUM");
			stmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Data Store Optimize Failure", e.getLocalizedMessage());
		}
		return result;
	}

	public LinkedList<String> getAccountTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(ACCOUNT_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(NAME).append(" VARCHAR NOT NULL, ");
		sb.append(TYPE + " VARCHAR NOT NULL, ");
		sb.append(FILTER_SET_ID).append(" INTEGER, ");
		sb.append(INITIAL_BALANCE).append(" REAL, ");
		sb.append(LAST_LOAD_DATE).append(" DATE, ");
		sb.append(IMPORT_FOLDER).append(" VARCHAR) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE UNIQUE INDEX ").append(ACCOUNT_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(ACCOUNT_STORE_NAME).append(" ( ").append(NAME).append(" ) ");
		defs.add(sb.toString());
		return defs;
	}
	
	public LinkedList<String> getCategoryTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(CATEGORY_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(NAME).append(" VARCHAR NOT NULL) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE UNIQUE INDEX ").append(CATEGORY_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(CATEGORY_STORE_NAME).append(" ( ").append(NAME).append(" ) ");
		defs.add(sb.toString());
		return defs;
	}
	
	public LinkedList<String> getTransactionTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(TRANSACTION_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(ACCOUNT_ID).append(" INTEGER NOT NULL, ");
		sb.append(NUMBER).append(" VARCHAR, ");
		sb.append(DATE).append(" DATE NOT NULL, ");
		sb.append(DESCRIPTION).append(" VARCHAR NOT NULL, ");
		sb.append(AMOUNT).append(" REAL NOT NULL, ");
		sb.append(MEMO).append(" VARCHAR NOT NULL, ");
		sb.append(DEDUCTIBLE).append(" BOOLEAN NOT NULL, ");
		sb.append(ORIG_DESC).append(" VARCHAR NOT NULL, ");
		sb.append(ORIG_MEMO).append(" VARCHAR NOT NULL, ");
		sb.append(USER_CHANGED_DESC).append(" BOOLEAN NOT NULL, ");
		sb.append(USER_CHANGED_MEMO).append(" BOOLEAN NOT NULL, ");
		sb.append(USER_CHANGED_CATEGORY).append(" BOOLEAN NOT NULL, ");
		sb.append(USER_CHANGED_DEDUCTIBLE).append(" BOOLEAN NOT NULL, ");
		sb.append(BALANCE).append(" REAL NOT NULL) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(TRANSACTION_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(TRANSACTION_STORE_NAME).append(" ( ");
		sb.append(ACCOUNT_ID).append(", ");
		sb.append(DATE).append(" DESC) ");
		defs.add(sb.toString());
		return defs;
	}
	
	public LinkedList<String> getTransactionCategoryTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(TRANSACTION_CATEGORY_STORE_NAME).append(" (");
		sb.append(TRANSACTION_CATEGORY_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(TRANSACTION_ID).append(" INTEGER NOT NULL, ");
		sb.append(CATEGORY_ID).append(" INTEGER NOT NULL, ");
		sb.append(CATEGORY_AMOUNT).append(" REAL NOT NULL) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(TRANSACTION_CATEGORY_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(TRANSACTION_CATEGORY_STORE_NAME).append(" ( ");
		sb.append(TRANSACTION_ID).append(") ");
		defs.add(sb.toString());
		return defs;
	}
	
	public LinkedList<String> getFilterTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(FILTER_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(FILTER_SET_ID).append(" INTEGER NOT NULL, ");
		sb.append(SEQUENCE).append(" INTEGER NOT NULL, ");
		sb.append(FIELD).append(" VARCHAR NOT NULL, ");
		sb.append(EXPRESSION).append(" VARCHAR NOT NULL, ");
		sb.append(VALUE).append(" VARCHAR NOT NULL, ");
		sb.append(RESULT).append(" VARCHAR NOT NULL, ");
		sb.append(REPLACEMENT).append(" VARCHAR, ");
		sb.append(DEDUCTIBLE).append(" BOOLEAN, ");
		sb.append(CATEGORY_ID).append(" INTEGER ) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(FILTER_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(FILTER_STORE_NAME).append(" ( ").append(FILTER_SET_ID).append(" ) ");
		defs.add(sb.toString());
		return defs;
	}

	public LinkedList<String> getFilterSetTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(FILTER_SET_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(NAME).append(" VARCHAR NOT NULL, ");
		sb.append(DEFAULT_FIELD).append(" VARCHAR NOT NULL, ");
		sb.append(DEFAULT_EXPRESSION).append(" VARCHAR NOT NULL, ");
		sb.append(DEFAULT_RESULT).append(" VARCHAR NOT NULL) ");
		defs.add(sb.toString());
		return defs;
	}

	
	public LinkedList<String> getSummaryTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(SUMMARY_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(NAME).append(" VARCHAR NOT NULL, ");
		sb.append(SUMMARY_FIELDS).append(" VARCHAR NOT NULL, ");
		sb.append(TRANSACTION_INCLUSION).append(" VARCHAR NOT NULL, ");
		sb.append(CATEGORY_INCLUSION).append(" VARCHAR NOT NULL, ");
		sb.append(CATEGORIZED_ONLY).append(" BOOLEAN NOT NULL, ");
		sb.append(DEDUCTIBLE_INCLUSION).append(" VARCHAR NOT NULL, ");
		sb.append(CHECK_INCLUSION).append(" VARCHAR NOT NULL, ");
		sb.append(START_DATE).append(", ");
		sb.append(END_DATE).append(") ");
		defs.add(sb.toString());
		return defs;
	}

	public LinkedList<String> getSummaryAccountTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(SUMMARY_ACCOUNT_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(SUMMARY_ID).append(" INTEGER NOT NULL, ");
		sb.append(ACCOUNT_ID).append(" INTEGER NOT NULL) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(SUMMARY_ACCOUNT_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(SUMMARY_ACCOUNT_STORE_NAME).append(" ( ").append(SUMMARY_ID).append(" )");
		defs.add(sb.toString());
		return defs;
	}
	
	public LinkedList<String> getSummaryCategoryTableDefs(){
		LinkedList<String> defs = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(SUMMARY_CATEGORY_STORE_NAME).append(" (");
		sb.append(ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sb.append(SUMMARY_ID).append(" INTEGER NOT NULL, ");
		sb.append(CATEGORY_ID).append(" INTEGER NOT NULL) ");
		defs.add(sb.toString());
		sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(SUMMARY_CATEGORY_STORE_NAME).append("_IX1");
		sb.append(" ON ").append(SUMMARY_CATEGORY_STORE_NAME).append(" ( ").append(SUMMARY_ID).append(" )");
		defs.add(sb.toString());
		return defs;
	}
	
}
