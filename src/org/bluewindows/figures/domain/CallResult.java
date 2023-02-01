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

package org.bluewindows.figures.domain;

import org.bluewindows.figures.app.Figures;

public class CallResult {
	
	private boolean callGood = true;
	private Object returnedObject;
	private String errorMessage = "";
	private String messageDecorator = "";

	public boolean isGood() {
		return callGood;
	}
	
	public boolean isBad() {
		return (! callGood);
	}
	
	public CallResult setCallBad() {
		callGood = false;
		return this;
	}
	
	public CallResult setCallBad(String messageDecorator, String errorMessage) {
		this.callGood = false;
		this.messageDecorator = messageDecorator;
		this.errorMessage = errorMessage;
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String errorClass = stackTrace[2].getClassName();
		int lineNumber = stackTrace[2].getLineNumber();
		Figures.logger.severe(messageDecorator + ", " + errorMessage + " in " + errorClass + " at line " + lineNumber);
		return this;
	}
	
	public CallResult setCallBad(String errorMessage) {
		this.callGood = false;
		this.errorMessage = errorMessage;
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String errorClass = stackTrace[2].getClassName();
		int lineNumber = stackTrace[2].getLineNumber();
		Figures.logger.severe(messageDecorator + ", " + errorMessage + " in " + errorClass + " at line " + lineNumber);
		return this;
	}
	
	public Object getReturnedObject() {
		return returnedObject;
	}

	public CallResult setReturnedObject(Object returnedObject) {
		this.returnedObject = returnedObject;
		return this;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setMessageDecorator(String messageDecorator){
		this.messageDecorator = messageDecorator;
	}
	
	public String getMessageDecorator() {
		return messageDecorator;
	}
	
}
