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
package org.bluewindows.figures.jasper;

import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;

public class JasperColumn {
	
	private String columnHeader;
	private int width;
	private HorizontalTextAlignEnum alignment;
	private Class<?> valueClass;
	private String pattern;
	private int position;
	private boolean summarized;
	private String fieldName;
	private boolean stretchHeight = false;
	
	public JasperColumn(String columnHeader, int width, HorizontalTextAlignEnum alignment, 
		Class<?> valueClass, String pattern, boolean summarized) {
		this.columnHeader = columnHeader;
		this.width = width;
		this.alignment = alignment;
		this.valueClass = valueClass;
		this.pattern = pattern;
		this.summarized = summarized;
	}
	
	public String getColumnHeader() {
		return columnHeader;
	}
	
	public int getWidth() {
		return width;
	}
	
	public HorizontalTextAlignEnum getAlignment() {
		return alignment;
	}
	
	public Class<?> getValueClass() {
		return valueClass;
	}

	public String getPattern() {
		return pattern;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public boolean isSummarized() {
		return summarized;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isStretchHeight() {
		return stretchHeight;
	}

	public void setStretchHeight(boolean stretchHeight) {
		this.stretchHeight = stretchHeight;
	}
	
}
