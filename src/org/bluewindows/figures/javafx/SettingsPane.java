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

import java.util.TreeSet;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class SettingsPane extends AbstractNewDataPane {
	
	public static double WIDTH = 400;
	public static double HEIGHTH = 300;
	private static final int LABEL_WIDTH = 150;
	private static final int DATA_WIDTH = 200;
	private ComboBox<String> dateCombo;
	private boolean dataChanged;
	
	public Pane getSettingsPane() {
		initialize();
	
		HBox dataHeadingBar = new HBox();
		dataBox.getChildren().add(dataHeadingBar);
		Label dataSettingsLabel = new Label("Data Settings");
		dataSettingsLabel.setId("BoldText");
		dataSettingsLabel.setMinWidth(LABEL_WIDTH);
		dataHeadingBar.getChildren().add(dataSettingsLabel);

		HBox dateBar = new HBox();
		dataBox.getChildren().add(dateBar);
		Label dateLabel = new Label("Date Format:");
		dateLabel.setMinWidth(LABEL_WIDTH);
		dateBar.getChildren().add(dateLabel);
		dateCombo = new ComboBox<String>();
		dateCombo.setPrefWidth(DATA_WIDTH);
		dateBar.getChildren().add(dateCombo);
		TreeSet<String> sortedDateFormats = new TreeSet<String>();
		sortedDateFormats.addAll(Figures.STANDARD_DATE_LIST);
		for (String dateFormat : sortedDateFormats) {
			dateCombo.getItems().add(dateFormat);
		}
		dateCombo.getSelectionModel().select(Figures.getProperty(Figures.DATE_FORMAT_NAME));
		dateCombo.setOnAction(getDateComboHandler());

		dateCombo.getSelectionModel().select(Figures.getProperty(Figures.DATE_FORMAT_NAME));
		dateCombo.setOnAction(getDateComboHandler());
		
		return basePane;
	}
	
	private EventHandler<ActionEvent> getDateComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dateCombo.getStyleClass().add("selected-bold");
				dataChanged = true;
			}
		};
	}
	
	@Override
	protected CallResult validateData() {
		CallResult result = new CallResult();
		return result;
	}

	@Override
	protected boolean isDataChanged() {
		return dataChanged;
	}

	@Override
	protected CallResult saveData() {
		CallResult result = new CallResult();
		Figures.setProperty(Figures.DATE_FORMAT_NAME, dateCombo.getSelectionModel().getSelectedItem());
		ServiceFactory.getInstance().getDisplaySvc().displayInformationMessage(result.getMessageDecorator(), "You must restart Figures to see the changes you have made.");
		return result;
	}

}
