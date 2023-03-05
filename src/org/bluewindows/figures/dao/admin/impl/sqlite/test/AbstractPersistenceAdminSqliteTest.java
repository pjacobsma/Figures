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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.admin.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.ServiceFactory;
import org.junit.Before;

public abstract class AbstractPersistenceAdminSqliteTest {
	
	protected PersistenceAdminDaoImplSqlite persistenceAdmin = new PersistenceAdminDaoImplSqlite();
	protected Properties properties;
	
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

	protected CallResult dropTableColumn(String tableName, String columnName) {
		return persistenceAdmin.executeUpdateStatement("ALTER TABLE "+ tableName + " DROP COLUMN "+ columnName);
	}
	
	protected List<String> getTableColumns(String tableName) throws SQLException{
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
