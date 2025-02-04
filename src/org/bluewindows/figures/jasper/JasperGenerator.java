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

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JRDesignVariable;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.CalculationEnum;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.ExpressionTypeEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.ResetTypeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.TextAdjustEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;

public class JasperGenerator {

	private static final int RUN_DATE_WIDTH = 80;
	private static final int PAGE_TEXT_WIDTH = 24;
	private static final int PAGE_NUMBER_WIDTH = 26;
	private static final int OF_TEXT_WIDTH = 10;
	private static AffineTransform affinetransform = new AffineTransform();
	private static FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

	public JasperDesign createDesign(JasperSpecification spec) throws JRException {
		validateSpec(spec);
		// For correct display, the last grouping column, if there is one, will be a detail column
		if (spec.getGroupColumns().size() > 0) {
			spec.getDetailColumns().add(0, spec.getGroupColumns().get(spec.getGroupColumns().size() - 1));
			spec.getGroupColumns().remove(spec.getGroupColumns().size() - 1);
		}
		JasperDesign design = new JasperDesign();
		design.setName("Design");
		design.setLanguage(JRReport.LANGUAGE_JAVA);
		setPageLayout(design, spec);
		addStyle(design, spec);
		addPageHeader(design, spec);
		addReportFields(design, spec);
		addColumnHeaders(design, spec);
		addDetail(design, spec);
		addGroups(design, spec);
		addSummaries(design, spec);
		return design;
	}

	private void validateSpec(JasperSpecification spec) throws JRException {
		if (spec.getHeadings().size() == 0) {
			throw new JRException("No heading in report specification");
		}
		if (spec.getDetailColumns().size() == 0) {
			throw new JRException("No data columns in report specification");
		}
	}

	private void setPageLayout(JasperDesign design, JasperSpecification spec) throws JRException {
		int reportWidth = spec.getReportWidth();
		if (reportWidth > spec.getPageWidth()) {
			design.setPageWidth(spec.getPageHeight());
			design.setPageHeight(spec.getPageWidth());
			design.setOrientation(OrientationEnum.LANDSCAPE);
		} else {
			design.setPageWidth(spec.getPageWidth());
			design.setPageHeight(spec.getPageHeight());
			design.setOrientation(OrientationEnum.PORTRAIT);
		}
		design.setLeftMargin(spec.getLeftMargin());
		design.setRightMargin(spec.getRightMargin());
		design.setTopMargin(spec.getTopMargin());
		design.setBottomMargin(spec.getBottomMargin());
	}

	private void addStyle(JasperDesign design, JasperSpecification spec) throws JRException {
		JRDesignStyle normalStyle = new JRDesignStyle();
		normalStyle.setName("NormalStyle");
		normalStyle.setDefault(true);
		normalStyle.setFontName(spec.getFontName());
		normalStyle.setFontSize(Float.valueOf(spec.getFontSize()));
		normalStyle.setPdfFontName(spec.getPdfFontName());
		normalStyle.setPdfEncoding(spec.getPdfEncoding());
		normalStyle.setPdfEmbedded(true);
		design.addStyle(normalStyle);
	}

