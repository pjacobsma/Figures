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
package org.bluewindows.figures.service;

import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.enums.CursorType;

public interface DisplayService {
	
	public CallResult initialize();
	public CallResult initializePersistence(String fileName, String password, boolean isNew);
	public CallResult getInputFileName(String folder, String title);
	public CallResult getOutputFileName(String folder, String fileName, String title);
	public void setStatusGood(String message);
	public void setStatusHelp(String message);
	public void setStatusBad(String message);
	public void setStatusBad(CallResult result);
	public void clearStatus();
	public void setCursor(CursorType cursorType);
	public void displayErrorMessage(String title, String messageText);
	public void displayInformationMessage(String title, String messageText);
	public void displayHelp(String fileName);

}
