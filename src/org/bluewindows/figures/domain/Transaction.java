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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class Transaction implements Comparable<Transaction> {

	protected int id;
	protected String number = "";
	protected TransactionDate date = new TransactionDate();
	protected Money amount = new Money();
	protected String description = "";
	protected String memo = "";
	protected String originalDescription = "";
	protected String originalMemo = "";
	protected boolean deductible = false;
	protected boolean userChangedDesc = false;
	protected boolean userChangedMemo = false;
	protected boolean userChangedDeductible = false;
	protected boolean userChangedCategory = false;
	protected Money balance = new Money();
	protected List<TransactionCategory> categories = new ArrayList<TransactionCategory>();
	protected List<TransactionCategory> addedCategories = new ArrayList<TransactionCategory>();
	protected List<TransactionCategory> deletedCategories = new ArrayList<TransactionCategory>();
	protected boolean updated = false;
	protected boolean categoriesUpdated = false;
	
	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number.replaceFirst("^0+(?!$)", ""); //Trim leading zeros
		updated = true;
	}

	public TransactionDate getDate() {
		return date;
	}
	
	public void setDate(TransactionDate date) {
		this.date = date;
		updated = true;
	}

	public void setDate(String dateString) throws ParseException {
		this.date = new TransactionDate(dateString);
		updated = true;
	}

	public Money getAmount() {
		return amount;
	}

	public void setAmount(Money amount) {
		this.amount = amount;
		updated = true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String name) {
		this.description = name;
		updated = true;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
		updated = true;
	}

	public String getOriginalDescription() {
		return originalDescription;
	}
	
	public void setOriginalDescription(String originalDescription) {
		this.originalDescription = originalDescription;
		updated = true;
	}
	
	public String getOriginalMemo() {
		return originalMemo;
	}
	
	public void setOriginalMemo(String originalMemo) {
		this.originalMemo = originalMemo;
		updated = true;
	}

	public List<TransactionCategory> getCategories() {
		return categories;
	}
	
	// This method is used to apply categories to a transaction retrieved from persistence, so 
	// it doesn't set the update flags
	public void initializeCategories(List<TransactionCategory> categories) {
		if (!this.categories.isEmpty()) {
			throw new UnsupportedOperationException("Cannot use this method to update a transaction already containing categories");
		}
		this.categories = categories;
	}
	
	// This method is used by filters to apply a category
	public void setCategory(TransactionCategory category) {
		if (getID() == 14172) {
			System.out.println("Setting category " + category.getCategoryID());
		}

		if (categories.contains(category) || addedCategories.contains(category)) return;
		if (categories.size() > 0) {
			List<TransactionCategory> existingCategories = new ArrayList<TransactionCategory>();
			existingCategories.addAll(getCategories());
			for (TransactionCategory transactionCategory : existingCategories) {
				deleteCategory(transactionCategory);
			}
		}
		if (addedCategories.size() > 0) addedCategories.clear();
		addedCategories.add(category);
		categoriesUpdated = true;
		updated = true;
	}
	
	// This method is used by the UI when a user applies split categories to a transaction
	public void setCategories(List<TransactionCategory> categories) {
		if (categories.size() > 0) {
			List<TransactionCategory> existingCategories = new ArrayList<TransactionCategory>();
			existingCategories.addAll(getCategories());
			for (TransactionCategory transactionCategory : existingCategories) {
				deleteCategory(transactionCategory);
			}
		}
		if (addedCategories.size() > 0) addedCategories.clear();
		addedCategories.addAll(categories);
		categoriesUpdated = true;
		updated = true;
	}
	
	public List<TransactionCategory> getAddedCategories(){
		return addedCategories;
	}
	
	private void deleteCategory(TransactionCategory category) {
		if (this.categories.isEmpty()) {
			throw new UnsupportedOperationException("Category not found in this transaction");
		}
		if (!categories.remove(category)) {
			throw new UnsupportedOperationException("Category not found in this transaction");
		};
		deletedCategories.add(category);
	}
	
	public List<TransactionCategory> getDeletedCategories(){
		return deletedCategories;
	}
	
	public boolean isCategoryUpdated() {
		return categoriesUpdated;
	}

	public boolean isDeductible() {
		return deductible;
	}
	
	public String getDeductible(){
		return deductible? "1": "0";
	}
	
	public void setDeductible(boolean deductible) {
		this.deductible = deductible;
		updated = true;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public boolean isUpdated(){
		return updated;
	}

	public boolean isUserChangedDesc() {
		return userChangedDesc;
	}

	public void setUserChangedDesc(boolean userChangedDesc) {
		this.userChangedDesc = userChangedDesc;
	}

	public boolean isUserChangedMemo() {
		return userChangedMemo;
	}

	public void setUserChangedMemo(boolean userChangedMemo) {
		this.userChangedMemo = userChangedMemo;
	}
	
	public boolean isUserChangedDeductible() {
		return userChangedDeductible;
	}

	public void setUserChangedDeductible(boolean userChangedDeductible) {
		this.userChangedDeductible = userChangedDeductible;
	}
	
	public boolean isUserChangedCategory() {
		return userChangedCategory;
	}

	public void setUserChangedCategory(boolean userChangedCategory) {
		this.userChangedCategory = userChangedCategory;
	}

	public Money getBalance() {
		return balance;
	}

	public void setBalance(Money balance) {
		this.balance = balance;
		updated = true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((memo == null) ? 0 : memo.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
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
		Transaction other = (Transaction) obj;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (memo == null) {
			if (other.memo != null)
				return false;
		} else if (!memo.equals(other.memo))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

//	public String toString(){
//		StringBuffer sb = new StringBuffer();
//		sb.append("ID: [").append(id).append("], ");
//		sb.append("Date: [").append(date).append("], ");
//		sb.append("Amount: [").append(amount).append("], ");
//		sb.append("Description: [").append(description).append("], ");
//		sb.append("Memo: [").append(memo).append("], ");
//		sb.append("Deductible: [").append(deductible).append("], ");
//		sb.append("Balance: [").append(balance).append("], ");
//		return sb.toString();
//	}

	@Override
	public int compareTo(Transaction other) {
		if (this.date.value().compareTo(other.date.value()) !=0) {
			return this.date.value().compareTo(other.date.value());
		}else {
			return Integer.valueOf(this.id).compareTo(Integer.valueOf(other.id));
		}
	}

}
