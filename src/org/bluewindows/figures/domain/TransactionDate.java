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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.bluewindows.figures.app.Figures;

public class TransactionDate implements Comparable<TransactionDate>{

	public static final TransactionDate MINIMUM_DATE = new TransactionDate();
	private LocalDate date;
	
	public TransactionDate() {
		date = LocalDate.of(1969, 12, 31);
	}
	
	public TransactionDate(LocalDate localDate) {
		if (localDate == null) throw new IllegalArgumentException("LocalDate cannot be null.");
		this.date = localDate;
	}

	public TransactionDate(String dateString) throws ParseException {
		if (dateString == null || dateString.isEmpty())	throw new IllegalArgumentException("dateString cannot be null.");;
		date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
	}
	
	@Override
	public String toString(){
		return date.format(Figures.dateFormat);
	}
	
	public LocalDate value() {
		return date;
	}
	
	public boolean isBetween(TransactionDate startDate, TransactionDate endDate){
		return this.value().compareTo(startDate.value()) >= 0 && this.value().compareTo(endDate.value()) <= 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
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
		TransactionDate other = (TransactionDate) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}

	@Override
	public int compareTo(TransactionDate other) {
		return this.value().compareTo(other.value());
	}

}
