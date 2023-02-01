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
package org.bluewindows.figures.service.impl.jasper;

import static net.sf.jasperreports.engine.type.HorizontalTextAlignEnum.CENTER;
import static net.sf.jasperreports.engine.type.HorizontalTextAlignEnum.LEFT;
import static net.sf.jasperreports.engine.type.HorizontalTextAlignEnum.RIGHT;
import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY;
import static org.bluewindows.figures.domain.persistence.Persistence.DEPOSITS_AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.DEPOSITS_COUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.DESCRIPTION;
import static org.bluewindows.figures.domain.persistence.Persistence.MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.MONTH;
import static org.bluewindows.figures.domain.persistence.Persistence.WITHDRAWALS_AMOUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.WITHDRAWALS_COUNT;
import static org.bluewindows.figures.domain.persistence.Persistence.YEAR;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.SearchCriterion;
import org.bluewindows.figures.domain.Summary;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.enums.CategoryInclusion;
import org.bluewindows.figures.jasper.JasperColumn;
import org.bluewindows.figures.jasper.JasperGenerator;
import org.bluewindows.figures.jasper.JasperSpecification;
import org.bluewindows.figures.service.ReportService;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.design.JasperDesign;

public class ReportServiceImplJasper implements ReportService {

	private static final String PDF_ENCODING = "UTF-8";
	private static final int FONT_SIZE = 9;
	private static final int TITLE_FONT_SIZE = 14;
	private static final int TITLE_HEIGHT = 20;
	private static final int PAGE_WIDTH = 612;
	private static final int PAGE_HEIGHT = 840;
	private static final int LEFT_MARGIN = 20;
	private static final int RIGHT_MARGIN = 20;
	private static final int TOP_MARGIN = 30;
	private static final int BOTTOM_MARGIN = 30;
	private static final int ROW_HEIGHT = 14;
	private static final int ROW_SPACING = 3;
	private static final int COLUMN_SPACING = 1;
	private static ReportServiceImplJasper instance;
	private static JasperGenerator reportGenerator = new JasperGenerator();
	
	public static ReportServiceImplJasper getInstance() {
		if (instance == null) {
			instance = new ReportServiceImplJasper();
		}
		return instance;
	}
	
	private void setLayoutDefaults(JasperSpecification spec) {
		spec.setFontName(Figures.fontName);
		spec.setPdfFontName(Figures.fontName);
		spec.setPdfEncoding(PDF_ENCODING);
		spec.setFontSize(FONT_SIZE);
		spec.setTitleFontSize(TITLE_FONT_SIZE);
		spec.setTitleHeight(TITLE_HEIGHT);
		spec.setPageWidth(PAGE_WIDTH);
		spec.setPageHeight(PAGE_HEIGHT);
		spec.setLeftMargin(LEFT_MARGIN);
		spec.setRightMargin(RIGHT_MARGIN);
		spec.setTopMargin(TOP_MARGIN);
		spec.setBottomMargin(BOTTOM_MARGIN);
		spec.setRowHeight(ROW_HEIGHT);
		spec.setRowSpacing(ROW_SPACING);
		spec.setColumnSpacing(COLUMN_SPACING);
	}
	
	@Override
	public CallResult createDetailReport(List<SearchCriterion> searchCriteria, List<Transaction> transactions, String accountName, String title) {
		CallResult result = new CallResult();
		JasperSpecification spec = new JasperSpecification();
		setLayoutDefaults(spec);
		if (title == null || title.isEmpty()) {
			spec.addHeading("Transaction Detail");
		}else {
			spec.addHeading(title);
		}
		spec.setRunDate(Figures.dateFormat.format(LocalDate.now()));
		spec.addSearchCriteria(new ImmutablePair<String, String>("Account", accountName));
		addDetailSearchCriteria(searchCriteria, spec);
		addDetailDataColumns(spec);

    	List<PrintableTransaction> printableTransactions = getPrintableTransactions(transactions);
    	
		JasperPrint print;
		try {
			JasperDesign design = reportGenerator.createDesign(spec);
	        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(printableTransactions);
			print = createJasperReport(design, dataSource);
		} catch (JRException e) {
			Figures.logger.severe(e.getLocalizedMessage());
			return result.setCallBad("Summary Report Creation Error", e.getLocalizedMessage());
		}
		return result.setReturnedObject(print);
	}
	
