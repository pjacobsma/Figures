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

import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.filter.Filter;


public class FilterSet implements Cloneable {
	
	private int id;
	private String name = "";
	private String defaultColumn;
	private String defaultExpression;
	private String defaultResult;
	private List<Filter> filters = new ArrayList<Filter>();

	public static final FilterSet NONE = new FilterSet(0, "None", "", "", "");
	
	public FilterSet(int id, String name, String defaultColumn, String defaultExpression, String defaultResult){
		this.id = id;
		this.name = name;
		this.defaultColumn = defaultColumn;
		this.defaultExpression = defaultExpression;
		this.defaultResult = defaultResult;
	}
	
	public FilterSet(String name){
		this.name = name;
	}
	
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDefaultColumn() {
		return defaultColumn;
	}

	public void setDefaultColumn(String defaultColumn) {
		this.defaultColumn = defaultColumn;
	}

	public String getDefaultExpression() {
		return defaultExpression;
	}

	public void setDefaultExpression(String defaultExpression) {
		this.defaultExpression = defaultExpression;
	}

	public String getDefaultResult() {
		return defaultResult;
	}

	public void setDefaultResult(String defaultResult) {
		this.defaultResult = defaultResult;
	}

	public List<Filter> getFilters() {
		return filters;
	}
	
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
	public Object clone() {
		FilterSet theClone = null;
		try {
			theClone = (FilterSet)super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return theClone;
	}
	
	public String toString(){
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterSet other = (FilterSet) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
