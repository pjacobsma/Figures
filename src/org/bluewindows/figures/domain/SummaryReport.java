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

import org.bluewindows.figures.enums.CategoryInclusion;
import org.bluewindows.figures.enums.CheckInclusion;
import org.bluewindows.figures.enums.DeductibleInclusion;
import org.bluewindows.figures.enums.TransactionInclusion;

public class SummaryReport {
	
	private int id;
	private String name = new String();
	private List<Account> accounts = new ArrayList<Account>();
	protected List<Account> addedAccounts = new ArrayList<Account>();
	protected List<Account> deletedAccounts = new ArrayList<Account>();
	private DateRange dateRange = new DateRange();
	private String summaryFields = "";
	private TransactionInclusion transactionInclusion;
	private CategoryInclusion categoryInclusion;
	private DeductibleInclusion deductibleInclusion;
	private CheckInclusion checkInclusion;
	private boolean categorizedOnly;

	private List<Category> categories = new ArrayList<Category>();
	protected List<Category> addedCategories = new ArrayList<Category>();
	protected List<Category> deletedCategories = new ArrayList<Category>();

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

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		if (!this.accounts.isEmpty()) {
			throw new UnsupportedOperationException("Cannot use this method to update an existing summary report.");
		}
		this.accounts = accounts;
	}

	public void addAccount(Account account) {
		if (accounts.contains(account)) {
			throw new UnsupportedOperationException("Duplicate account added to summary report.");
		}
		accounts.add(account);
		addedAccounts.add(account);
	}
	
	public List<Account> getAddedAccounts(){
		return addedAccounts;
	}
	
	public void deleteAccount(Account account) {
		if (this.accounts.isEmpty()) {
			throw new UnsupportedOperationException("Can only use this method to update an existing summary report.");
		}
		if (!accounts.remove(account)) {
			throw new UnsupportedOperationException("Account not found in this summary report.");
		};
		deletedAccounts.add(account);
	}
	
	public List<Account> getDeletedAccounts(){
		return deletedAccounts;
	}

	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}
	
	public String getSummaryFields() {
		return summaryFields;
	}

	public void setSummaryFields(String summaryFields) {
		this.summaryFields = summaryFields;
	}

	public TransactionInclusion getTransactionInclusion() {
		return transactionInclusion;
	}

	public void setTransactionInclusion(TransactionInclusion inclusion) {
		this.transactionInclusion = inclusion;
	}

	public boolean isCategorizedOnly() {
		return categorizedOnly;
	}

	public void setCategorizedOnly(boolean categorizedOnly) {
		this.categorizedOnly = categorizedOnly;
	}
	
	public CategoryInclusion getCategoryInclusion() {
		return categoryInclusion;
	}

	public void setCategoryInclusion(CategoryInclusion categoryInclusion) {
		this.categoryInclusion = categoryInclusion;
	}
	
	public DeductibleInclusion getDeductibleInclusion() {
		return deductibleInclusion;
	}

	public void setDeductibleInclusion(DeductibleInclusion deductibleInclusion) {
		this.deductibleInclusion = deductibleInclusion;
	}

	public CheckInclusion getCheckInclusion() {
		return checkInclusion;
	}

	public void setCheckInclusion(CheckInclusion checkInclusion) {
		this.checkInclusion = checkInclusion;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		if (!this.categories.isEmpty()) {
			throw new UnsupportedOperationException("Cannot use this method to update an existing summary report.");
		}
		this.categories = categories;
	}
	
	public void addCategory(Category category) {
		if (categories.contains(category)) {
			throw new UnsupportedOperationException("Duplicate category added to summary report.");
		}
		categories.add(category);
		addedCategories.add(category);
	}
	
	public List<Category> getAddedCategories(){
		return addedCategories;
	}
	
	public void deleteCategory(Category category) {
		if (this.categories.isEmpty()) {
			throw new UnsupportedOperationException("Can only use this method to update an existing summary report.");
		}
		if (!categories.remove(category)) {
			throw new UnsupportedOperationException("Category not found in this summary report.");
		}
		deletedCategories.add(category);
	}
	
	public void deleteAllCategories() {
		if (this.categories.isEmpty()) return;
		deletedCategories.addAll(categories);
		categories.clear();
	}
	
	public List<Category> getDeletedCategories(){
		return deletedCategories;
	}
	
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SummaryReport other = (SummaryReport) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