	private void addPageHeader(JasperDesign design, JasperSpecification spec) {
		JRDesignBand pageHeader = new JRDesignBand();
		pageHeader.setHeight(spec.getTitleHeight());

		JRDesignStaticText runDate = new JRDesignStaticText();
		runDate.setText(spec.getRunDate());
		runDate.setX(0);
		runDate.setY(0);
		runDate.setWidth(RUN_DATE_WIDTH);
		runDate.setHeight(spec.getRowHeight());
		runDate.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
		pageHeader.addElement(runDate);

		List<JRDesignStaticText> headings = new ArrayList<JRDesignStaticText>();
		headings.add(new JRDesignStaticText());
		JRDesignStaticText heading = headings.get(headings.size() - 1);
		heading.setText(spec.getHeadings().get(0));
		heading.setFontName(spec.getFontName());
		heading.setPdfFontName(spec.getPdfFontName());
		heading.setBold(Boolean.TRUE);
		heading.setFontSize(Float.valueOf(spec.getTitleFontSize()));
		heading.setPositionType(PositionTypeEnum.FIX_RELATIVE_TO_TOP);
		heading.setX(runDate.getX() + runDate.getWidth() + 1);
		heading.setY(0);
		int headingWidth = design.getPageWidth() - spec.getLeftMargin() - spec.getRightMargin() - RUN_DATE_WIDTH - PAGE_TEXT_WIDTH
				- PAGE_NUMBER_WIDTH - OF_TEXT_WIDTH - PAGE_NUMBER_WIDTH - 5;
		heading.setWidth(headingWidth);
		heading.setHeight(spec.getTitleHeight());
		heading.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		pageHeader.addElement(heading);

		JRDesignStaticText pageText = new JRDesignStaticText();
		pageText.setText("Page");
		pageText.setPositionType(PositionTypeEnum.FIX_RELATIVE_TO_TOP);
		pageText.setX(heading.getX() + heading.getWidth() + 1);
		pageText.setY(0);
		pageText.setWidth(PAGE_TEXT_WIDTH);
		pageText.setHeight(spec.getRowHeight());
		pageText.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
		pageHeader.addElement(pageText);

		JRDesignTextField pageNumber = new JRDesignTextField();
		JRDesignExpression pageNumberExpression = new JRDesignExpression("$V{PAGE_NUMBER}");
		pageNumber.setExpression(pageNumberExpression);
		pageNumber.setEvaluationTime(EvaluationTimeEnum.PAGE);
		pageNumber.setX(pageText.getX() + pageText.getWidth() + 1);
		pageNumber.setY(0);
		pageNumber.setWidth(PAGE_NUMBER_WIDTH);
		pageNumber.setHeight(spec.getRowHeight());
		pageNumber.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		pageHeader.addElement(pageNumber);

		JRDesignStaticText ofText = new JRDesignStaticText();
		ofText.setText("of");
		ofText.setPositionType(PositionTypeEnum.FIX_RELATIVE_TO_TOP);
		ofText.setX(pageNumber.getX() + pageNumber.getWidth() + 1);
		ofText.setY(0);
		ofText.setWidth(OF_TEXT_WIDTH);
		ofText.setHeight(spec.getRowHeight());
		ofText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		pageHeader.addElement(ofText);

		JRDesignTextField pageCount = new JRDesignTextField();
		JRDesignExpression pageCountExpression = new JRDesignExpression("$V{PAGE_NUMBER}");
		pageCount.setExpression(pageCountExpression);
		pageCount.setEvaluationTime(EvaluationTimeEnum.REPORT);
		pageCount.setX(ofText.getX() + ofText.getWidth() + 1);
		pageCount.setY(0);
		pageCount.setWidth(PAGE_NUMBER_WIDTH);
		pageCount.setHeight(spec.getRowHeight());
		pageCount.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		pageHeader.addElement(pageCount);

		for (int i = 1; i < spec.getHeadings().size(); i++) {
			pageHeader.setHeight(pageHeader.getHeight() + spec.getTitleHeight());
			headings.add(new JRDesignStaticText());
			heading = headings.get(headings.size() - 1);
			heading.setText(spec.getHeadings().get(i));
			heading.setBold(Boolean.TRUE);
			heading.setFontSize(Float.valueOf(spec.getTitleFontSize()));
			heading.setPositionType(PositionTypeEnum.FIX_RELATIVE_TO_TOP);
			heading.setX(runDate.getX() + runDate.getWidth() + 1);
			heading.setY(pageHeader.getHeight() - spec.getTitleHeight());
			heading.setWidth(headingWidth);
			heading.setHeight(spec.getTitleHeight());
			heading.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
			pageHeader.addElement(heading);
		}

		if (spec.getSearchCriteriaList().size() == 0) {
			design.setPageHeader(pageHeader);
			return;
		}

		List<JRDesignStaticText> criteriaList = new ArrayList<JRDesignStaticText>();
		int criteriaWidth = spec.getPageWidth() - spec.getLeftMargin() - spec.getRightMargin();
		Font font = new Font(spec.getFontName(), Font.PLAIN, spec.getFontSize());
		for (Pair<String, String> criteria : spec.getSearchCriteriaList()) {
			String criteriaText;
			if (criteria.getRight().trim().isEmpty()) {
				criteriaText = criteria.getLeft();
			} else {
				criteriaText = criteria.getLeft() + ": " + criteria.getRight();
			}
			double textSize = font.getStringBounds(criteriaText, frc).getWidth();
			if (textSize > criteriaWidth && criteria.getRight().contains(",")) {
				wrapCriteria(criteriaList, criteriaText, pageHeader, spec);
			} else {
				addCriteriaRow(criteriaList, criteriaText, pageHeader, spec);
			}
		}

		design.setPageHeader(pageHeader);
	}

