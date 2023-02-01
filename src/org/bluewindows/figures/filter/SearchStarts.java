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

package org.bluewindows.figures.filter;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.domain.Transaction;


public class SearchStarts implements SearchInterface {

	public boolean found(Transaction transaction, SourceFieldInterface sourceField, String search)  {
		return StringUtils.startsWith(sourceField.getSource(transaction), search) &&
			sourceField.getSource(transaction).length() >= search.length();
	}
}
