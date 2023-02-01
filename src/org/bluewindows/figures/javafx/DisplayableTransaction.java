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
package org.bluewindows.figures.javafx;

import org.bluewindows.figures.domain.Transaction;

//This is a Transaction class decorated for use in the JavaFX tableview
public class DisplayableTransaction extends Transaction {
	
	private SortableTransactionDate sortableDate;
	
	public DisplayableTransaction(Transaction transaction) {
		this.id = transaction.getID();
		this.number = transaction.getNumber();
		this.date = transaction.getDate();
		sortableDate = new SortableTransactionDate(transaction.getDate(), transaction.getID());
		this.amount = transaction.getAmount();
		this.description = transaction.getDescription();
		this.memo = transaction.getMemo();
		this.originalDescription = transaction.getOriginalDescription();
		this.originalMemo = transaction.getOriginalMemo();
		this.categories = transaction.getCategories();
		this.deductible = transaction.isDeductible();
		this.userChangedDesc = transaction.isUserChangedDesc();
		this.userChangedMemo = transaction.isUserChangedMemo();
		this.userChangedDeductible = transaction.isUserChangedDeductible();
		this.userChangedCategory = transaction.isUserChangedCategory();
		this.balance = transaction.getBalance();
	}

	public String getCategoryList() {
		if (categories.size() == 0) {
			return "None";
		}
		if (categories.size() == 1) {
			return categories.get(0).getName();
		}else{
			StringBuilder sb = new StringBuilder(categories.get(0).getName() + ": " + categories.get(0).getAmount().toString());
			for (int i = 1; i < categories.size(); i++) {
				sb.append("\n" + categories.get(i).getName() + ": " + categories.get(i).getAmount().toString());
			}
			return sb.toString();
		}
	}
	
	public String getDeductibleLabel() {
		return this.isDeductible()? "Yes":"No";
	}
	
	public SortableTransactionDate getSortableDate() {
		return sortableDate;
	}
	
}
