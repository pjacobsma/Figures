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

import static org.bluewindows.figures.domain.persistence.Persistence.ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_SET_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.FILTER_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.SUMMARY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_CATEGORY_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.TRANSACTION_STORE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.ServiceFactory;
import org.junit.Before;
import org.junit.Test;


public class PersistenceAdminDaoImplSqliteTest {
	
	private PersistenceAdminDaoImplSqlite persistenceAdmin = new PersistenceAdminDaoImplSqlite();
	private Properties properties;
	
	@Before
	public void before() throws SecurityException, IOException{
		Figures.configureLogging();
		ServiceFactory.getInstance().setUpPersistenceSvcSqlite();
		persistenceAdmin = (PersistenceAdminDaoImplSqlite)ServiceFactory.getInstance().getPersistenceSvc().getPersistenceAdminDao();
		properties = new Properties();
		properties.setProperty(Figures.DATE_FORMAT_NAME, Figures.DEFAULT_DATE_FORMAT);
		Figures.dateFormat = Figures.DATE_FORMAT_MAP.get(Figures.DEFAULT_DATE_FORMAT);
		Figures.setProperties(properties);
		CallResult result = persistenceAdmin.openNew(":memory:", null);
		assertTrue(result.isGood());
	}
	
	@Test
	public void testOpenNewAccountTable() throws Exception {
		// openNew is called above in the setUp method
		List<String> tableColumns = getTableColumns(ACCOUNT_STORE_NAME);
		assertEquals(8, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:Name:VARCHAR:1:null", tableColumns.get(1));
		assertEquals("2:Type:VARCHAR:1:null", tableColumns.get(2));
		assertEquals("3:FilterSetID:INTEGER:0:null", tableColumns.get(3));
		assertEquals("4:InitialBalance:REAL:0:null", tableColumns.get(4));
		assertEquals("5:LastLoadDate:DATE:0:null", tableColumns.get(5));
		assertEquals("6:LastFilterDate:DATE:0:null", tableColumns.get(6));
		assertEquals("7:ImportFolder:VARCHAR:0:null", tableColumns.get(7));
	}
	
	@Test
	public void testOpenNewCategoriesTable() throws Exception {
		// openNew is called above in the setUp method
		List<String> tableColumns = getTableColumns(CATEGORY_STORE_NAME);
		assertEquals(2, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:Name:VARCHAR:1:null", tableColumns.get(1));
	}
	
	@Test
	public void testOpenNewFiltersTable() throws Exception {
		// openNew is called above in the setUp method
		List<String> tableColumns = getTableColumns(FILTER_STORE_NAME);
		assertEquals(10, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:FilterSetID:INTEGER:1:null", tableColumns.get(1));
		assertEquals("2:Sequence:INTEGER:1:null", tableColumns.get(2));
		assertEquals("3:Field:VARCHAR:1:null", tableColumns.get(3));
		assertEquals("4:Expression:VARCHAR:1:null", tableColumns.get(4));
		assertEquals("5:Value:VARCHAR:1:null", tableColumns.get(5));
		assertEquals("6:Result:VARCHAR:1:null", tableColumns.get(6));
		assertEquals("7:Replacement:VARCHAR:0:null", tableColumns.get(7));
		assertEquals("8:Deductible:BOOLEAN:0:null", tableColumns.get(8));
		assertEquals("9:CategoryID:INTEGER:0:null", tableColumns.get(9));
	}

	@Test
	public void testOpenNewFilterSetsTable() throws Exception {
		// openNew is called above in the setUp method
		List<String> tableColumns = getTableColumns(FILTER_SET_STORE_NAME);
		assertEquals(5, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:Name:VARCHAR:1:null", tableColumns.get(1));
		assertEquals("2:DefaultField:VARCHAR:1:null", tableColumns.get(2));
		assertEquals("3:DefaultExpression:VARCHAR:1:null", tableColumns.get(3));
		assertEquals("4:DefaultResult:VARCHAR:1:null", tableColumns.get(4));
	}
	
	@Test
	public void testOpenNewSummaryTables() throws Exception {
		// openNew is called above in the setUp method
		List<String> tableColumns = getTableColumns(SUMMARY_STORE_NAME);
		assertEquals(10, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:Name:VARCHAR:1:null", tableColumns.get(1));
		assertEquals("2:SummaryFields:VARCHAR:1:null", tableColumns.get(2));
		assertEquals("3:TransactionInclusion:VARCHAR:1:null", tableColumns.get(3));
		assertEquals("4:CategoryInclusion:VARCHAR:1:null", tableColumns.get(4));
		assertEquals("5:CategorizedOnly:BOOLEAN:1:null", tableColumns.get(5));
		assertEquals("6:DeductibleInclusion:VARCHAR:1:null", tableColumns.get(6));
		assertEquals("7:CheckInclusion:VARCHAR:1:null", tableColumns.get(7));
		assertEquals("8:StartDate::0:null", tableColumns.get(8));
		assertEquals("9:EndDate::0:null", tableColumns.get(9));
		tableColumns = getTableColumns(SUMMARY_ACCOUNT_STORE_NAME);
		assertEquals(3, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:SummaryID:INTEGER:1:null", tableColumns.get(1));
		assertEquals("2:AccountID:INTEGER:1:null", tableColumns.get(2));
		tableColumns = getTableColumns(SUMMARY_CATEGORY_STORE_NAME);
		assertEquals(3, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:SummaryID:INTEGER:1:null", tableColumns.get(1));
		assertEquals("2:CategoryID:INTEGER:1:null", tableColumns.get(2));
	}
	
	@Test
	public void testOpenNewTransactionTables() throws Exception {
		// openNew is called above in the setUp method
		List<String> tableColumns = getTableColumns(TRANSACTION_STORE_NAME);
		assertEquals(15, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:AccountID:INTEGER:1:null", tableColumns.get(1));
		assertEquals("2:Number:VARCHAR:0:null", tableColumns.get(2));
		assertEquals("3:Date:DATE:1:null", tableColumns.get(3));
		assertEquals("4:Description:VARCHAR:1:null", tableColumns.get(4));
		assertEquals("5:Amount:REAL:1:null", tableColumns.get(5));
		assertEquals("6:Memo:VARCHAR:1:null", tableColumns.get(6));
		assertEquals("7:Deductible:BOOLEAN:1:null", tableColumns.get(7));
		assertEquals("8:OriginalDescription:VARCHAR:1:null", tableColumns.get(8));
		assertEquals("9:OriginalMemo:VARCHAR:1:null", tableColumns.get(9));
		assertEquals("10:UserChangedDesc:BOOLEAN:1:null", tableColumns.get(10));
		assertEquals("11:UserChangedMemo:BOOLEAN:1:null", tableColumns.get(11));
		assertEquals("12:UserChangedCategory:BOOLEAN:1:null", tableColumns.get(12));
		assertEquals("13:UserChangedDeductible:BOOLEAN:1:null", tableColumns.get(13));
		assertEquals("14:Balance:REAL:1:null", tableColumns.get(14));
		tableColumns = getTableColumns(TRANSACTION_CATEGORY_STORE_NAME);
		assertEquals(4, tableColumns.size());
		assertEquals("0:TransactionCategoryID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:TransactionID:INTEGER:1:null", tableColumns.get(1));
		assertEquals("2:CategoryID:INTEGER:1:null", tableColumns.get(2));
		assertEquals("3:CategoryAmount:REAL:1:null", tableColumns.get(3));
	}

	@Test
	public void testClose() throws Exception {
		CallResult result = persistenceAdmin.close();
		assertTrue(result.isGood());
		assertTrue(persistenceAdmin.getConnection().isClosed());
	}

	@Test
	public void testCloseOnUnopenedConnection() throws Exception{
		CallResult result = persistenceAdmin.close();
		assertTrue(result.isGood());
		result = persistenceAdmin.close();
		assertTrue(result.isGood());
	}

	@Test
	public void testStartTransactionOnUnopenedConnection() throws Exception {
		CallResult result = persistenceAdmin.close();
		assertTrue(result.isGood());
		result = persistenceAdmin.startTransaction();
		assertTrue(result.isBad());
		assertEquals("Connection is not open.", result.getErrorMessage());
	}

	@Test
	public void testStartTransaction() throws Exception {
		CallResult result = persistenceAdmin.startTransaction();
		assertTrue(result.isGood());
		assertFalse(persistenceAdmin.getConnection().getAutoCommit());
	}
	
	@Test
	public void testCommitTransactionOnUnopenedConnection() throws Exception {
		CallResult result = persistenceAdmin.close();
		assertTrue(result.isGood());
		result = persistenceAdmin.commitTransaction();
		assertTrue(result.isBad());
		assertEquals("Connection is not open.", result.getErrorMessage());
	}

	@Test
	public void testRollBackTransactionOnUnopenedConnection() throws Exception {
		CallResult result = persistenceAdmin.close();
		assertTrue(result.isGood());
		result = persistenceAdmin.rollBackTransaction();
		assertTrue(result.isBad());
		assertEquals("Connection is not open.", result.getErrorMessage());
	}
	
	private List<String> getTableColumns(String tableName) throws SQLException{
		Statement stmt = persistenceAdmin.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
		List<String> columnDefinitions = new ArrayList<String>();
		int numberOfColumns = rs.getMetaData().getColumnCount();
		while(rs.next()){
			String rowString = rs.getString(1);
			for (int i = 2; i < numberOfColumns; i++) {
				rowString = rowString + ":" + rs.getString(i);
			}
			columnDefinitions.add(rowString);
		}
		return columnDefinitions;
	}


}