	private JasperPrint createJasperReport(JasperDesign design, JRBeanCollectionDataSource dataSource) throws JRException {
		DefaultJasperReportsContext.getInstance().setProperty(JRCompiler.COMPILER_TEMP_DIR, System.getProperty("java.io.tmpdir"));
		JasperReport report = JasperCompileManager.compileReport(design);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(JRParameter.REPORT_LOCALE, Locale.getDefault());
		return JasperFillManager.fillReport(report, parameterMap, dataSource);
	}
	
	private void addDetailSearchCriteria(List<SearchCriterion> searchCriteria, JasperSpecification spec) {
		for (SearchCriterion criterion : searchCriteria) {
			spec.addSearchCriteria(new ImmutablePair<String, String>(criterion.getLabel(), criterion.getValue()));
		}
	}
	
	// The field names for these columns must match getters in the PrintableTransaction class
	private void addDetailDataColumns(JasperSpecification spec) {
		JasperColumn numberColumn = new JasperColumn("Number", 36, RIGHT, String.class, null, false);
		numberColumn.setFieldName("number");
		spec.addDetailColumn(numberColumn);
		JasperColumn dateColumn = new JasperColumn("Date", 48, LEFT, String.class, null, false);
		dateColumn.setFieldName("date");
		spec.addDetailColumn(dateColumn);
		JasperColumn descriptionColumn = new JasperColumn("Description", 180, LEFT, String.class, null, false);
		descriptionColumn.setFieldName("description");
		descriptionColumn.setStretchHeight(true);
		spec.addDetailColumn(descriptionColumn);
		JasperColumn amountColumn = new JasperColumn("Amount", 60, RIGHT, BigDecimal.class, "#,##0.00", false);
		amountColumn.setFieldName("amount");
		spec.addDetailColumn(amountColumn);
		JasperColumn categoryColumn = new JasperColumn("Categories", 140, LEFT, String.class, null, false);
		categoryColumn.setFieldName("categoryList");
		categoryColumn.setStretchHeight(true);
		spec.addDetailColumn(categoryColumn);
		JasperColumn memoColumn = new JasperColumn("Memo", 180, LEFT, String.class, null, false);
		memoColumn.setFieldName("memo");
		memoColumn.setStretchHeight(true);
		spec.addDetailColumn(memoColumn);
		JasperColumn deductibleColumn = new JasperColumn("Deductible", 50, CENTER, String.class, null, false);
		deductibleColumn.setFieldName("deductible");
		spec.addDetailColumn(deductibleColumn);
		JasperColumn balanceColumn = new JasperColumn("Balance", 50, RIGHT, BigDecimal.class, "#,##0.00", false);
		balanceColumn.setFieldName("balance");
		spec.addDetailColumn(balanceColumn);
	}

	
	@Override
	public CallResult createSummaryReport(SummaryReport report, List<Summary> summaries) {
		CallResult result = new CallResult();
		JasperSpecification spec = new JasperSpecification();
		setLayoutDefaults(spec);
		spec.addHeading(report.getName());
		spec.setRunDate(Figures.dateFormat.format(LocalDate.now()));
		addSummarySearchCriteria(report, spec);
		if (report.getSummaryFields().contains(MONTH)) {
			setMonthNames(report, summaries);
		}
		Map<String, JasperColumn> columnMap = getSummaryColumnMap();
		addSummaryGroupColumns(report, spec, columnMap);
		addSummaryDataColumns(report, spec, columnMap);
		JasperPrint print = null;
		try {
			JasperDesign design = reportGenerator.createDesign(spec);
	        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(summaries);
			print = createJasperReport(design, dataSource);
		} catch (JRException e) {
			Figures.logger.severe(e.getCause().getLocalizedMessage());
			return result.setCallBad("Summary Report Creation Error", e.getLocalizedMessage());
		}
		return result.setReturnedObject(print);
	}
	
