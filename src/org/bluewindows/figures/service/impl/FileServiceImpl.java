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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.FileService;

public class FileServiceImpl implements FileService{
	
	private String installDirectory;
	
	public FileServiceImpl(){
		try {
			installDirectory = new File( "." ).getCanonicalPath();
		} catch (IOException e) {
			Figures.logStackTrace(e);
		}
	}
	
	@Override
	public CallResult getFileInputStream(String filePath) {
		CallResult result = new CallResult();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(installDirectory + filePath);
			result.setReturnedObject(fis);
		} catch (FileNotFoundException e) {
			Figures.logStackTrace(e);
			result.setCallBad("File Error", e.getLocalizedMessage());
		}
		return result;
	}
	
	@Override
	// Gets a file from the local file system OR from the jar
	public CallResult getFileResource(String filePath) {
		CallResult result = new CallResult();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(installDirectory + filePath);
			result.setReturnedObject(inputStream);
		} catch (FileNotFoundException e) {
			inputStream = getClass().getResourceAsStream(filePath);
			if (inputStream == null) {
				return result.setCallBad("File Error", "File not found.");
			}
			result.setReturnedObject(inputStream);
		}
		return result;
	}

	@Override
	public CallResult getFileOutputStream(String filePath, boolean overWrite) {
		CallResult result = new CallResult();
		File file = new File(filePath);
		if (file.exists() && !overWrite){
			result.setCallBad("File Error", "File exists");
		}else if (file.exists() && !file.canWrite() && overWrite){
			result.setCallBad("File Error", "File cannot be overwritten");
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("File Error", e.getLocalizedMessage());
		}
		result.setReturnedObject(fos);
		return result;
	}

	public String getFolder(File file){
		if (file == null) return null;
		String path = null;
		try {
			path = file.getCanonicalPath();
		} catch (IOException e) {
			Figures.logStackTrace(e);
		}
		int lastPos = StringUtils.lastIndexOf(path, '\\');
		if (lastPos == -1) lastPos = StringUtils.lastIndexOf(path, '/');
		if (lastPos == -1) return null;
		return path.substring(0, lastPos + 1);
	}
	
	@Override
	public CallResult makeFolder(String folderName) {
		CallResult result = new CallResult();
		
		
		return result;
	}
	
	public String getExtension(File file){
		if (file == null) return null;
		int lastPos = StringUtils.lastIndexOf(file.getName(), '.');
		if (lastPos == -1) return null;
		return file.getName().substring(lastPos+1, file.getName().length());
	}

	@Override
	public CallResult loadProperties(String fileName) {
		CallResult result = new CallResult();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Property File Error", "File not found.");
			return result;
		}
		Properties properties = new Properties();
		try {
			properties.load(fis);
		} catch (IOException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Property File Error", e.getLocalizedMessage());
			return result;
		}
		result.setReturnedObject(properties);
		return result;
	}

	@Override
	public CallResult storeProperties(String fileName, Properties properties) {
		CallResult result = getFileOutputStream(fileName, true);
		if (result.isGood()){
			try {
				properties.store((FileOutputStream)result.getReturnedObject(), "");
				((FileOutputStream)result.getReturnedObject()).close();
			} catch (IOException e) {
				Figures.logStackTrace(e);
				result.setCallBad("Property File Error", e.getLocalizedMessage());
			}
		}
		return result;
	}


}
