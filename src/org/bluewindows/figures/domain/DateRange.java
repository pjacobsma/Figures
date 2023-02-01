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

public class DateRange {
	
	private TransactionDate startDate = null;
	private TransactionDate endDate = null;
	
	public DateRange() {
		
	}
	
	public DateRange(TransactionDate startDate, TransactionDate endDate) {
		if (startDate == null) {
			throw new IllegalArgumentException("Start date cannot be null.");
		}
		if (endDate == null) {
			throw new IllegalArgumentException("End date cannot be null.");
		}
		if (endDate.compareTo(startDate) < 0) {
			throw new IllegalArgumentException("End date cannot be before start date.");
		}
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public TransactionDate getStartDate() {
		return startDate;
	}
	
	public void setStartDate(TransactionDate startDate) {
		this.startDate = startDate;
	}
	
	public TransactionDate getEndDate() {
		return endDate;
	}
	
	public void setEndDate(TransactionDate endDate) {
		this.endDate = endDate;
	}
	
	public boolean contains(TransactionDate date) {
		return date.isBetween(startDate, endDate);
	}
	
	public boolean isAfter(DateRange otherDateRange) {
		if (startDate.compareTo(otherDateRange.getEndDate()) > 0) {
			return true;
		}
		return false;
	}
	
}
