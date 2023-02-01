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
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.FilterResult;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class FilterSetSettingsPane extends AbstractNewDataPane {
	
	public static double WIDTH = 500;
	public static double HEIGHTH = 300;
	private static final int LABEL_WIDTH = 150;
	private static final int DATA_WIDTH = 300;
	private FilterSet filterSet;
	private TextField filterSetName;
	private ComboBox<String> filterColumnCombo;
	private ComboBox<String> filterExpressionCombo;
	private ComboBox<String> filterResultCombo;
	private boolean dataChanged = false;

	public ScrollPane getFilterSetSettingsPane(FilterSet filterSet) throws Exception {
		initialize();
		this.filterSet = filterSet;
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setPrefWidth(WIDTH);
		scrollPane.setPrefHeight(HEIGHTH);
		scrollPane.setContent(basePane);
		
		HBox nameBar = new HBox();
		dataBox.getChildren().add(nameBar);

		Label filterSetNameLabel = new Label("Filter Set Name:");
		filterSetNameLabel.setMinWidth(100);
		nameBar.getChildren().add(filterSetNameLabel);
		
		filterSetName = new TextField();
		filterSetName.setPrefWidth(200);
		filterSetName.textProperty().addListener((observable, oldValue, newValue) -> {
		    dataChanged = true;
		});
		nameBar.getChildren().add(filterSetName);
		
		HBox filterHeadingBar = new HBox();
		dataBox.getChildren().add(filterHeadingBar);
		Label filterSettingsLabel = new Label("New Filter Defaults");
		filterSettingsLabel.setId("BoldText");
		filterSettingsLabel.setMinWidth(LABEL_WIDTH);
		filterHeadingBar.getChildren().add(filterSettingsLabel);
		Separator filterSettingsSeparator = new Separator();
		filterSettingsSeparator.setPrefWidth(WIDTH - LABEL_WIDTH - 12);
		filterHeadingBar.getChildren().add(filterSettingsSeparator);

		HBox filterColumnBar = new HBox();
		dataBox.getChildren().add(filterColumnBar);
		Label filterColumnLabel = new Label("Search Field:");
		filterColumnLabel.setMinWidth(LABEL_WIDTH);
		filterColumnBar.getChildren().add(filterColumnLabel);
		filterColumnCombo = new ComboBox<String>();
		filterColumnCombo.setPrefWidth(DATA_WIDTH);
		filterColumnBar.getChildren().add(filterColumnCombo);
		filterColumnCombo.setPromptText("Choose a Field");
		for (FilterField fieldType : FilterField.values()) {
			filterColumnCombo.getItems().add(fieldType.toString());
		}
		filterColumnCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
		    dataChanged = true;
		});
		
		HBox filterExpressionBar = new HBox();
		dataBox.getChildren().add(filterExpressionBar);
		Label filterExpressionLabel = new Label("Expression:");
		filterExpressionLabel.setMinWidth(LABEL_WIDTH);
		filterExpressionBar.getChildren().add(filterExpressionLabel);
		filterExpressionCombo = new ComboBox<String>();
		filterExpressionCombo.setPrefWidth(DATA_WIDTH);
		filterExpressionBar.getChildren().add(filterExpressionCombo);
		filterExpressionCombo.setPromptText("Choose an Expression");
		for (FilterExpression expression : FilterExpression.values()) {
			filterExpressionCombo.getItems().add(expression.toString());
		}
		filterExpressionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
		    dataChanged = true;
		});

		HBox filterResultBar = new HBox();
		dataBox.getChildren().add(filterResultBar);
		Label filterResultLabel = new Label("Result:");
		filterResultLabel.setMinWidth(LABEL_WIDTH);
		filterResultBar.getChildren().add(filterResultLabel);
		filterResultCombo = new ComboBox<String>();
		filterResultCombo.setPrefWidth(DATA_WIDTH);
		filterResultBar.getChildren().add(filterResultCombo);
		filterResultCombo.setPromptText("Choose a Result");
		for (FilterResult result : FilterResult.values()) {
			filterResultCombo.getItems().add(result.toString());
		}
		filterResultCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
		    dataChanged = true;
		});
		
		if (filterSet != null) {
			filterSetName.setText(filterSet.getName());
			filterColumnCombo.getSelectionModel().select(filterSet.getDefaultColumn());
			filterExpressionCombo.getSelectionModel().select(filterSet.getDefaultExpression());
			filterResultCombo.getSelectionModel().select(filterSet.getDefaultResult());
			dataChanged = false;
		}else {
			filterColumnCombo.getSelectionModel().select(FilterField.DESCRIPTION.toString());
			filterExpressionCombo.getSelectionModel().select(FilterExpression.CONTAINS.toString());
			filterResultCombo.getSelectionModel().select(FilterResult.REPLACE_DESCRIPTION.toString());
		}
		dataChanged = false;
		Platform.runLater(()->basePane.requestFocus());
		return scrollPane;
	}

	@Override
	protected CallResult validateData() {
		CallResult result = new CallResult();
		if (StringUtils.isEmpty(filterSetName.getText())) {
			Platform.runLater(()->filterSetName.requestFocus());
			result.setCallBad("Filter Set Settings Failure", "You need to set a name for the Filter Set.");
		}
		return result;
	}

	@Override
	protected boolean isDataChanged() {
		return dataChanged;
	}

	@Override
	protected CallResult saveData() {
		CallResult result = new CallResult();
		if (filterSet == null) {
			String name = filterSetName.getText();
			String defaultColumn = filterColumnCombo.getSelectionModel().getSelectedItem();
			String defaultExpression = filterExpressionCombo.getSelectionModel().getSelectedItem();
			String defaultResult = filterResultCombo.getSelectionModel().getSelectedItem();
			filterSet = new FilterSet(0, name, defaultColumn, defaultExpression, defaultResult);
	    	result = ServiceFactory.getInstance().getPersistenceSvc().addFilterSet(filterSet);
		}else {
			filterSet.setName(filterSetName.getText());
			filterSet.setDefaultColumn(filterColumnCombo.getSelectionModel().getSelectedItem());
			filterSet.setDefaultExpression(filterExpressionCombo.getSelectionModel().getSelectedItem());
			filterSet.setDefaultResult(filterResultCombo.getSelectionModel().getSelectedItem());
	    	result = ServiceFactory.getInstance().getPersistenceSvc().updateFilterSet(filterSet);
		}
		return result;
	}
	
}
