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

import org.bluewindows.figures.domain.TransactionDate;

// This date object includes the transaction id so when the user sorts the transaction table
// by date the original order of the transactions within date is preserved
public class SortableTransactionDate implements Comparable<SortableTransactionDate> {

	private TransactionDate date;
	private Integer id;
	
	public SortableTransactionDate(TransactionDate date, int id) {
		this.date = date;
		this.id = id;
	}

	@Override
	public int compareTo(SortableTransactionDate other) {
		if (this.date.compareTo(other.date) !=0) {
			return this.date.compareTo(other.date);
		}else {
			return this.id.compareTo(other.id);
		}
	}
	
	public String toString() {
		return date.toString();
	}

}
