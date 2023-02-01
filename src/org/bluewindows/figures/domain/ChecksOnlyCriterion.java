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

import org.apache.commons.lang3.StringUtils;

public class ChecksOnlyCriterion implements SearchCriterion {

	public ChecksOnlyCriterion() {
	}
	
	@Override
	public boolean matches(Transaction transaction) {
		return !StringUtils.isEmpty(transaction.getNumber().trim());
	}

	public String getLabel() {
		return "Checks Only";
	}

	public String getValue() {
		return " ";
	}
}