	private Map<String, JasperColumn> getSummaryColumnMap() {
		Map<String, JasperColumn> columnMap = new HashMap<String, JasperColumn>();
		columnMap = new HashMap<String, JasperColumn>();
		columnMap.put(YEAR, new JasperColumn("Year", 66, LEFT, String.class, null, false));
		columnMap.put(MONTH, new JasperColumn("Month", 60, LEFT, String.class, null, false));
		JasperColumn categoryColumn = new JasperColumn("Category", 120, LEFT, String.class, null, false);
		categoryColumn.setStretchHeight(true);
		columnMap.put(CATEGORY, categoryColumn);
		JasperColumn descriptionColumn = new JasperColumn("Description", 200, LEFT, String.class, null, false);
		descriptionColumn.setStretchHeight(true);
		columnMap.put(DESCRIPTION, descriptionColumn);
		JasperColumn memoColumn = new JasperColumn("Memo", 200, LEFT, String.class, null, false);
		memoColumn.setStretchHeight(true);
		columnMap.put(MEMO, memoColumn);
		columnMap.put(WITHDRAWALS_COUNT, new JasperColumn("Withdrawals", 68, RIGHT, Long.class, "#,##0", true));
		columnMap.put(WITHDRAWALS_AMOUNT,new JasperColumn("Amount", 64, RIGHT, BigDecimal.class, "#,##0.00", true));
		columnMap.put(DEPOSITS_COUNT, new JasperColumn("Deposits", 46, RIGHT, Long.class, "#,##0", true));
		columnMap.put(DEPOSITS_AMOUNT, new JasperColumn("Amount", 64, RIGHT, BigDecimal.class, "#,##0.00", true));
		return columnMap;
	}
	
	private void addSummarySearchCriteria(SummaryReport report, JasperSpecification spec) {
		spec.addSearchCriteria(new ImmutablePair<String, String>("Date Range", report.getDateRange().getStartDate() + " - " + report.getDateRange().getEndDate()));
		String accounts;
		accounts = report.getAccounts().get(0).getName();
		for (int i = 1; i < report.getAccounts().size(); i++) {
			accounts = accounts + ", " + report.getAccounts().get(i).getName();
		}
		if (report.getAccounts().size() > 1) {
			spec.addSearchCriteria(new ImmutablePair<String, String>("Accounts", accounts));
		}else {
			spec.addSearchCriteria(new ImmutablePair<String, String>("Account", accounts));
		}
		if (!report.getCategoryInclusion().equals(CategoryInclusion.ALL)){
			String categoryLabel;
			if (report.getCategoryInclusion().equals(CategoryInclusion.INCLUDED)){
				categoryLabel = "Categories";
			}else {
				categoryLabel = "Categories Excluded";
			}
			String categories = report.getCategories().get(0).getName();
			for (int i = 1; i < report.getCategories().size(); i++) {
				categories = categories + ", " + report.getCategories().get(i).getName();
			}
			spec.addSearchCriteria(new ImmutablePair<String, String>(categoryLabel, categories));
		}
		switch (report.getTransactionInclusion()) {
			case ALL:
				break;
			case DEPOSITS:
				spec.addSearchCriteria(new ImmutablePair<String, String>("Deposits Only", ""));
				break;
			case WITHDRAWALS:
				spec.addSearchCriteria(new ImmutablePair<String, String>("Withdrawals Only", ""));
				break;
		}
		switch (report.getDeductibleInclusion()) {
			case EXCLUDED:
				spec.addSearchCriteria(new ImmutablePair<String, String>("Deductibles Excluded", ""));
				break;
			case INCLUDED:
				spec.addSearchCriteria(new ImmutablePair<String, String>("Deductibles Only", ""));
				break;
			case NONE:
				break;
		}
	}
	