	private void addCriteriaRow(List<JRDesignStaticText> criteriaList, String criteriaText, JRDesignBand pageHeader,
			JasperSpecification spec) {
		criteriaList.add(new JRDesignStaticText());
		JRDesignStaticText criteria = criteriaList.get(criteriaList.size() - 1);
		pageHeader.setHeight(pageHeader.getHeight() + spec.getRowHeight());
		criteria.setText(criteriaText);
		criteria.setBold(Boolean.TRUE);
		criteria.setFontSize(Float.valueOf(spec.getFontSize()));
		criteria.setPositionType(PositionTypeEnum.FIX_RELATIVE_TO_TOP);
		criteria.setX(0);
		criteria.setY((pageHeader.getHeight() - spec.getRowHeight()));
		criteria.setWidth(spec.getPageWidth() - spec.getLeftMargin() - spec.getRightMargin());
		criteria.setHeight(spec.getRowHeight());
		criteria.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
		criteria.setPrintWhenExpression(new JRDesignExpression("Boolean.valueOf($V{PAGE_NUMBER} == 1)"));
		criteria.setRemoveLineWhenBlank(Boolean.TRUE);
		pageHeader.addElement(criteria);
	}

	private void wrapCriteria(List<JRDesignStaticText> criteriaList, String criteriaText, JRDesignBand pageHeader,
			JasperSpecification spec) {
		// Add as many rows as necessary to display the list of comma-separated criteria strings.
		// Wrap rows on full strings
		Font font = new Font(spec.getFontName(), Font.PLAIN, spec.getFontSize());
		List<String> criteriaStrings = Arrays.asList(criteriaText.split(","));
		StringBuffer sb = new StringBuffer(criteriaStrings.get(0).trim());
		int criteriaWidth = spec.getPageWidth() - spec.getLeftMargin() - spec.getRightMargin() - 30;
		double currentSize;
		double nextSize;
		String nextString;
		int index = 1;
		while (index < criteriaStrings.size()) {
			currentSize = font.getStringBounds(sb.toString(), frc).getWidth();
			nextString = criteriaStrings.get(index).trim();
			nextSize = font.getStringBounds(nextString, frc).getWidth();
			if ((currentSize + nextSize) < criteriaWidth) {
				sb.append(", " + nextString);
			} else {
				sb.append(",");
				addCriteriaRow(criteriaList, sb.toString(), pageHeader, spec);
				sb = new StringBuffer(nextString);
			}
			index++;
		}
		addCriteriaRow(criteriaList, sb.toString(), pageHeader, spec);
	}

	private void addReportFields(JasperDesign design, JasperSpecification spec) throws JRException {
		for (JasperColumn column : spec.getReportColumns()) {
			JRDesignField field = new JRDesignField();
			field.setName(column.getFieldName());
			field.setValueClass(column.getValueClass());
			design.addField(field);
		}
	}

