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

import java.io.File;
import java.util.Properties;

import org.bluewindows.figures.domain.CallResult;


public interface FileService {
	
	public CallResult getFileInputStream(String filePath);
	
	public CallResult getFileResource(String filePath);

	public CallResult getFileOutputStream(String filePath, boolean overwrite);

	public String getFolder(File fileToOpen);

	public CallResult makeFolder(String folderName);

	public String getExtension(File file);
	
	public CallResult loadProperties(String fileName);

	public CallResult storeProperties(String fileName, Properties properties);

}
