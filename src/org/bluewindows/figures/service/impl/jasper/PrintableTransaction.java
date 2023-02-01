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

import java.math.BigDecimal;

import org.bluewindows.figures.domain.Transaction;

public class PrintableTransaction {
	
	private Transaction transaction;
	
	public PrintableTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public String getNumber() {
		return transaction.getNumber();
	}
	
	public String getDate() {
		return transaction.getDate().toString();
	}

	public BigDecimal getAmount() {
		return transaction.getAmount().getValue();
	}
	
	public String getDescription() {
		return transaction.getDescription();
	}
	
	public String getMemo() {
		return transaction.getMemo();
	}
	
	public String getCategoryList() {
		if (transaction.getCategories().size() == 0) {
			return "";
		}
		if (transaction.getCategories().size() == 1) {
			return transaction.getCategories().get(0).getName();
		}else {
			StringBuilder sb = new StringBuilder(transaction.getCategories().get(0).getName() + ": " + transaction.getCategories().get(0).getAmount().toString());
			for (int i = 1; i < transaction.getCategories().size(); i++) {
				sb.append("\n" + transaction.getCategories().get(i).getName() + ": " + transaction.getCategories().get(i).getAmount().toString());
			}
			return sb.toString();
		}
	}
	
	public String getDeductible() {
		return transaction.isDeductible()? "Yes":"No";
	}
	
	public BigDecimal getBalance() {
		return transaction.getBalance().getValue();
	}

}