	private void addColumnHeaders(JasperDesign design, JasperSpecification spec) {
		JRDesignBand headerBand = new JRDesignBand();
		headerBand.setHeight(spec.getRowHeight() + 10);
		List<JRDesignStaticText> headers = new ArrayList<JRDesignStaticText>();
		int fieldPos = 0;
		for (JasperColumn column : spec.getReportColumns()) {
			headers.add(new JRDesignStaticText());
			JRDesignStaticText header = headers.get(headers.size() - 1);
			header.setX(fieldPos);
			header.setY(0);
			header.setText(column.getColumnHeader());
			header.setWidth(column.getWidth());
			header.setHeight(headerBand.getHeight() - 4);
			header.setBold(Boolean.TRUE);
			header.setHorizontalTextAlign(column.getAlignment());
			header.setPositionType(PositionTypeEnum.FLOAT);
			header.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
			header.getLineBox().getBottomPen().setLineWidth(Float.valueOf(1));
			header.getLineBox().getBottomPen().setLineColor(Color.BLACK);
			headerBand.addElement(header);
			column.setPosition(fieldPos);
			fieldPos = fieldPos + column.getWidth() + spec.getColumnSpacing();
		}
		design.setColumnHeader(headerBand);
	}

	private void addDetail(JasperDesign design, JasperSpecification spec) {
		JRDesignBand detailBand = new JRDesignBand();
		detailBand.setHeight(spec.getRowHeight());

		// Space past the grouping columns
		int fieldPos = 0;
		for (JasperColumn column : spec.getGroupColumns()) {
			fieldPos += column.getWidth() + spec.getColumnSpacing();
		}
		// Add the data columns
		for (JasperColumn column : spec.getDetailColumns()) {
			JRDesignTextField textField = getTextField(column, spec);
			textField.setHorizontalTextAlign(column.getAlignment());
			textField.setExpression(new JRDesignExpression("$F{" + column.getFieldName() + "}"));
			textField.setPositionType(PositionTypeEnum.FLOAT);
			if (column.isStretchHeight()){
				textField.setTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
			}
			detailBand.addElement(textField);
			fieldPos = fieldPos + column.getWidth() + spec.getColumnSpacing();
		}
		((JRDesignSection) design.getDetailSection()).addBand(detailBand);
	}

	private void addGroups(JasperDesign design, JasperSpecification spec) throws JRException {
		if (spec.getGroupColumns().size() == 0) {
			return;
		}
		List<JRDesignGroup> groups = new ArrayList<JRDesignGroup>();
		List<JRDesignBand> bands = new ArrayList<JRDesignBand>();
		List<JRDesignStaticText> staticTexts = new ArrayList<JRDesignStaticText>();
		for (JasperColumn column : spec.getGroupColumns()) {
			groups.add(new JRDesignGroup());
			JRDesignGroup group = groups.get(groups.size() - 1);
			group.setKeepTogether(true);
			group.setReprintHeaderOnEachPage(true);
			group.setName(column.getFieldName());
			group.setExpression(new JRDesignExpression());
			JRDesignExpression groupExpression = (JRDesignExpression) group.getExpression();
			groupExpression.setType(ExpressionTypeEnum.SIMPLE_TEXT);
			groupExpression.setText("$F{" + column.getFieldName() + "}");

			bands.add(new JRDesignBand());
			JRDesignBand groupHeaderBand = bands.get(bands.size() - 1);
			((JRDesignSection) group.getGroupHeaderSection()).addBand(groupHeaderBand);
			groupHeaderBand.setHeight(spec.getRowHeight());
			groupHeaderBand.setSplitType(SplitTypeEnum.PREVENT);
			JRDesignTextField textField = getTextField(column, spec);
			textField.setExpression(groupExpression);
			textField.setPattern(column.getPattern());
			groupHeaderBand.addElement(textField);

			bands.add(new JRDesignBand());
			JRDesignBand groupFooter = bands.get(bands.size() - 1);
			((JRDesignSection) group.getGroupFooterSection()).addBand(groupFooter);
			groupFooter.setHeight(spec.getRowHeight());
			staticTexts.add(new JRDesignStaticText());
			JRDesignStaticText staticText = staticTexts.get(staticTexts.size() - 1);
			staticText.setX(column.getPosition());
			staticText.setWidth(column.getWidth());
			staticText.setY(0);
			staticText.setHeight(spec.getRowHeight());
			staticText.setText("Total");
			staticText.setBold(Boolean.TRUE);
			groupFooter.addElement(staticText);
			for (JasperColumn detailColumn : spec.getDetailColumns()) {
				if (detailColumn.isSummarized()) {
					addGroupColumnToFooter(detailColumn, group, groupFooter, design, spec);
				}
			}
			design.addGroup(group);
		}
	}

