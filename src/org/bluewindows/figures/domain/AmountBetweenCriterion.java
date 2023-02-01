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

public class AmountBetweenCriterion implements SearchCriterion {

	private Money lowAmount;
	private Money highAmount;
	
	public AmountBetweenCriterion(Money lowAmount, Money highAmount) {
		if (lowAmount == null) throw new IllegalArgumentException("Low amount cannot be null.");
		if (highAmount == null) throw new IllegalArgumentException("High amount cannot be null.");
		this.lowAmount = lowAmount;
		this.highAmount = highAmount;
	}
	
	@Override
	public boolean matches(Transaction transaction) {
		if (transaction.getAmount() == null) return false;
		if (transaction.getAmount().getValue().abs().compareTo(lowAmount.getValue()) < 0) return false;
		if (transaction.getAmount().getValue().abs().compareTo(highAmount.getValue()) > 0) return false;
		return true;
	}
	
	public String getLabel() {
		return "Amount Between";
	}

	public String getValue() {
		return lowAmount.toStringNumber() + " and " + highAmount.toStringNumber();
	}

}
