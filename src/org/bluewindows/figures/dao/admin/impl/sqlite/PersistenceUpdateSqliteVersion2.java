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
package org.bluewindows.figures.dao.admin.impl.sqlite;

import static org.bluewindows.figures.domain.persistence.Persistence.*;
import org.bluewindows.figures.dao.admin.AbstractPersistenceUpdateDaoSqlite;
import org.bluewindows.figures.dao.admin.PersistenceUpdateDao;
import org.bluewindows.figures.dao.impl.sqlite.AbstractDaoImplSqlite;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.TransactionDate;

public class PersistenceUpdateSqliteVersion2 extends AbstractPersistenceUpdateDaoSqlite implements PersistenceUpdateDao{
	
	@Override
	public int getVersion() {
		return 2;
	}
	
	@Override
	public CallResult update() {
		CallResult result =  persistenceAdmin.executeUpdateStatement("ALTER TABLE "+ ACCOUNT_STORE_NAME + " ADD COLUMN "+ LAST_FILTER_DATE + " DATE ");
		if (result.isBad()) return result;
		return persistenceAdmin.executeUpdateStatement("UPDATE " + ACCOUNT_STORE_NAME + " " +
			"SET " + LAST_FILTER_DATE + " = '" + TransactionDate.MINIMUM_DATE.value().format(AbstractDaoImplSqlite.JDBC_DATE_FORMAT) + "' " +
			"WHERE " + LAST_FILTER_DATE + " IS NULL ");
	}
}
