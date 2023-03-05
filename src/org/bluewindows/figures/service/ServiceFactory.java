package org.bluewindows.figures.service;

import org.bluewindows.figures.dao.admin.impl.sqlite.PersistenceAdminDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.AccountDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.CategoryDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.FilterDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.FilterSetDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.SummaryDaoImplSqlite;
import org.bluewindows.figures.dao.impl.sqlite.TransactionDaoImplSqlite;
import org.bluewindows.figures.service.impl.PersistenceServiceImpl;

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

public class ServiceFactory {

	private static ServiceFactory instance;
	private FileService fileSvc;
	private DisplayService displaySvc;
	private ImportService importSvc;
	private ExportService exportSvc;
	private MaintenanceService maintenanceSvc;
	private PersistenceService persistenceSvc;
	private ReportService reportSvc;
	
	private ServiceFactory(){
	}
	
	public static ServiceFactory getInstance(){
		if (instance == null) instance = new ServiceFactory();
		return instance;
	}
	
	public FileService getFileSvc() {
		return fileSvc;
	}

	public void setFileSvc(FileService fileSvc) {
		this.fileSvc = fileSvc;
	}

	public DisplayService getDisplaySvc() {
		return displaySvc;
	}

	public void setDisplaySvc(DisplayService displaySvc) {
		this.displaySvc = displaySvc;
	}
	
	public ImportService getImportSvc() {
		return importSvc;
	}

	public void setImportSvc(ImportService importSvc) {
		this.importSvc = importSvc;
	}

	public ExportService getExportSvc() {
		return exportSvc;
	}

	public void setExportSvc(ExportService exportSvc) {
		this.exportSvc = exportSvc;
	}

	public MaintenanceService getMaintenanceSvc() {
		return maintenanceSvc;
	}

	public void setMaintenanceSvc(MaintenanceService maintenanceSvc) {
		this.maintenanceSvc = maintenanceSvc;
	}

	public PersistenceService getPersistenceSvc() {
		return persistenceSvc;
	}

	public void setPersistenceSvc(PersistenceService persistenceSvc) {
		this.persistenceSvc = persistenceSvc;
	}
	
	public void setUpPersistenceSvcSqlite(){
		persistenceSvc = PersistenceServiceImpl.getInstance();
		PersistenceAdminDaoImplSqlite persistenceAdmin = new PersistenceAdminDaoImplSqlite();
		persistenceSvc.setPersistenceAdminDao(persistenceAdmin);
		persistenceSvc.setAccountDao(new AccountDaoImplSqlite(persistenceAdmin));
		persistenceSvc.setCategoryDao(new CategoryDaoImplSqlite(persistenceAdmin));
		persistenceSvc.setFilterDao(new FilterDaoImplSqlite(persistenceAdmin));
		persistenceSvc.setFilterSetDao(new FilterSetDaoImplSqlite(persistenceAdmin));
		persistenceSvc.setSummaryDao(new SummaryDaoImplSqlite(persistenceAdmin));
		persistenceSvc.setTransactionDao(new TransactionDaoImplSqlite(persistenceAdmin));
	}

	public ReportService getReportSvc() {
		return reportSvc;
	}

	public void setReportSvc(ReportService reportSvc) {
		this.reportSvc = reportSvc;
	}
}