	// The field names for these columns must match getters in the Summary class
	private void addSummaryGroupColumns(SummaryReport report, JasperSpecification spec, Map<String, JasperColumn> columnMap) {
		String[] groupFields = report.getSummaryFields().split(",");
		for (int i = 0; i < groupFields.length; i++) {
			JasperColumn column = columnMap.get(groupFields[i].trim());
			column.setFieldName("key" + (i+1));
			spec.addGroupColumn(column);
		}
	}
	
	// The field names for these columns must match getters in the Summary class
	private void addSummaryDataColumns(SummaryReport report, JasperSpecification spec, Map<String, JasperColumn> columnMap) {
		switch (report.getTransactionInclusion()) {
		case ALL:
			JasperColumn withdrawalsCountColumn = columnMap.get(WITHDRAWALS_COUNT);
			withdrawalsCountColumn.setFieldName("withdrawalsCount");
			spec.addDetailColumn(withdrawalsCountColumn);
			JasperColumn withdrawalsColumn = columnMap.get(WITHDRAWALS_AMOUNT);
			withdrawalsColumn.setFieldName("withdrawalsAmount");
			spec.addDetailColumn(withdrawalsColumn);
			JasperColumn depositsCountColumn = columnMap.get(DEPOSITS_COUNT);
			depositsCountColumn.setFieldName("depositsCount");
			spec.addDetailColumn(depositsCountColumn);
			JasperColumn depositsColumn = columnMap.get(DEPOSITS_AMOUNT);
			depositsColumn.setFieldName("depositsAmount");
			spec.addDetailColumn(depositsColumn);
			break;
		case WITHDRAWALS:
			withdrawalsCountColumn = columnMap.get(WITHDRAWALS_COUNT);
			withdrawalsCountColumn.setFieldName("withdrawalsCount");
			spec.addDetailColumn(withdrawalsCountColumn);
			withdrawalsColumn = columnMap.get(WITHDRAWALS_AMOUNT);
			withdrawalsColumn.setFieldName("withdrawalsAmount");
			spec.addDetailColumn(withdrawalsColumn);
			break;
		case DEPOSITS:
			depositsCountColumn = columnMap.get(DEPOSITS_COUNT);
			depositsCountColumn.setFieldName("depositsCount");
			spec.addDetailColumn(depositsCountColumn);
			depositsColumn = columnMap.get(DEPOSITS_AMOUNT);
			depositsColumn.setFieldName("depositsAmount");
			spec.addDetailColumn(depositsColumn);
			break;
		}
	}

	
	private List<PrintableTransaction> getPrintableTransactions(List<Transaction> transactions) {
		List<PrintableTransaction> printableTransactions = new ArrayList<PrintableTransaction>();
		for (Transaction transaction : transactions) {
			printableTransactions.add(new PrintableTransaction(transaction));
		}
		return printableTransactions;
	}
		
	private void setMonthNames(SummaryReport report, List<Summary> summaries) {
		String[] keyFields = report.getSummaryFields().split(",");
		int monthIndex = 0;
		for (int i = 0; i < keyFields.length; i++) {
			if (keyFields[i].trim().equals(MONTH)) {
				monthIndex = i;
				break;
			}
		}
		for (Summary summary : summaries) {
			switch (summary.getSummaryKeys().get(monthIndex)) {
				case "01": summary.getSummaryKeys().set(monthIndex, "January");break;
				case "02": summary.getSummaryKeys().set(monthIndex, "February");break;
				case "03": summary.getSummaryKeys().set(monthIndex, "March");break;
				case "04": summary.getSummaryKeys().set(monthIndex, "April");break;
				case "05": summary.getSummaryKeys().set(monthIndex, "May");break;
				case "06": summary.getSummaryKeys().set(monthIndex, "June");break;
				case "07": summary.getSummaryKeys().set(monthIndex, "July");break;
				case "08": summary.getSummaryKeys().set(monthIndex, "August");break;
				case "09": summary.getSummaryKeys().set(monthIndex, "September");break;
				case "10": summary.getSummaryKeys().set(monthIndex, "October");break;
				case "11": summary.getSummaryKeys().set(monthIndex, "November");break;
				case "12": summary.getSummaryKeys().set(monthIndex, "December");break;
			}
		}
	}

	

}
