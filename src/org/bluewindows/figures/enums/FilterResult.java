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

import java.util.ArrayList;
import java.util.List;

public enum FilterResult {
	COPY_TO_DESCRIPTION("Copy It To Description"),
	COPY_TO_MEMO("Copy It To Memo"),
	NONE("None"),
	REPLACE_DESCRIPTION("Replace Description With"),
	REPLACE_MEMO("Replace Memo With");
	
	private String text;
	
	FilterResult(String text){
		this.text = text;
	}

	public String toString() {
		return text;
	}
	
	public static FilterResult findByText(String text){
		if (COPY_TO_DESCRIPTION.text.equals(text)){
			return COPY_TO_DESCRIPTION;
		}else if (COPY_TO_MEMO.text.equals(text)){
			return COPY_TO_MEMO;
		}else if (REPLACE_DESCRIPTION.text.equals(text)){
			return REPLACE_DESCRIPTION;
		}else if (REPLACE_MEMO.text.equals(text)){
			return REPLACE_MEMO;
		}else if (NONE.text.equals(text)){
			return NONE;
		}else{
			throw new IllegalArgumentException("Illegal FilterResult: " + text);
		}	
	}
	
	public static List<FilterResult> getAppropriateValues(FilterField resultSource) {
		FilterResult excludedResultType = null;
		List<FilterResult> appropriateValues = new ArrayList<FilterResult>();
		if (resultSource.equals(FilterField.DESCRIPTION)) {
			excludedResultType = FilterResult.COPY_TO_DESCRIPTION;
		} else if (resultSource.equals(FilterField.MEMO)) {
			excludedResultType = FilterResult.COPY_TO_MEMO;
		}
		for (FilterResult result : FilterResult.values()) {
			if (!result.equals(excludedResultType)) {
				appropriateValues.add(result);
			}
		}
		return appropriateValues;
	}
}

