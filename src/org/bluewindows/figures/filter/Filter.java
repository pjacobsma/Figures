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

package org.bluewindows.figures.filter;

import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.enums.Deductible;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.FilterResult;

public class Filter {
	
	private int id = 0;
	private int filterSetID;
	private int sequence = 0;
	private float reSequence;
	private String field = "";
	private String expression = "";
	protected String searchValue = "";
	private String resultAction = "";
	private String replacementValue = "";
	private boolean deductible = false;
	private Integer categoryID;
	private SearchInterface searchStrategy;
	private SourceFieldInterface searchSourceField;
	private SourceFieldInterface resultSourceField;
	private List<ResultFieldInterface> results = new ArrayList<ResultFieldInterface>();
	
	public Filter(int id, int filterSetID, int sequence, String fieldType, String expression, 
			String searchValue, String resultAction, String replacementValue, boolean deductible, Integer categoryID){
		this.id = id;
		this.filterSetID = filterSetID;
		this.sequence = sequence;
		this.field = fieldType;
		this.expression = expression;
		this.searchValue = searchValue;
		this.resultAction = resultAction;
		this.replacementValue = replacementValue;
		this.deductible = deductible;
		this.categoryID = categoryID;
		setSearchSourceField(FilterField.findByText(fieldType));
		setSearchStrategy(FilterExpression.findByText(expression));
		setResultSourceField(FilterResult.findByText(resultAction));
		setResults(FilterResult.findByText(resultAction), deductible);
	}

	public boolean execute(Transaction transaction) {
		boolean filterHit = false;
		SourceFieldInterface sourceFieldForThisTransaction = searchSourceField;
		// If the user changed the source field, filter on the user's value
		if (transaction.isUserChangedDesc() && searchSourceField instanceof SourceOriginalDescription) {
			sourceFieldForThisTransaction = new SourceUserDescription();
		}
		if (transaction.isUserChangedMemo() && searchSourceField instanceof SourceOriginalMemo) {
			sourceFieldForThisTransaction = new SourceUserMemo();
		}
		if (searchStrategy.found(transaction, sourceFieldForThisTransaction, searchValue)) {
			for (ResultFieldInterface resultField : results) {
				if (resultField.isOkToExecute(transaction)) {
					resultField.execute(transaction);
					filterHit = true;
				}			
			}
		}
		return filterHit;
	}
	
	public int getID() {
		return id;
	}

	public String getName(){
		return field + " " + expression + " " + searchValue;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getField() {
		return field;
	}

	public String getFieldName() {
		return field;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public String getSearchValue() {
		return searchValue;
	}
	
	public String getResultAction() {
		return resultAction;
	}

	public int getFilterSetID() {
		return filterSetID;
	}
	
	public String getReplacementValue() {
		return replacementValue;
	}
	
	public void setReplacementValue(String replacementValue) {
		this.replacementValue = replacementValue;
	}
	
	public SearchInterface getFieldStrategy() {
		return searchStrategy;
	}
	
	private void setSearchSourceField(FilterField fieldType) {
		if (fieldType.equals(FilterField.DESCRIPTION)) {
			searchSourceField = new SourceOriginalDescription();
		}else if (fieldType.equals(FilterField.MEMO)) {
			searchSourceField = new SourceOriginalMemo();
		}else {
			searchSourceField = new SourceLiteral(replacementValue);
		}
	}
	
	private void setResultSourceField(FilterResult filterResult) {
		if (filterResult.equals(FilterResult.COPY_TO_DESCRIPTION) || filterResult.equals(FilterResult.COPY_TO_MEMO)) {
			resultSourceField = searchSourceField;
		}else if (filterResult.equals(FilterResult.REPLACE_DESCRIPTION) || filterResult.equals(FilterResult.REPLACE_MEMO)) {
			resultSourceField = new SourceLiteral(replacementValue);
		}
	}

	private void setSearchStrategy(FilterExpression expression){
		if (expression == FilterExpression.CONTAINS){
			searchStrategy = new SearchContains();
		}else if (expression == FilterExpression.EQUALS){
			searchStrategy = new SearchEquals();
		}else if (expression == FilterExpression.STARTSWITH){
			searchStrategy = new SearchStarts();
		}else if (expression == FilterExpression.ENDSWITH){
			searchStrategy = new SearchEnds();
		}	
	}
	
	private void setResults(FilterResult filterResult, boolean deductible) {
		ResultFieldInterface resultField = null;
		if (filterResult.equals(FilterResult.COPY_TO_DESCRIPTION) || filterResult.equals(FilterResult.REPLACE_DESCRIPTION)) {
			resultField = new ResultDescription(); 
		}else if (filterResult.equals(FilterResult.COPY_TO_MEMO) || filterResult.equals(FilterResult.REPLACE_MEMO)) {
			resultField = new ResultMemo(); 
		}
		// resultField might be null if the only action is to set a category or set this transaction deductible
		if (resultField != null) {
			resultField.setResultSource(resultSourceField);
			results.add(resultField);
		}
		// categoryID might be null if the filter does not specify a category
		if (categoryID != null) {
			ResultCategory resultCategory = new ResultCategory();
			SourceCategory sourceCategory = new SourceCategory(categoryID);
			resultCategory.setResultSource(sourceCategory);
			results.add(resultCategory);
		}
		if (deductible) {
			ResultDeductible resultDeductible = new ResultDeductible();
			results.add(resultDeductible);
		}
	}

	public boolean isDeductible() {
		return deductible;
	}
	
	public Deductible getDeductible() {
		return deductible? Deductible.YES: Deductible.NO;
	}
	
	public Integer getCategoryID() {
		return categoryID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (deductible ? 1231 : 1237);
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((searchStrategy == null) ? 0 : searchStrategy.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + filterSetID;
		result = prime * result + id;
		result = prime * result + ((replacementValue == null) ? 0 : replacementValue.hashCode());
		result = prime * result + ((resultAction == null) ? 0 : resultAction.hashCode());
		result = prime * result + ((searchValue == null) ? 0 : searchValue.hashCode());
		result = prime * result + sequence;
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
		Filter other = (Filter) obj;
		if (deductible != other.deductible)
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (filterSetID != other.filterSetID)
			return false;
		if (id != other.id)
			return false;
		if (replacementValue == null) {
			if (other.replacementValue != null)
				return false;
		} else if (!replacementValue.equals(other.replacementValue))
			return false;
		if (resultAction == null) {
			if (other.resultAction != null)
				return false;
		} else if (!resultAction.equals(other.resultAction))
			return false;
		if (searchValue == null) {
			if (other.searchValue != null)
				return false;
		} else if (!searchValue.equals(other.searchValue))
			return false;
		if (sequence != other.sequence)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Filter [id=" + id + ", filterSetID=" + filterSetID + ", sequence=" + sequence + ", field=" + field
				+ ", expression=" + expression + ", searchValue=" + searchValue + ", resultAction=" + resultAction
				+ ", replacementValue=" + replacementValue + ", deductible=" + deductible
				+ ", searchStrategy=" + searchStrategy + "]";
	}
	
	public float getReSequence() {
		return reSequence;
	}

	public void setReSequence(float reSequence) {
		this.reSequence = reSequence;
	}


}
