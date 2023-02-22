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

package org.bluewindows.figures.domain.persistence;

public abstract class Persistence {

	public static final String ACCOUNT_STORE_NAME = "Accounts";
	public static final String CATEGORY_STORE_NAME = "Categories";
	public static final String TRANSACTION_STORE_NAME = "Transactions";
	public static final String TRANSACTION_CATEGORY_STORE_NAME = "TransactionCategory";
	public static final String FILTER_STORE_NAME = "Filters";
	public static final String FILTER_SET_STORE_NAME = "FilterSets";
	public static final String SUMMARY_STORE_NAME = "Summary";
	public static final String SUMMARY_ACCOUNT_STORE_NAME = "SummaryAccount";
	public static final String SUMMARY_CATEGORY_STORE_NAME = "SummaryCategory";
	public static final String ID = "ID";
	public static final String ACCOUNT_ID = "AccountID";
	public static final String CATEGORY_ID = "CategoryID";
	public static final String TRANSACTION_CATEGORY_ID = "TransactionCategoryID";
	public static final String CATEGORY_AMOUNT = "CategoryAmount";
	public static final String FILTER_SET_ID = "FilterSetID";
	public static final String DEFAULT_FIELD = "DefaultField";
	public static final String DEFAULT_EXPRESSION = "DefaultExpression";
	public static final String DEFAULT_RESULT = "DefaultResult";
	public static final String TRANSACTION_ID = "TransactionID";
	public static final String SUMMARY_ID = "SummaryID";
	public static final String NAME = "Name";
	public static final String NUMBER = "Number";
	public static final String DATE = "Date";
	public static final String START_DATE = "StartDate";
	public static final String END_DATE = "EndDate";
	public static final String TYPE = "Type";
	public static final String DESCRIPTION = "Description";
	public static final String ORIG_DESC = "OriginalDescription";
	public static final String AMOUNT = "Amount";
	public static final String MEMO = "Memo";
	public static final String ORIG_MEMO = "OriginalMemo";
	public static final String CATEGORY = "Category";
	public static final String SEQUENCE = "Sequence";
	public static final String FIELD = "Field";
	public static final String EXPRESSION = "Expression";
	public static final String VALUE = "Value";
	public static final String RESULT = "Result";
	public static final String REPLACEMENT = "Replacement";
	public static final String DEDUCTIBLE = "Deductible";
	public static final String USER_CHANGED_DESC = "UserChangedDesc";
	public static final String USER_CHANGED_MEMO = "UserChangedMemo";
	public static final String USER_CHANGED_CATEGORY = "UserChangedCategory";
	public static final String USER_CHANGED_DEDUCTIBLE = "UserChangedDeductible";
	public static final String BALANCE = "Balance";
	public static final String INITIAL_BALANCE = "InitialBalance";
	public static final String LAST_LOAD_DATE = "LastLoadDate";
	public static final String LAST_FILTER_DATE = "LastFilterDate";
	public static final String SUMMARY_FIELDS = "SummaryFields";
	public static final String TRANSACTION_INCLUSION = "TransactionInclusion";
	public static final String DEDUCTIBLE_INCLUSION = "DeductibleInclusion";
	public static final String CHECK_INCLUSION = "CheckInclusion";
	public static final String CATEGORY_INCLUSION = "CategoryInclusion";
	public static final String CATEGORIZED_ONLY = "CategorizedOnly";
	public static final String YEAR = "Year";
	public static final String MONTH = "Month";
	public static final String WITHDRAWALS_AMOUNT = "WithdrawalsAmount";
	public static final String WITHDRAWALS_COUNT = "WithdrawalsCount";
	public static final String DEPOSITS_AMOUNT = "DepositsAmount";
	public static final String DEPOSITS_COUNT = "DepositsCount";
	public static final String IMPORT_FOLDER = "ImportFolder";
	
}
