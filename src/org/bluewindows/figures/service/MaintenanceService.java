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

package org.bluewindows.figures.service;

import java.io.File;
import java.util.List;

import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;

public interface MaintenanceService {

	public CallResult importFile(File file, Account account);
	public int filterTransactions(List<Transaction> transactions, Account account);
	public CallResult filterTransaction(Transaction transaction, Account account);
	public CallResult filterAndSaveTransactions(List<Transaction> transactions, Account account);
	public CallResult filterAndUpdateTransactions(Account account);
	public CallResult resequenceFilters(FilterSet filterSet, int updatedFilterID, int newSequence);
	public Money applyInitialBalance(List<Transaction> transactions, Money balance);
	public Money applyCurrentBalance(Account account, Money balance);
	public CallResult deleteAccount(Account account);

}
