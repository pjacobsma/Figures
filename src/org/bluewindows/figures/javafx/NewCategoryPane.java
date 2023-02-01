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

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class NewCategoryPane extends AbstractNewDataPane {
	
	private TextField categoryName;
	
	public Pane getNewCategoryPane() {
		initialize();
		
		HBox nameBar = new HBox();
		dataBox.getChildren().add(nameBar);

		Label categoryLabel = new Label("Category Name:");
		categoryLabel.setMinWidth(100);
		nameBar.getChildren().add(categoryLabel);
		
		categoryName = new TextField();
		categoryName.setPrefWidth(200);
		nameBar.getChildren().add(categoryName);

		return basePane;
	}

	public String getNewCategoryName() {
		return categoryName.getText();
	}
	
	@Override
	protected CallResult validateData() {
		CallResult result = new CallResult();
		if (isDataChanged()) {
			if (StringUtils.isEmpty(categoryName.getText())) {
				return result.setCallBad("Missing Category Name", "You need to specify a category name.");
			}
		}
		return result;
	}
	
	@Override
	protected boolean isDataChanged() {
		return !StringUtils.isEmpty(categoryName.getText());
	}

	@Override
	protected CallResult saveData() {
    	CallResult result = ServiceFactory.getInstance().getPersistenceSvc().addCategory(categoryName.getText());
		if (result.isBad()) {
			if (result.getErrorMessage() != null) {
				if (result.getErrorMessage().contains("UNIQUE constraint")) {
					ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Duplicate Category", 
						"That is a duplicate of an existing category.");
				}else {
					ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage(result.getMessageDecorator(), result.getErrorMessage());
				}
			}
		}
    	return result;
	}

}
