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
package org.bluewindows.figures.dao.impl.sqlite;

import static org.bluewindows.figures.domain.persistence.Persistence.*;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.dao.Mapper;
import org.bluewindows.figures.dao.SummaryDao;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.Summary;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.CategoryInclusion;
import org.bluewindows.figures.enums.CheckInclusion;
import org.bluewindows.figures.enums.DeductibleInclusion;
import org.bluewindows.figures.enums.TransactionInclusion;

public class SummaryDaoImplSqlite extends AbstractDaoImplSqlite implements SummaryDao {
	
	private AccountDaoImplSqlite accountDao;

	public SummaryDaoImplSqlite(PersistenceAdminDaoImplSqlite persistenceJdbc) {
		super(persistenceJdbc);
		accountDao = new AccountDaoImplSqlite(persistenceJdbc);
	}

	@Override
	public CallResult getReports() {
		CallResult result = executeQueryStatement("Select * From " + SUMMARY_STORE_NAME + " " +
		    "ORDER BY " + NAME);
		ResultSet resultSet = (ResultSet)result.getReturnedObject();
		if (result.isBad()) return result;
		result =  mapSummaryReports(resultSet);
		closeResultSet(resultSet);
		return result;
	}

	@SuppressWarnings("unchecked")
	private CallResult mapSummaryReports(ResultSet rs) {
		CallResult result = new CallResult();
		List<SummaryReport> reports = new ArrayList<SummaryReport>();
		try {
			while(rs.next()){
				SummaryReport report = new SummaryReport();
				report.setID(rs.getInt(ID));
				report.setName(rs.getString(NAME));
				report.setSummaryFields(rs.getString(SUMMARY_FIELDS));
				report.setTransactionInclusion(TransactionInclusion.valueOf(rs.getString(TRANSACTION_INCLUSION)));
				report.setCategoryInclusion(CategoryInclusion.valueOf(rs.getString(CATEGORY_INCLUSION)));
				report.setCategorizedOnly(rs.getBoolean(CATEGORIZED_ONLY));
				report.setDeductibleInclusion(DeductibleInclusion.valueOf(rs.getString(DEDUCTIBLE_INCLUSION)));
				report.setCheckInclusion(CheckInclusion.valueOf(rs.getString(CHECK_INCLUSION)));
				result = mapDate(rs, START_DATE);
				if (result.isBad()) return result;
				report.setDateRange(new DateRange());
				report.getDateRange().setStartDate((TransactionDate)result.getReturnedObject());
				result = mapDate(rs, END_DATE);
				if (result.isBad()) return result;
				report.getDateRange().setEndDate((TransactionDate)result.getReturnedObject());
				result = mapReportAccounts(report.getID());
				if (result.isBad()) return result;
				report.setAccounts((List<Account>)result.getReturnedObject());
				result = mapReportCategories(report.getID());
				if (result.isBad()) return result;
				report.setCategories((List<Category>)result.getReturnedObject());
				reports.add(report);
			}
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Summary Retrieval Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(reports);
	}

	private CallResult mapReportAccounts(int reportID) {
		CallResult result = executeQueryStatement("Select " + ACCOUNT_ID + " From " + SUMMARY_ACCOUNT_STORE_NAME + " " +
			"Where " + SUMMARY_ID + " = " + reportID);
		if (result.isBad()) return result;
		ResultSet rs = (ResultSet)result.getReturnedObject();
		List<Account> accounts = new ArrayList<Account>();
		try {
			while(rs.next()) {
				CallResult accountResult = accountDao.getAccount(rs.getInt(ACCOUNT_ID));
				if (accountResult.isBad()) return accountResult;
				accounts.add((Account)accountResult.getReturnedObject());
			}
			closeResultSet(rs);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Account Retrieval Failure For Summary Report", e.getLocalizedMessage());
		}
		return result.setReturnedObject(accounts);
	}

	private CallResult mapReportCategories(int reportID) {
		CallResult result = executeQueryStatement("Select sc." + CATEGORY_ID + ", c." + NAME + " From " + SUMMARY_CATEGORY_STORE_NAME + " sc " +
			"Join " + CATEGORY_STORE_NAME + " c " +
			"On sc." + CATEGORY_ID + " = c." + ID + " " +
			"Where sc." + SUMMARY_ID + " = " + reportID);
		if (result.isBad()) return result;
		ResultSet rs = (ResultSet)result.getReturnedObject();
		List<Category> categories = new ArrayList<Category>();
		try {
			while(rs.next()) {
				Category category = new Category(rs.getInt(CATEGORY_ID), rs.getString(NAME));
				categories.add(category);
			}
			closeResultSet(rs);
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Category Retrieval Failure For Summary Report", e.getLocalizedMessage());
		}
		return result.setReturnedObject(categories);
	}

	@Override
	public CallResult saveReport(SummaryReport report) {
		CallResult result = new CallResult();
		try {
			PreparedStatement summaryStmt = prepareStatement("INSERT INTO " + SUMMARY_STORE_NAME + 
				" (" + NAME + ", " + SUMMARY_FIELDS + ", " + TRANSACTION_INCLUSION + ", " + CATEGORY_INCLUSION + ", " + CATEGORIZED_ONLY + 
				", " + DEDUCTIBLE_INCLUSION + ", " + CHECK_INCLUSION +
				", " + START_DATE + ", " + END_DATE + ") VALUES(?,?,?,?,?,?,?,?,?)");
			summaryStmt.setString(1,report.getName());
			summaryStmt.setString(2, report.getSummaryFields());
			summaryStmt.setString(3, report.getTransactionInclusion().name());
			summaryStmt.setString(4, report.getCategoryInclusion().name());
			summaryStmt.setInt   (5, report.isCategorizedOnly()? 1: 0);
			summaryStmt.setString(6, report.getDeductibleInclusion().name());
			summaryStmt.setString(7, report.getCheckInclusion().name());
			if (report.getDateRange().getStartDate() != null) {
				summaryStmt.setString(8, report.getDateRange().getStartDate().value().format(JDBC_DATE_FORMAT));
				summaryStmt.setString(9, report.getDateRange().getEndDate().value().format(JDBC_DATE_FORMAT));
			}else {
				summaryStmt.setString(8, null);
				summaryStmt.setString(9, null);
			}
			summaryStmt.executeUpdate();
			summaryStmt.close();
			// Get the report ID so we can use it on the Account and Category tables
			CallResult rowIdResult = executeQueryStatement("select seq from sqlite_sequence where name = '" + SUMMARY_STORE_NAME + "'");
			if (rowIdResult.isBad()) return rowIdResult;
			ResultSet rs = (ResultSet)rowIdResult.getReturnedObject();
			report.setID(rs.getInt(1));
			closeResultSet(rs);
			result = saveAccounts(report, report.getAccounts());
			if (result.isBad()) return result;
			PreparedStatement accountStmt = (PreparedStatement)result.getReturnedObject();
			accountStmt.close();
			result = saveCategories(report, report.getCategories());
			if (result.isBad()) return result;
			PreparedStatement categoryStmt = (PreparedStatement)result.getReturnedObject();
			categoryStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Summary Save Failed", e.getLocalizedMessage());
		}		
		return result;
	}
	
	@Override
	public CallResult updateReport(SummaryReport report) {
		CallResult result = new CallResult();
		try {
			PreparedStatement summaryStmt = prepareStatement("Update " + SUMMARY_STORE_NAME + " " +
				"Set " + NAME + " = ?" + 
				", "   + SUMMARY_FIELDS + " = ?" +
				", "   + TRANSACTION_INCLUSION + " = ?" +
				", "   + CATEGORY_INCLUSION + " = ?" +
				", "   + CATEGORIZED_ONLY + " = ?" +
				", "   + DEDUCTIBLE_INCLUSION + " = ?" +
				", "   + CHECK_INCLUSION + " = ?" +
				", "   + START_DATE + " = ?" +
				", "   + END_DATE + " = ?" +
				"Where " + ID + " = ?");
			summaryStmt.setString(1, report.getName());
			summaryStmt.setString(2, report.getSummaryFields());
			summaryStmt.setString(3, report.getTransactionInclusion().name());
			summaryStmt.setString(4, report.getCategoryInclusion().name());
			summaryStmt.setInt(5, report.isCategorizedOnly()?1:0);
			summaryStmt.setString(6, report.getDeductibleInclusion().name());
			summaryStmt.setString(7, report.getCheckInclusion().name());
			if (report.getDateRange().getStartDate() != null) {
				summaryStmt.setString(8, report.getDateRange().getStartDate().value().format(JDBC_DATE_FORMAT));
				summaryStmt.setString(9, report.getDateRange().getEndDate().value().format(JDBC_DATE_FORMAT));
			}else {
				summaryStmt.setString(8, null);
				summaryStmt.setString(9, null);
			}
			summaryStmt.setInt(10, report.getID());
			summaryStmt.executeUpdate();
			summaryStmt.close();
			result = deleteAccounts(report, report.getDeletedAccounts());
			if (result.isBad()) return result;
			((PreparedStatement)result.getReturnedObject()).close();
			result = saveAccounts(report, report.getAddedAccounts());
			if (result.isBad()) return result;
			((PreparedStatement)result.getReturnedObject()).close();
			result = deleteCategories(report, report.getDeletedCategories());
			if (result.isBad()) return result;
			((PreparedStatement)result.getReturnedObject()).close();
			result = saveCategories(report, report.getAddedCategories());
			if (result.isBad()) return result;
			((PreparedStatement)result.getReturnedObject()).close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Summary Update Failed", e.getLocalizedMessage());
		}		
		return result;
	}
	
	@Override
	public CallResult deleteReport(SummaryReport report) {
		CallResult result = new CallResult();
		try {
			PreparedStatement accountStmt = prepareStatement("Delete From " + SUMMARY_ACCOUNT_STORE_NAME + " " + " Where " + SUMMARY_ID + " = ?");
			accountStmt.setInt(1, report.getID());
			accountStmt.executeUpdate();
			PreparedStatement categoryStmt = prepareStatement("Delete From " + SUMMARY_CATEGORY_STORE_NAME + " " + " Where " + SUMMARY_ID + " = ?");
			categoryStmt.setInt(1, report.getID());
			categoryStmt.executeUpdate();
			PreparedStatement summaryStmt = prepareStatement("Delete From " + SUMMARY_STORE_NAME + " " + " Where " + ID + " = ?");
			summaryStmt.setInt(1, report.getID());
			summaryStmt.executeUpdate();
			accountStmt.close(); 
			categoryStmt.close();
			summaryStmt.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad("Summary Update Failed", e.getLocalizedMessage());
		}		
		return result;
	}

	private CallResult saveAccounts(SummaryReport report, List<Account> accounts) throws SQLException {
		CallResult result = new CallResult();
		PreparedStatement pStmt = prepareStatement("INSERT INTO " + SUMMARY_ACCOUNT_STORE_NAME + 
				" (" + SUMMARY_ID + ", " + ACCOUNT_ID + ") VALUES(?,?)");
		for (Account account : accounts) {
			pStmt.setInt(1, report.getID());
			pStmt.setInt(2, account.getID());
			pStmt.executeUpdate();
		}
		return result.setReturnedObject(pStmt);
	}
	
	private CallResult deleteAccounts(SummaryReport report, List<Account> accounts) throws SQLException {
		CallResult result = new CallResult();
		PreparedStatement pStmt = prepareStatement("Delete From " + SUMMARY_ACCOUNT_STORE_NAME + 
				" Where " + SUMMARY_ID + " = ? And " + ACCOUNT_ID + " = ?");
		for (Account account : accounts) {
			pStmt.setInt(1, report.getID());
			pStmt.setInt(2, account.getID());
			pStmt.executeUpdate();
		}
		return result.setReturnedObject(pStmt);
	}
	
	private CallResult saveCategories(SummaryReport report, List<Category> categories) throws SQLException {
		CallResult result = new CallResult();
		PreparedStatement pStmt = prepareStatement("INSERT INTO " + SUMMARY_CATEGORY_STORE_NAME + 
				" (" + SUMMARY_ID + ", " + CATEGORY_ID + ") VALUES(?,?)");
		for (Category category : categories) {
			pStmt.setInt(1, report.getID());
			pStmt.setInt(2, category.getID());
			pStmt.executeUpdate();
		}
		return result.setReturnedObject(pStmt);
	}
	
	private CallResult deleteCategories(SummaryReport report, List<Category> categories) throws SQLException {
		CallResult result = new CallResult();
		PreparedStatement pStmt = prepareStatement("Delete From " + SUMMARY_CATEGORY_STORE_NAME + 
				" Where " + SUMMARY_ID + " = ? And " + CATEGORY_ID + " = ?");
		for (Category category : categories) {
			pStmt.setInt(1, report.getID());
			pStmt.setInt(2, category.getID());
			pStmt.executeUpdate();
		}
		return result.setReturnedObject(pStmt);
	}
	
	@Override
	public CallResult getSummaries(SummaryReport report) {
		CallResult result = buildAndExecuteSummaryQuery(report);
		if (result.isBad()) return result;
		ResultSet rs = (ResultSet)result.getReturnedObject();
		result = mapSummaries(rs, report.getSummaryFields());
		closeResultSet(rs);
		return result;
	}

	private CallResult buildAndExecuteSummaryQuery(SummaryReport report) {
		CallResult queryResult = new CallResult();
		StringBuffer sb = new StringBuffer("Select ");
		sb.append(addGroupingColumnsToSelect(report.getSummaryFields()));
		sb.append("sum(" + WITHDRAWALS_AMOUNT + ") as " + WITHDRAWALS_AMOUNT + ", ");
		sb.append("count(distinct WithdrawalID) as " + WITHDRAWALS_COUNT + ", ");
		sb.append("sum(" + DEPOSITS_AMOUNT + ") as " + DEPOSITS_AMOUNT + ", ");
		sb.append("count(distinct DepositID) as " + DEPOSITS_COUNT + " from ");
		sb.append("(");
		sb.append(addSubquerySelect(report));
		sb.append(addSubqueryWhere(report));
		sb.append(") ");
		sb.append(addGroupBy(report.getSummaryFields()));
		sb.append(addOrderBy(report.getSummaryFields()));
		queryResult = executeQueryStatement(sb.toString());
		return queryResult;
	}
	
	private String addSubquerySelect(SummaryReport report) {
		String categoryJoin;
		if (report.isCategorizedOnly()) {
			categoryJoin = "join ";
		}else {
			categoryJoin = "left outer join ";
		}
		return "Select t." + DATE + ", ifNull(c." + NAME + ", 'Uncategorized') as " + CATEGORY + ", " +
			DESCRIPTION + ", " + MEMO + ", " +
	        " (case when t." + AMOUNT + " < 0 then t." + ID + " else null end) as WithdrawalID," + 
	        " (case when t." + AMOUNT + " > 0 then t." + ID + " else null end) as DepositID," + 
	        " (case when t." + AMOUNT + " < 0 then ifNull(tc." + CATEGORY_AMOUNT + ",t." + AMOUNT + ") else null end) as " + WITHDRAWALS_AMOUNT + ", " +
	        " (case when t." + AMOUNT + " > 0 then ifNull(tc." + CATEGORY_AMOUNT + ",t." + AMOUNT + ") else null end) as " + DEPOSITS_AMOUNT + " " + 
	        " From " + TRANSACTION_STORE_NAME + " t " + categoryJoin + TRANSACTION_CATEGORY_STORE_NAME + " tc" +
			" on t." + ID + " = tc." + TRANSACTION_ID +
			" left outer join " + CATEGORY_STORE_NAME + " c" +
			" on tc." + CATEGORY_ID + " = c." + ID + " ";
	}

	private String addSubqueryWhere(SummaryReport report) {
		StringBuffer sb = new StringBuffer();
		sb.append("where t." + ACCOUNT_ID + " In (" + report.getAccounts().get(0).getID()); 
		for (int i = 1; i < report.getAccounts().size(); i++) {
			sb.append("," + report.getAccounts().get(i).getID());
		}
		sb.append(") and t." + DATE + " between '" + report.getDateRange().getStartDate().value().format(JDBC_DATE_FORMAT));
		sb.append("' and '" + report.getDateRange().getEndDate().value().format(JDBC_DATE_FORMAT) + "' ");
		if (!report.getCategories().isEmpty()) {
			if (report.getCategoryInclusion().equals(CategoryInclusion.INCLUDED)) {
				sb.append("and (tc." + CATEGORY_ID + " in (");
			}else {
				sb.append("and (tc." + CATEGORY_ID + " is null or tc." + CATEGORY_ID + " not in (");
			}
			sb.append(report.getCategories().get(0).getID());
			for (int i = 1; i < report.getCategories().size(); i++) {
				sb.append("," + report.getCategories().get(i).getID());
			}
			sb.append(")) ");
		}
		if (report.getTransactionInclusion().equals(TransactionInclusion.WITHDRAWALS)) {
			sb.append("and t." + AMOUNT + " < 0 ");
		}else if (report.getTransactionInclusion().equals(TransactionInclusion.DEPOSITS)) {
			sb.append("and t." + AMOUNT + " > 0 ");
		}
		if (!report.getDeductibleInclusion().equals(DeductibleInclusion.NONE)) {
			if (report.getDeductibleInclusion().equals(DeductibleInclusion.INCLUDED)) {
				sb.append("and t." + DEDUCTIBLE + " = 1 ");
			}else {
				sb.append("and t." + DEDUCTIBLE + " = 0 ");
			}
		}
		if (!report.getCheckInclusion().equals(CheckInclusion.NONE)) {
			if (report.getCheckInclusion().equals(CheckInclusion.INCLUDED)) {
				sb.append("and t." + NUMBER + " <> '' ");
			}else {
				sb.append("and t." + NUMBER + " = '' ");
			}
		}
		return sb.toString();
	}
	
	private String addGroupingColumnsToSelect(String groupFields) {
		if (groupFields.isEmpty()) return "";
		String groupingColumns = new String(groupFields + ", ");
		groupingColumns = StringUtils.replace(groupingColumns, YEAR, "strftime('%Y', " + DATE + ") as " + YEAR );
		groupingColumns = StringUtils.replace(groupingColumns, MONTH, "strftime('%m', " + DATE + ") as " + MONTH);
		return groupingColumns;
	}
	
	private String addGroupBy(String groupFields) {
		if (groupFields.isEmpty()) return "";
		return "Group By " + groupFields + " ";
	}
	
	private String addOrderBy(String groupFields) {
		if (groupFields.isEmpty()) return "";
		return "Order By " + groupFields + " ";
	}

	private CallResult mapSummaries(ResultSet rs, String summaryFields) {
		CallResult result = new CallResult();
		List<Summary> summaries = new ArrayList<Summary>();
		List<Mapper> keyMappers = getKeyMappers(summaryFields);
		try {
			while (rs.next()) {
				Summary summary = new Summary();
				for (Mapper mapper : keyMappers) {
					summary.getSummaryKeys().add(mapper.map(rs));
				}
				mapValues(rs, summary);
				summaries.add(summary);
			}
			rs.close();
		} catch (SQLException e) {
			Figures.logStackTrace(e);
			result.setCallBad("Summary Retrieval Failure", e.getLocalizedMessage());
		}
		return result.setReturnedObject(summaries);
	}
	
	private List<Mapper> getKeyMappers(String summaryFieldString) {
		List<Mapper> mappers = new ArrayList<Mapper>();
		List<String> summaryFields = Arrays.asList(summaryFieldString.split(","));
		for (String fieldName : summaryFields) {
			fieldName = fieldName.trim();
			if (fieldName.equals(YEAR)) {
				IntMapperSqlite mapper = new IntMapperSqlite(fieldName);
				mappers.add(mapper);
			}else if (fieldName.equals(MONTH)) {
				MonthMapperSqlite mapper = new MonthMapperSqlite(fieldName);
				mappers.add(mapper);
			}else {
				StringMapperSqlite mapper = new StringMapperSqlite(fieldName);
				mappers.add(mapper);
			}
		}
		return mappers;
	}

	private void mapValues(ResultSet rs, Summary summary) throws SQLException {
		if (rs.getBigDecimal(WITHDRAWALS_AMOUNT) != null) summary.setWithdrawalsAmount(rs.getBigDecimal(WITHDRAWALS_AMOUNT));
		summary.setWithdrawalsCount(rs.getInt(WITHDRAWALS_COUNT));
		if (rs.getBigDecimal(DEPOSITS_AMOUNT) != null) summary.setDepositsAmount(rs.getBigDecimal(DEPOSITS_AMOUNT));
		summary.setDepositsCount(rs.getInt(DEPOSITS_COUNT));
	}

}
