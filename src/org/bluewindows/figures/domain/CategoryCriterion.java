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

import java.util.List;

public class CategoryCriterion implements SearchCriterion {

	List<Category> categories;
	
	public CategoryCriterion(List<Category> categories) {
		if (categories == null) {
			throw new IllegalArgumentException("Category list cannot be null.");
		}
		this.categories = categories;
	}
	
	@Override
	public boolean matches(Transaction transaction) {
		for (Category category : categories) {
			for (TransactionCategory transactionCategory : transaction.getCategories()) {
				if (transactionCategory.getCategoryID() == category.getID()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String getLabel() {
		if (categories.size() > 1) {
			return "Categories";
		}else {
			return "Category";
		}
	}
	
	public String getValue() {
		if (categories.size() == 0) {
			return " ";
		}else {
			StringBuilder sb = new StringBuilder(categories.get(0).getName());
			for (int i = 1; i < categories.size(); i++) {
				sb.append(", " + categories.get(i).getName());
			}
			return sb.toString();
		}
	}
}
