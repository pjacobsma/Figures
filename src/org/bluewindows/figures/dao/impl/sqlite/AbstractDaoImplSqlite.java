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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.admin.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;


public abstract class AbstractDaoImplSqlite {

	protected PersistenceAdminDaoImplSqlite persistenceAdmin;
	public static DateTimeFormatter JDBC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public AbstractDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc){
		this.persistenceAdmin = persistenceJdbc;
	}
	
	protected CallResult decorateResult(CallResult result){
		result.setMessageDecorator("  Occured in " + this.getClass().getName());
		return result;
	}
	
	protected void closeResultSet(ResultSet resultSet) {
		try {
			resultSet.getStatement().close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
		}
	}
	
}
