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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;

public class Money implements Comparable<Money>, Serializable {
	//Wrapper for BigDecimal to store dollar amounts
	private static final long serialVersionUID = 285893844859428437L;
	
	public static final Money ZERO = new Money("0.00");
	protected BigDecimal value;
	private static NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance();
	static {
		MONEY_FORMAT.setMaximumFractionDigits(2);
		MONEY_FORMAT.setMinimumFractionDigits(2);
		MONEY_FORMAT.setGroupingUsed(false);
	}
	
	public Money(){
		value = null;
	}
	
	public Money(long longValue) {
		this.value = validateValue(longValue);
	}
	
	public Money(double doubleValue) {
		this.value = validateValue(doubleValue);
	}
	
	public Money(String moneyString) {
	    if (moneyString == null || moneyString.isEmpty() ) {
	        throw new IllegalArgumentException("Money value empty or null.");
	    }
		this.value = validateValue(moneyString);
	}
	
	public Money(BigDecimal moneyValue) {
	    if (moneyValue == null) {
	        throw new IllegalArgumentException("Money value empty or null.");
	    }
		this.value = validateValue(moneyValue);
	}
	
	public Money(Money moneyValue) {
		this.value = moneyValue.getValue();
	}
	
	private BigDecimal validateValue(String moneyString){
		moneyString = moneyString.trim();
		moneyString = StringUtils.replace(moneyString, " ", "");
		moneyString = StringUtils.remove(moneyString, Figures.currencySymbol);
		moneyString = StringUtils.remove(moneyString, ",");
		BigDecimal moneyAmount = new BigDecimal(moneyString);
	    return validateValue(moneyAmount);
	}
	
	private BigDecimal validateValue(BigDecimal moneyValue) {
	    if (moneyValue.scale() > 2 ) {
	        throw new IllegalArgumentException("Money value with more than 2 decimal places.");
	    }else if (moneyValue.scale() < 2) {
	    	moneyValue = moneyValue.setScale(2, RoundingMode.UNNECESSARY);
	    }
		return moneyValue;
	}
	
	private BigDecimal validateValue(long longValue) {
		BigDecimal moneyValue = BigDecimal.valueOf(longValue);
	    if (moneyValue.scale() > 2 ) {
	        throw new IllegalArgumentException("Money value with more than 2 decimal places.");
	    }else if (moneyValue.scale() < 2) {
	    	moneyValue = moneyValue.setScale(2, RoundingMode.UNNECESSARY);
	    }
		return moneyValue;
	}
	
	private BigDecimal validateValue(double doubleValue) {
		BigDecimal moneyValue = BigDecimal.valueOf(doubleValue);
	    if (moneyValue.scale() > 2 ) {
	        throw new IllegalArgumentException("Money value with more than 2 decimal places.");
	    }else if (moneyValue.scale() < 2) {
	    	moneyValue = moneyValue.setScale(2, RoundingMode.UNNECESSARY);
	    }
		return moneyValue;
	}
	
	public BigDecimal getValue() {
		if (value == null){
			throw new IllegalStateException("Attempt to use uninitialized Money value.");
		}
		return value;
	}
	
	public void add(Money additionalAmount) {
		value = value.add(additionalAmount.getValue());
	}
	
	public void subtract(Money additionalAmount) {
		value = value.subtract(additionalAmount.getValue());
	}
	
	public String toString() {
		if (value == null) return "";
		return StringUtils.remove(Figures.currencyFormat.format(value), "-");
	}
	
	public String toStringNegative() {
		if (value == null) return "";
		return Figures.currencyFormat.format(value);
	}

	public String toStringNumber() {
		return MONEY_FORMAT.format(getValue());
	}
	
	public String toStringNumberPositive() {
		return StringUtils.remove(MONEY_FORMAT.format(getValue()), "-");
	}

	@Override
	public int compareTo(Money other) {
		return this.getValue().compareTo(other.getValue());
	}
	
	public boolean isMissing(){
		return (value == null);
	}

	public boolean isNotMissing(){
		return (value != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Money other = (Money) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		}
		return value.equals(other.value);
	}

	public boolean isCredit() {
		return value.compareTo(BigDecimal.ZERO) == 1;
	}

}
