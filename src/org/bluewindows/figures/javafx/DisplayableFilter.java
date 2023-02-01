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

import java.util.List;

import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.filter.Filter;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import javafx.scene.image.Image;

//This is a Filter class decorated for use in the JavaFX table
public class DisplayableFilter extends Filter {
	
	private static List<Category> categories;
	
	public DisplayableFilter(int id, int filterSetID, int sequence, String fieldType, String expression, String searchValue,
			String resultAction, String replacementValue, boolean deductible, int categoryID) {
		super(id, filterSetID, sequence, fieldType, expression, searchValue, resultAction, replacementValue,  deductible, categoryID);
	}
	
	public DisplayableFilter(Filter filter) {
		super(filter.getID(), filter.getFilterSetID(), filter.getSequence(), filter.getField(), filter.getExpression(), filter.getSearchValue(), 
				filter.getResultAction(), filter.getReplacementValue(), filter.isDeductible()?true:false, filter.getCategoryID());
	}

	public String getCategory() {
		if (this.getCategoryID() != null) {
			for (Category category : categories) {
				if (category.getID() == this.getCategoryID()) {
					return category.getName();
				}
			}
		}
		return "";
	}
	
	public void setCategories(List<Category> categories) {
		DisplayableFilter.categories = categories;
	}
	
	public Image getEditIcon() {
		return DisplayServiceImplJavaFX.editIcon;
	}
	
	public Image getDeleteIcon() {
		return DisplayServiceImplJavaFX.deleteIcon;
	}

	@Override
	public boolean execute(Transaction trans) {
		return false;
	}

}