	private void addGroupColumnToFooter(JasperColumn column, JRDesignGroup group, JRDesignBand groupFooter, JasperDesign design,
			JasperSpecification spec) throws JRException {
		JRDesignVariable variable = new JRDesignVariable();
		variable.setName(group.getName() + column.getFieldName());
		variable.setCalculation(CalculationEnum.SUM);
		variable.setValueClassName(column.getValueClass().getName());
		variable.setResetType(ResetTypeEnum.GROUP);
		variable.setInitialValueExpression(new JRDesignExpression(column.getValueClass().getSimpleName() + ".valueOf(0)"));
		variable.setExpression(new JRDesignExpression("($F{" + column.getFieldName() + "})"));
		variable.setResetGroup(group);
		design.addVariable(variable);

		JRDesignTextField textField = getTextField(column, spec);
		textField.setExpression(new JRDesignExpression("($V{" + variable.getName() + "})"));
		groupFooter.addElement(textField);
	}

	private JRDesignTextField getTextField(JasperColumn column, JasperSpecification spec) {
		JRDesignTextField textField = new JRDesignTextField();
		textField.setX(column.getPosition());
		textField.setY(0);
		textField.setWidth(column.getWidth());
		textField.setHeight(spec.getRowHeight());
		textField.setHorizontalTextAlign(column.getAlignment());
		textField.setPattern(column.getPattern());
		return textField;
	}

	private void addSummaries(JasperDesign design, JasperSpecification spec) throws JRException {
		boolean needSummary = false;
		for (JasperColumn column : spec.getDetailColumns()) {
			if (column.isSummarized()) {
				needSummary = true;
				break;
			}
		}
		if (!needSummary)
			return;

		JRDesignBand summaryBand = new JRDesignBand();
		summaryBand.setHeight(spec.getRowHeight());
		JRDesignStaticText staticText = new JRDesignStaticText();
		staticText.setX(0);
		if (spec.getGroupColumns().size() > 0) {
			staticText.setWidth(spec.getGroupColumns().get(0).getWidth());
		} else {
			staticText.setWidth(spec.getDetailColumns().get(0).getWidth());
		}
		staticText.setY(0);
		staticText.setHeight(spec.getRowHeight());
		staticText.setText("Overall Total");
		staticText.setBold(Boolean.TRUE);
		summaryBand.addElement(staticText);

		for (JasperColumn column : spec.getDetailColumns()) {
			if (column.isSummarized()) {
				addSummaryColumn(column, summaryBand, design, spec);
			}
		}
		design.setSummary(summaryBand);
	}

	private void addSummaryColumn(JasperColumn column, JRDesignBand summaryBand, JasperDesign design, JasperSpecification spec)
			throws JRException {
		JRDesignVariable variable = new JRDesignVariable();
		variable.setName("V" + column.getFieldName());
		variable.setCalculation(CalculationEnum.SUM);
		variable.setValueClassName(column.getValueClass().getName());
		variable.setInitialValueExpression(new JRDesignExpression(column.getValueClass().getSimpleName() + ".valueOf(0)"));
		variable.setExpression(new JRDesignExpression("($F{" + column.getFieldName() + "})"));
		design.addVariable(variable);

		JRDesignTextField textField = getTextField(column, spec);
		textField.setExpression(new JRDesignExpression("($V{" + variable.getName() + "})"));
		summaryBand.addElement(textField);
	}

}
