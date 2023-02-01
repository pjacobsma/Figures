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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Summary {

	// The summaryKey string contains a list of the grouping field string values for this summary, e.g. Year, Month, etc
	private List<String> summaryKeys = new ArrayList<String>();
	private long depositsCount = 0;
	private BigDecimal depositsAmount = BigDecimal.ZERO;
	private long withdrawalsCount = 0;
	private BigDecimal withdrawalsAmount = BigDecimal.ZERO;

	public List<String> getSummaryKeys() {
		return summaryKeys;
	}
	
	public void addKey(String summaryKey) {
		summaryKeys.add(summaryKey);
	}
	
	public String getKey1() {
		return summaryKeys.get(0);
	}
	
	public String getKey2() {
		return summaryKeys.get(1);
	}
	
	public String getKey3() {
		return summaryKeys.get(2);
	}
	
	public String getKey4() {
		return summaryKeys.get(3);
	}
	
	public String getKey5() {
		return summaryKeys.get(4);
	}
	
	public Long getDepositsCount() {
		return Long.valueOf(depositsCount);
	}
	
	public void setDepositsCount(int depositsCount) {
		this.depositsCount = Integer.valueOf(depositsCount);
	}
	
	public BigDecimal getDepositsAmount() {
		return depositsAmount;
	}
	
	public void setDepositsAmount(BigDecimal depositsAmount) {
		this.depositsAmount = depositsAmount;
	}
	
	public Long getWithdrawalsCount() {
		return Long.valueOf(withdrawalsCount);
	}
	
	public void setWithdrawalsCount(int withdrawalsCount) {
		this.withdrawalsCount = Integer.valueOf(withdrawalsCount);
	}
	
	public BigDecimal getWithdrawalsAmount() {
		return withdrawalsAmount;
	}
	
	public void setWithdrawalsAmount(BigDecimal withdrawalsAmount) {
		this.withdrawalsAmount = withdrawalsAmount.abs();
	}
	
	public void add(Summary summaryToAdd) {
		withdrawalsAmount.add(summaryToAdd.withdrawalsAmount);
		withdrawalsCount += summaryToAdd.withdrawalsCount;
		depositsAmount.add(summaryToAdd.depositsAmount);
		depositsCount += summaryToAdd.depositsCount;
	}
	
}
