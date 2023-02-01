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

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.enums.FilterExpression;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.FilterResult;
import org.bluewindows.figures.filter.Filter;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class FilterSettingsPane extends AbstractNewDataPane {
	
	public static double WIDTH = 600;
	public static double HEIGHT = 400;
	
	private Filter filter;
	private FilterSet filterSet;
	private int filterID = 0;
	private Integer sequence;
	private String searchField;
	private String expression;
	private String searchValue;
	private ContextMenu searchValuecontextMenu;
	private String result;
	protected String replacementValue;
	private int categoryID = 0;
	private Boolean deductible = Boolean.FALSE;
	private static final int LABEL_WIDTH = 135;
	private static final int DATA_WIDTH = 400;
	private ComboBox<FilterField> sourceFieldTypeCombo;
	private ComboBox<FilterExpression> expressionCombo;
	private TextField searchValueTextField;
	private ComboBox<FilterResult> resultActionCombo;
	private boolean settingResultActionInCode = false;
	private HBox replacementValueBar;
	private TextField replacementValueTextField;
	private ComboBox<Category> categoryCombo;
	private CheckBox deductibleCheckBox;
	private boolean dataChanged = false;

	public FilterSettingsPane(FilterSet filterSet, Filter filter) {
		if (filterSet == null) throw new IllegalArgumentException("FilterSet cannot be null.");
		if (filter == null) throw new IllegalArgumentException("Filter cannot be null.");
		this.filterSet = filterSet;
		filterID = filter.getID();
		sequence = filter.getSequence();
		searchField = filter.getField();
		expression = filter.getExpression();
		searchValue = filter.getSearchValue();
		result = filter.getResultAction();
		replacementValue = filter.getReplacementValue();
		categoryID = filter.getCategoryID();
		deductible = filter.isDeductible();
	}
	
	public FilterSettingsPane(FilterSet filterSet, Integer sequenceNumber, String sourceFieldType, String searchValue) {
		if (filterSet== null) throw new IllegalArgumentException("FilterSet cannot be null.");
		if (sequenceNumber == null) throw new IllegalArgumentException("Sequence number cannot be null.");
		this.filterSet = filterSet;
		this.sequence = sequenceNumber;
		this.searchField = sourceFieldType;
		this.searchValue = searchValue;
	}


	public FilterSettingsPane(FilterSet filterSet, Integer sequenceNumber) {
		if (filterSet == null) throw new IllegalArgumentException("FilterSet cannot be null.");
		if (sequenceNumber == null) throw new IllegalArgumentException("Sequence number cannot be null.");
		this.filterSet = filterSet;
		this.sequence = sequenceNumber;
		this.searchField = filterSet.getDefaultColumn();
		this.expression = filterSet.getDefaultExpression();
		this.result = filterSet.getDefaultResult();
	}
	
	public Pane getNewFilterPane() {
		initialize();
		basePane.setPadding(new Insets(0, 0, 0, 0));
		basePane.setPrefWidth(WIDTH);
		basePane.setPrefHeight(HEIGHT);
		
		HBox sourceFieldTypeBar = new HBox();
		dataBox.getChildren().add(sourceFieldTypeBar);
		Label sourceFieldTypeLabel = new Label("Search Field:");
		sourceFieldTypeLabel.setMinWidth(LABEL_WIDTH);
		sourceFieldTypeBar.getChildren().add(sourceFieldTypeLabel);
		sourceFieldTypeCombo = new ComboBox<FilterField>();
		sourceFieldTypeCombo.setMinWidth(DATA_WIDTH);
		sourceFieldTypeCombo.setPromptText("Choose a Column");
		for (FilterField fieldType : FilterField.values()) {
			sourceFieldTypeCombo.getItems().add(fieldType);
		}
		if (searchField != null) {
			sourceFieldTypeCombo.getSelectionModel().select(FilterField.findByText(searchField));
		}else {
			sourceFieldTypeCombo.getSelectionModel().select(FilterField.findByText(filterSet.getDefaultColumn()));
			searchField = filterSet.getDefaultColumn();
		}
		sourceFieldTypeCombo.setOnAction(getSourceFieldTypeComboHandler());
		sourceFieldTypeBar.getChildren().add(sourceFieldTypeCombo);
		
		HBox expressionBar = new HBox();
		dataBox.getChildren().add(expressionBar);
		Label expressionLabel = new Label("Expression:");
		expressionLabel.setMinWidth(LABEL_WIDTH);
		expressionBar.getChildren().add(expressionLabel);
		expressionCombo = new ComboBox<FilterExpression>();
		expressionCombo.setMinWidth(DATA_WIDTH);
		expressionCombo.setPromptText("Choose a Search Type");
		for (FilterExpression filterExpression : FilterExpression.values()) {
			expressionCombo.getItems().add(filterExpression);
		}
		if (expression != null) {
			expressionCombo.getSelectionModel().select(FilterExpression.findByText(expression));
		}else {
			expressionCombo.getSelectionModel().select(FilterExpression.findByText(filterSet.getDefaultExpression()));
			expression = filterSet.getDefaultExpression();
		}
		expressionCombo.setOnAction(getExpressionComboHandler()); 
		expressionBar.getChildren().add(expressionCombo);
		
		HBox searchValueBar = new HBox();
		dataBox.getChildren().add(searchValueBar);
		Label searchValueLabel = new Label("Search Value:");
		searchValueLabel.setMinWidth(LABEL_WIDTH);
		searchValueBar.getChildren().add(searchValueLabel);
		searchValueTextField = new TextField(); 
		searchValueTextField.setPrefWidth(DATA_WIDTH);
		searchValueTextField.setAlignment(Pos.BASELINE_LEFT);
		if (searchValue != null) {
			searchValueTextField.setText(searchValue);
		}
		searchValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			searchValueTextField.setStyle("-fx-font-weight: bold");
			searchValue = newValue;
			dataChanged = true;
		});
		searchValueBar.getChildren().add(searchValueTextField);
		
		HBox resultActionBar = new HBox();
		dataBox.getChildren().add(resultActionBar);
		Label resultActionLabel = new Label("Result:");
		resultActionLabel.setMinWidth(LABEL_WIDTH);
		resultActionBar.getChildren().add(resultActionLabel);
		resultActionCombo = new ComboBox<FilterResult>();
		resultActionCombo.setMinWidth(DATA_WIDTH);
		resultActionCombo.setPromptText("Choose a Result Action");
		loadResultActionCombo();
		if (result != null) {
			resultActionCombo.getSelectionModel().select(FilterResult.findByText(result));
		}else {
			resultActionCombo.getSelectionModel().select(FilterResult.findByText(filterSet.getDefaultResult()));
			result = filterSet.getDefaultResult();
		}
		setSearchValueContextMenu();
		resultActionCombo.setOnAction(getResultActionComboHandler());
		resultActionBar.getChildren().add(resultActionCombo);
		
		replacementValueBar = new HBox();
		dataBox.getChildren().add(replacementValueBar);
		Label replacementValueLabel = new Label("Replacement Value:");
		replacementValueLabel.setMinWidth(LABEL_WIDTH);
		replacementValueBar.getChildren().add(replacementValueLabel);
		replacementValueTextField = new TextField(); 
		replacementValueTextField.setPrefWidth(DATA_WIDTH);
		replacementValueTextField.setAlignment(Pos.BASELINE_LEFT);
		if (replacementValue != null) {
			replacementValueTextField.setText(replacementValue);
		}
		replacementValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			replacementValueTextField.setStyle("-fx-font-weight: bold");
			replacementValue = newValue; 
			dataChanged = true;
		});
		replacementValueBar.getChildren().add(replacementValueTextField);
		
		HBox categoryBar = new HBox();
		dataBox.getChildren().add(categoryBar);
		Label categoryLabel = new Label("Category:");
		categoryLabel.setMinWidth(LABEL_WIDTH);
		categoryBar.getChildren().add(categoryLabel);
		categoryCombo = new ComboBox<Category>();
		categoryCombo.setMinWidth(DATA_WIDTH);
		categoryCombo.setPromptText("Choose a Category");
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Categories", result.getErrorMessage());
			return null;
		}
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>)result.getReturnedObject();
		for (Category category : categories) {
			categoryCombo.getItems().add(category);
		}
		if (categoryID != 0) {
			for (Category category : categories) {
				if (category.getID() == categoryID) {
					categoryCombo.getSelectionModel().select(category);
				}
			}
		}
		categoryCombo.setOnAction(getCategoryComboHandler());
		categoryBar.getChildren().add(categoryCombo);
		
		HBox deductibleBar = new HBox();
		Region deductibleSpacer = new Region();
		deductibleSpacer.setMinWidth(LABEL_WIDTH);
		deductibleBar.getChildren().add(deductibleSpacer);
		deductibleCheckBox = new CheckBox();
		deductibleBar.getChildren().add(deductibleCheckBox);
		deductibleCheckBox.setText("Deductible");
		if (deductible != null) {
			deductibleCheckBox.setSelected(deductible);
		}
		deductibleCheckBox.setOnAction(getDeductibleCheckboxHandler());
		dataBox.getChildren().add(deductibleBar);

		setReplacementVisibility();
		dataChanged = false;
		return basePane;
	}

	private void loadResultActionCombo() {
		settingResultActionInCode = true;
		FilterResult selectedResult = resultActionCombo.getSelectionModel().getSelectedItem();
		resultActionCombo.getItems().clear();
		resultActionCombo.getItems().addAll(FilterResult.getAppropriateValues(sourceFieldTypeCombo.getSelectionModel().getSelectedItem()));
		if (selectedResult != null && resultActionCombo.getItems().contains(selectedResult)){
			resultActionCombo.getSelectionModel().select(selectedResult);
		}
		settingResultActionInCode = false;
	}
		
	private EventHandler<ActionEvent> getSourceFieldTypeComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sourceFieldTypeCombo.getStyleClass().add("selected-bold");
				searchField = sourceFieldTypeCombo.getSelectionModel().getSelectedItem().toString();
				loadResultActionCombo();
				dataChanged = true;
			}
		};
	}
	
	private EventHandler<ActionEvent> getExpressionComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setReplacementVisibility();
				expressionCombo.getStyleClass().add("selected-bold");
				expression = expressionCombo.getSelectionModel().getSelectedItem().toString();
				dataChanged = true;
			}
		};
	}
	
	private EventHandler<ActionEvent> getResultActionComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!settingResultActionInCode) {
					resultActionCombo.getStyleClass().add("selected-bold");
					result = resultActionCombo.getSelectionModel().getSelectedItem().toString();
					dataChanged = true;
				}
				setReplacementVisibility();
				setSearchValueContextMenu();
			}
		};
	}
	
	private void setReplacementVisibility() {
		if (resultActionCombo.getSelectionModel().getSelectedItem() == null) {
			replacementValueBar.setVisible(false);
			replacementValueBar.setManaged(false);
		}else if (resultActionCombo.getSelectionModel().getSelectedItem().toString().contains("Replace")) {
			replacementValueBar.setVisible(true);
			replacementValueBar.setManaged(true);
		}else {
			replacementValueBar.setVisible(false);
			replacementValueBar.setManaged(false);
		}
	}
	
	private void setSearchValueContextMenu() {
		searchValuecontextMenu = new ContextMenu();
		if (resultActionCombo.getSelectionModel().getSelectedItem().toString().contains("Replace")) {
	        MenuItem menuTrimAndCopy = new MenuItem("Trim and Copy To Replacement Value");
	        menuTrimAndCopy.setOnAction((event) -> {
	        	if (!searchValueTextField.getSelectedText().isEmpty()) {
	        		replacementValueTextField.setText(searchValueTextField.getSelectedText());
	        		replacementValue = searchValueTextField.getSelectedText();
	        		searchValue = searchValueTextField.getSelectedText();
	        		searchValueTextField.setText(searchValueTextField.getSelectedText());
	        	}
	        });
	        searchValuecontextMenu.getItems().add(menuTrimAndCopy);
		}
        MenuItem menuTrim = new MenuItem("Trim");
        menuTrim.setOnAction((event) -> {
        	if (!searchValueTextField.getSelectedText().isEmpty()) {
        		searchValue = searchValueTextField.getSelectedText();
        		searchValueTextField.setText(searchValueTextField.getSelectedText());
        	}
        });
        searchValuecontextMenu.getItems().add(menuTrim);
        MenuItem menuCopy = new MenuItem("Copy");
        menuCopy.setOnAction((event) -> {
        	ClipboardContent content = new ClipboardContent();
        	content.putString(searchValueTextField.getSelectedText());
        	Clipboard.getSystemClipboard().setContent(content);
        });
        searchValuecontextMenu.getItems().add(menuCopy);
        MenuItem menuCut = new MenuItem("Cut");
        menuCut.setOnAction((event) -> {
        	if (!searchValueTextField.getSelectedText().isEmpty()) {
	        	ClipboardContent content = new ClipboardContent();
	        	content.putString(searchValueTextField.getSelectedText());
	        	Clipboard.getSystemClipboard().setContent(content);
	        	IndexRange range = searchValueTextField.getSelection();
	        	String origText = searchValueTextField.getText();
	        	String firstPart = StringUtils.substring(origText, 0, range.getStart() );
	        	String lastPart = StringUtils.substring(origText, range.getEnd(), StringUtils.length(origText) );
	        	searchValueTextField.setText( firstPart + lastPart );
        	}
        });
        searchValuecontextMenu.getItems().add(menuCut);
        MenuItem menuPaste = new MenuItem("Paste");
        menuPaste.setOnAction((event) -> {
        	if (!Clipboard.getSystemClipboard().hasContent(DataFormat.PLAIN_TEXT)) {
        		return;
        	}
        	String clipboardText = Clipboard.getSystemClipboard().getString();
        	IndexRange range = searchValueTextField.getSelection();
        	String origText = searchValueTextField.getText();
        	int endPos = 0;
        	String updatedText = "";
        	String firstPart = StringUtils.substring( origText, 0, range.getStart() );
        	String lastPart = StringUtils.substring( origText, range.getEnd(), StringUtils.length(origText) );
        	updatedText = firstPart + clipboardText + lastPart;
        	if (range.getStart() == range.getEnd()) {
        		endPos = range.getEnd() + StringUtils.length(clipboardText);
        	}else{
        		endPos = range.getStart() + StringUtils.length(clipboardText);
        	}
        	searchValueTextField.setText(updatedText);
        	searchValueTextField.positionCaret(endPos);
        });
        searchValuecontextMenu.getItems().add(menuPaste);
        searchValueTextField.setContextMenu(searchValuecontextMenu);
	}

	private EventHandler<ActionEvent> getCategoryComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				categoryCombo.getStyleClass().add("selected-bold");
				categoryID = categoryCombo.getSelectionModel().getSelectedItem().getID();
				dataChanged = true;
			}
		};
	}
	
	private EventHandler<ActionEvent> getDeductibleCheckboxHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				deductibleCheckBox.getStyleClass().add("selected-bold");
				deductible = deductibleCheckBox.isSelected();
				dataChanged = true;
			}
		};
	}

	@Override
	protected CallResult validateData() {
		CallResult callResult = new CallResult();
		if (searchField == null) {
			return callResult.setCallBad("Missing Filter Column", "You need to specify a filter column.");
		}
		if (expression == null) {
			return callResult.setCallBad("Missing Search Type", "You need to specify a search type.");
		}
		if (searchValue == null || searchValue.isEmpty()) {
			return callResult.setCallBad("Missing Search Value", "You need to specify a search value.");
		}
		if (result == null) {
			return callResult.setCallBad("Missing Result Action", "You need to specify a result action.");
		}
		if (result.contains("Replace") && (replacementValue == null || replacementValue.isEmpty())) {
			return callResult.setCallBad("Missing Replacement Value", "You need to specify a replacement value.");
		}
		return callResult;
	}

	@Override
	public boolean isDataChanged() {
		return dataChanged;
	}

	@Override
	protected CallResult saveData() {
		Filter newFilter = new Filter(filterID, filterSet.getID(), sequence, searchField, expression, searchValue, 
				result, replacementValue, deductible, categoryID);
    	CallResult result;
    	if (newFilter.getID() == 0) {
        	result = ServiceFactory.getInstance().getPersistenceSvc().addFilter(newFilter);
    	}else {
        	result = ServiceFactory.getInstance().getPersistenceSvc().updateFilter(newFilter);
    	}
    	return result;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

}
