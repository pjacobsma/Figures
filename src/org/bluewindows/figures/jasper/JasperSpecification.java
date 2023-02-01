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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class JasperSpecification {
	
	private String fontName;
	private String pdfFontName;
	private String pdfEncoding;
	private int fontSize;
	private int titleFontSize;
	private int titleHeight;
	private int pageWidth;
	private int pageHeight;

	private int leftMargin;
	private int rightMargin;
	private int topMargin;
	private int bottomMargin;
	private int rowHeight;
	private int rowSpacing;
	private int columnSpacing;

	private List<String> headings = new ArrayList<String>();
	private String runDate;
	private List<Pair<String, String>> searchCriteriaList = new ArrayList<Pair<String, String>>();
	private List<JasperColumn> groupColumns = new ArrayList<JasperColumn>();
	private List<JasperColumn> detailColumns = new ArrayList<JasperColumn>();
	
	
	public String getFontName() {
		return fontName;
	}
	
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	
	public String getPdfFontName() {
		return pdfFontName;
	}
	
	public void setPdfFontName(String fontName) {
		this.pdfFontName = fontName;
	}
	
	public String getPdfEncoding() {
		return pdfEncoding;
	}
	
	public void setPdfEncoding(String encoding) {
		this.pdfEncoding = encoding;
	}
	
	public int getFontSize() {
		return fontSize;
	}
	
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	
	public int getTitleFontSize() {
		return titleFontSize;
	}

	public void setTitleFontSize(int titleFontSize) {
		this.titleFontSize = titleFontSize;
	}
	
	public int getTitleHeight() {
		return titleHeight;
	}

	public void setTitleHeight(int titleHeight) {
		this.titleHeight = titleHeight;
	}

	public int getPageWidth() {
		return pageWidth;
	}
	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}
	
	public int getPageHeight() {
		return pageHeight;
	}
	
	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}
	public int getLeftMargin() {
		return leftMargin;
	}
	
	public void setLeftMargin(int leftMargin) {
		this.leftMargin = leftMargin;
	}
	
	public int getRightMargin() {
		return rightMargin;
	}
	
	public void setRightMargin(int rightMargin) {
		this.rightMargin = rightMargin;
	}
	
	public int getTopMargin() {
		return topMargin;
	}
	
	public void setTopMargin(int topMargin) {
		this.topMargin = topMargin;
	}
	
	public int getBottomMargin() {
		return bottomMargin;
	}
	
	public void setBottomMargin(int bottomMargin) {
		this.bottomMargin = bottomMargin;
	}
	
	public int getRowHeight() {
		return rowHeight;
	}
	
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}
	
	public int getRowSpacing() {
		return rowSpacing;
	}

	public void setRowSpacing(int rowSpacing) {
		this.rowSpacing = rowSpacing;
	}

	public int getColumnSpacing() {
		return columnSpacing;
	}
	
	public void setColumnSpacing(int columnSpacing) {
		this.columnSpacing = columnSpacing;
	}
	
	public List<String> getHeadings() {
		return headings;
	}
	
	public String getRunDate() {
		return runDate;
	}

	public void setRunDate(String runDate) {
		this.runDate = runDate;
	}

	public void addHeading(String heading) {
		headings.add(heading);
	}

	public List<Pair<String, String>> getSearchCriteriaList() {
		return searchCriteriaList;
	}

	public void addSearchCriteria(Pair<String, String> criteria) {
		searchCriteriaList.add(criteria);
	}

	public List<JasperColumn> getGroupColumns() {
		return groupColumns;
	}

	public void addGroupColumn(JasperColumn column) {
		groupColumns.add(column);
	}
	
	public List<JasperColumn> getDetailColumns() {
		return detailColumns;
	}

	public void addDetailColumn(JasperColumn column) {
		detailColumns.add(column);
	}

	public int getReportWidth() {
		int reportWidth = 0 - columnSpacing;
		for (JasperColumn reportColumn : groupColumns) {
			reportWidth = reportWidth + columnSpacing + reportColumn.getWidth();
		}
		for (JasperColumn reportColumn : detailColumns) {
			reportWidth = reportWidth + columnSpacing + reportColumn.getWidth();
		}
		reportWidth = reportWidth + leftMargin + rightMargin;
		return reportWidth;
	}
	
	public List<JasperColumn> getReportColumns() {
		List<JasperColumn> reportColumns = new ArrayList<JasperColumn>(groupColumns);
		reportColumns.addAll(detailColumns);
		return reportColumns;
	}
	
}
