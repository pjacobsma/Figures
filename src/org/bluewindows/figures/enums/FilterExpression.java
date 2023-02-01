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
package org.bluewindows.figures.enums;

public enum FilterExpression {
	CONTAINS("Contains"),
	STARTSWITH("Starts With"),
	ENDSWITH("Ends With"),
	EQUALS("Equals"),
	NONE("");
	
	private String text;
	
	FilterExpression(String text){
		this.text = text;
	}

	public String toString() {
		return text;
	}
	
	public static FilterExpression findByText(String text){
		if (CONTAINS.text.equals(text)){
			return CONTAINS;
		}else if (STARTSWITH.text.equals(text)){
			return STARTSWITH;
		}else if (ENDSWITH.text.equals(text)){
			return ENDSWITH;
		}else if (EQUALS.text.equals(text)){
			return EQUALS;
		}else if (NONE.text.equals(text)){
			return NONE;
		}else{
			throw new IllegalArgumentException("Illegal Filter Text: " + text);
		}
	}
}
