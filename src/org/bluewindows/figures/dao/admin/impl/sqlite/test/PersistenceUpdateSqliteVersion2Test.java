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

package org.bluewindows.figures.dao.admin.impl.sqlite.test;

import static org.bluewindows.figures.domain.persistence.Persistence.ACCOUNT_STORE_NAME;
import static org.bluewindows.figures.domain.persistence.Persistence.LAST_FILTER_DATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bluewindows.figures.dao.admin.impl.sqlite.PersistenceUpdateSqliteVersion2;
import org.bluewindows.figures.dao.impl.sqlite.AccountDaoImplSqlite;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.TransactionDate;
import org.junit.Test;


public class PersistenceUpdateSqliteVersion2Test extends AbstractPersistenceAdminSqliteTest {
	
	private PersistenceUpdateSqliteVersion2 updater = new PersistenceUpdateSqliteVersion2();
	private AccountDaoImplSqlite accountDao = new AccountDaoImplSqlite(persistenceAdmin);
	
	@Test
	public void testUpdate() throws Exception {
		CallResult result = persistenceAdmin.savePersistenceVersion(Integer.valueOf(1));
		assertTrue(result.isGood());
		result = dropTableColumn(ACCOUNT_STORE_NAME, LAST_FILTER_DATE);
		assertTrue(result.isGood());
		List<String> tableColumns = getTableColumns(ACCOUNT_STORE_NAME);
		assertEquals(7, tableColumns.size());
		assertEquals(2, updater.getVersion());
		result = updater.update();
		assertTrue(result.isGood());
		tableColumns = getTableColumns(ACCOUNT_STORE_NAME);
		assertEquals(8, tableColumns.size());
		assertEquals("0:ID:INTEGER:0:null", tableColumns.get(0));
		assertEquals("1:Name:VARCHAR:1:null", tableColumns.get(1));
		assertEquals("2:Type:VARCHAR:1:null", tableColumns.get(2));
		assertEquals("3:FilterSetID:INTEGER:0:null", tableColumns.get(3));
		assertEquals("4:InitialBalance:REAL:0:null", tableColumns.get(4));
		assertEquals("5:LastLoadDate:DATE:0:null", tableColumns.get(5));
		assertEquals("6:ImportFolder:VARCHAR:0:null", tableColumns.get(6));
		assertEquals("7:LastFilterDate:DATE:0:null", tableColumns.get(7));
		result = accountDao.getAccounts();
		assertTrue(result.isGood());
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>)result.getReturnedObject();
		for (Account account : accounts) {
			assertEquals(TransactionDate.MINIMUM_DATE, account.getLastFilteredDate());
		}
		
	}
}
	
