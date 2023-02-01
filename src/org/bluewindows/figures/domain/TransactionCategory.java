/**
 * Copyright 2010 Phil Jacobsma
 * 
 * This file is part of MoneyLab.
 *
 * MoneyLab is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MoneyLab is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoneyLab; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.bluewindows.figures.domain;

public class TransactionCategory {
	
	private int transactionCategoryID;
	private int categoryID;
	private String name;
	private Money amount = new Money();
	
	public TransactionCategory(int transactionCategoryID, int categoryID, String name, Money amount) {
		this.transactionCategoryID = transactionCategoryID;
		this.categoryID = categoryID;
		this.name = name;
		this.amount = amount;
	}
	
	public TransactionCategory(int categoryID, Money amount) {
		this.categoryID = categoryID;
		this.amount = amount;
	}
	
	public int getTransactionCategoryID() {
		return transactionCategoryID;
	}

	public void setTransactionCategoryID(int transactionCategoryID) {
		this.transactionCategoryID = transactionCategoryID;
	}
		
	public int getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}

	public String getName() {
		if (name == null) return "";
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Money getAmount() {
		return amount;
	}
	
	public void setAmount(Money amount) {
		this.amount = amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + categoryID;
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
		TransactionCategory other = (TransactionCategory) obj;
		if (categoryID != other.categoryID) {
			return false;
		}
		if (!amount.equals(other.amount)) {
			return false;
		}
		return true;
	}


}
