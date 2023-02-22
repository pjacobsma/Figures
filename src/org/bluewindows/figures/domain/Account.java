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

import org.bluewindows.figures.enums.AccountType;

public class Account {
	private int id;
	private String name = "";
	private AccountType type;
	private FilterSet filterSet;
	private Money initialBalance = new Money("0.00");
	private TransactionDate lastLoadedDate;
	private TransactionDate lastFilteredDate;
	private String importFolder = "";
	private List<Transaction> tranList = new ArrayList<Transaction>();

	public Account(int id, String name, AccountType type, FilterSet filterSet) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.filterSet = filterSet;
	}

	public Account(int id, String name, AccountType type, FilterSet filterSet, Money initialBalance, 
			TransactionDate lastLoadedDate, TransactionDate lastFilteredDate){
		this.id = id;
		this.name = name;
		this.type = type;
		this.filterSet = filterSet;
		this.initialBalance = initialBalance;
		this.lastLoadedDate = lastLoadedDate;
		this.setLastFilteredDate(lastFilteredDate);
	}
	
	public Account(int id) {
		this.id = id;
	}
	
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

	public AccountType getType() {
		return type;
	}

	public void setType(AccountType type) {
		this.type = type;
	}

	public FilterSet getFilterSet() {
		return filterSet;
	}

	public void setFilterSet(FilterSet filterSet) {
		this.filterSet = filterSet;
	}

	public Money getInitialBalance() {
		return initialBalance;
	}

	public void setInitialBalance(Money initialBalance) {
		this.initialBalance = initialBalance;
	}

	public TransactionDate getLastLoadedDate() {
		return lastLoadedDate;
	}

	public void setLastLoadDate(TransactionDate lastLoadDate) {
		this.lastLoadedDate = lastLoadDate;
	}
	
	public TransactionDate getLastFilteredDate() {
		return lastFilteredDate;
	}

	public void setLastFilteredDate(TransactionDate lastFilteredDate) {
		this.lastFilteredDate = lastFilteredDate;
	}
	
	public String getImportFolder() {
		return importFolder;
	}

	public void setImportFolder(String importFolder) {
		this.importFolder = importFolder;
	}

	public List<Transaction> getTransactions() {
		return tranList;
	}
	
	public void setTransactions(List<Transaction> tranList) {
		this.tranList = tranList;
	}
	
	public void addTransactions(List<Transaction> tranList) {
		this.tranList.addAll(tranList);
	}
	
	public void addTransaction(Transaction transaction){
		tranList.add(transaction);
	}

	public boolean deleteTransaction(Transaction transaction) {
		return tranList.remove(transaction);
	}

	public List<Transaction> getUpdatedTransactions() {
		List<Transaction> updatedList = new ArrayList<Transaction>();
		for (Transaction transaction : tranList) {
			if (transaction.isUpdated()) updatedList.add(transaction);
		}
		return updatedList;
	}

	public int getTransactionCount() {
		return tranList.size();
	}

	public Account clone() {
		Account clonedAccount = new Account(this.getID(), this.getName(), this.getType(), this.getFilterSet());
		return clonedAccount;
	}
	
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Account other = (Account) obj;
		if (id != other.id)
			return false;
		return true;
	}


}
