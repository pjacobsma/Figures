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

import org.bluewindows.figures.domain.persistence.Persistence;

public enum FilterField {
	DESCRIPTION(Persistence.DESCRIPTION),
	MEMO(Persistence.MEMO);
	
	private String fieldTypeName;
	
	FilterField(String fieldTypeName){
		this.fieldTypeName = fieldTypeName;
	}

	public String toString() {
		return fieldTypeName;
	}
	
	public static FilterField findByText(String name){
		if (DESCRIPTION.fieldTypeName.equals(name)){
			return DESCRIPTION;
		}else if (MEMO.fieldTypeName.equals(name)){
			return MEMO;
		}else{
			throw new IllegalArgumentException("Illegal Field Type: " + name);
		}
	}
}
