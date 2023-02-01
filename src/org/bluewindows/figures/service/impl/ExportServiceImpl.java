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
package org.bluewindows.figures.service.impl;

import java.io.File;

import org.bluewindows.figures.dao.impl.ofx.ExportOFXDaoImpl;
import org.bluewindows.figures.dao.impl.qif.ExportQIFDaoImpl;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.ExportService;

public class ExportServiceImpl implements ExportService {
	
	private static ExportServiceImpl instance = new ExportServiceImpl();
	private ExportQIFDaoImpl qifDaoImpl = new ExportQIFDaoImpl();
	private ExportOFXDaoImpl ofxDaoImpl = new ExportOFXDaoImpl();
	
	private ExportServiceImpl(){
	}
	
	public static ExportServiceImpl getInstance(){
		return instance;
	}

	@Override
	public CallResult exportOFX(Account account, File file) {
		return ofxDaoImpl.exportAccount(account, file);
	}

	@Override
	public CallResult exportQIF(Account account, File file) {
		return qifDaoImpl.exportAccount(account, file);
	}

}
