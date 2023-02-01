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


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionCategory;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class NewTransactionCategoryPane extends AbstractNewDataPane {
	
	private static final int ROW_HEIGHT = 30;
	private static final int BUTTON_WIDTH = 40;
	private static final int CATEGORY_WIDTH = 200;
	private static final int LABEL_WIDTH = 100;
	private static final int DATA_WIDTH = 350;
	private static final int AMOUNT_WIDTH = 100;
	private Transaction transaction;
	private List<Category> categories;
	private List<CategoryRow> rows;
	private Label description;
	private Label totalAmountLabel;
	private Color amountColor;
	private String amountStyle;
	private boolean dataChanged = false;
	
	public Pane getNewTransactionCategoryPane(Transaction transaction, List<Category> categories) {
		this.transaction = transaction;
		this.categories = categories;
		rows = new ArrayList<CategoryRow>();
		if (transaction.getAmount().isCredit()){
			amountColor = Color.GREEN;
			amountStyle = "-fx-text-inner-color: green;";
		}else {
			amountColor = Color.BLACK;
			amountStyle = "-fx-text-inner-color: black;";
		}
		initialize();
		
		HBox descriptionBar = new HBox();
		descriptionBar.setAlignment(Pos.CENTER);
		dataBox.getChildren().add(descriptionBar);

		Label descriptionLabel = new Label("Description:");
		descriptionLabel.setMinWidth(LABEL_WIDTH);
		descriptionLabel.setPrefWidth(LABEL_WIDTH);
		descriptionBar.getChildren().add(descriptionLabel);

		description = new Label(transaction.getDescription());
		description.setMinWidth(DATA_WIDTH);
		description.setPrefWidth(DATA_WIDTH);
		descriptionBar.getChildren().add(description);
		
		HBox amountBar = new HBox();
		amountBar.setAlignment(Pos.CENTER);
		dataBox.getChildren().add(amountBar);

		Label amountLabel = new Label("Total Amount:");
		amountLabel.setMinWidth(LABEL_WIDTH);
		amountLabel.setPrefWidth(LABEL_WIDTH);
		amountBar.getChildren().add(amountLabel);
		
		totalAmountLabel = new Label(transaction.getAmount().toString());
		totalAmountLabel.setMinWidth(DATA_WIDTH);
		totalAmountLabel.setPrefWidth(DATA_WIDTH);
		totalAmountLabel.setTextFill(amountColor);
		amountBar.getChildren().add(totalAmountLabel);
				
		Region dataColumnspacer = new Region();
		dataColumnspacer.setPrefHeight(10);
		dataBox.getChildren().add(dataColumnspacer);

		HBox buttonBar = new HBox();
		buttonBar.setAlignment(Pos.CENTER);
		dataBox.getChildren().add(buttonBar);
		
		Button addButton = new Button("Add Category");
		addButton.setOnAction(getAddButtonHandler());
		addButton.setTooltip(new Tooltip("Click the Add Category button to assign an additional category."));
		buttonBar.getChildren().add(addButton);	
		
		if (transaction.getCategories().size() > 0) {
			for (TransactionCategory transactionCategory : transaction.getCategories()) {
				CategoryRow row = new CategoryRow(transactionCategory.getAmount(), 
					new Category(transactionCategory.getCategoryID(), transactionCategory.getName()));
				rows.add(row);
				dataBox.getChildren().add(row.categoryRowBar);
			}
		}else {
			CategoryRow row = new CategoryRow(transaction.getAmount(), null);
			rows.add(row);
			dataBox.getChildren().add(row.categoryRowBar);
		}

		dataChanged = false;
		return basePane;
	}
	
	protected EventHandler<ActionEvent> getAddButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (rows.size() < 7) {
					CategoryRow row = new CategoryRow(new Money("0.00"), null);
					rows.add(row);
					dataBox.getChildren().add(row.categoryRowBar);
					dataChanged = true;
				}
		    }
		};
	}
	
	@Override
	protected CallResult validateData() {
		CallResult result = new CallResult();
		if (rows.size() > 0) {
			if (!categoryAmountsOK()) return result.setCallBad();
			if (!categoriesOK()) return result.setCallBad();
		}
		return result;
	}
	
	@Override
	protected boolean isDataChanged() {
		return dataChanged;
	}

	@Override
	protected CallResult saveData() {
		// Replace the transaction categories with the categories in this new data pane
		List<TransactionCategory> newCategories = new ArrayList<TransactionCategory>();
		for (CategoryRow row : rows) {
			Category category = row.categoryCombo.getSelectionModel().getSelectedItem();
			Money amount = new Money(row.categoryAmount.getText());
			if (!transaction.getAmount().isCredit()) {
				amount = new Money(amount.getValue().multiply(BigDecimal.valueOf(-1)));
			}
			TransactionCategory transactionCategory = new TransactionCategory(category.getID(), amount);
			newCategories.add(transactionCategory);
		}
		if (newCategories.isEmpty()) { // Set category to None
			TransactionCategory category = new TransactionCategory(0, transaction.getAmount());
			newCategories.add(category);
		}
		transaction.setCategories(newCategories);
		// If the user sets the category for this transaction to None, unflag it so filters can update it
		if (newCategories.size() == 1 && newCategories.get(0).getCategoryID() == 0) {
			transaction.setUserChangedCategory(false);
		}else {
			transaction.setUserChangedCategory(true);
		}
    	return ServiceFactory.getInstance().getPersistenceSvc().updateTransaction(transaction);
	}
	
	private boolean categoryAmountsOK() {
		boolean amountsOK = true;
		Money sumAmount = new Money("0.00");
		for (CategoryRow row : rows) {
			Money rowAmount = null;
			try {
				row.categoryAmount.setStyle(amountStyle);
				rowAmount = new Money(row.categoryAmount.getText());
				sumAmount.add(rowAmount);
			} catch (Exception e) {
				amountsOK = false;
				row.categoryAmount.setStyle("-fx-text-inner-color: red;");
				message.setText("Invalid category amount.");
			}
		}
		if (amountsOK) {
			if (!sumAmount.getValue().equals(transaction.getAmount().getValue().abs())) {
				amountsOK = false;
				totalAmountLabel.setTextFill(Color.RED);
				message.setText("The sum of the category amounts doesn't equal the transaction total.");
			}else {
				totalAmountLabel.setTextFill(amountColor);
			}
		}
		return amountsOK;
	}
	
	private boolean categoriesOK() {
		for (CategoryRow row : rows) {
			if (row.categoryCombo.getSelectionModel().getSelectedItem() == null) {
				message.setText("Category not selected.");
				row.categoryCombo.requestFocus();
				return false;
			}
		}
		if (rows.size() > 1) {
			for (int i = 0; i <= rows.size()-1; i++) {
				String categoryNameToCheck = rows.get(i).categoryCombo.getSelectionModel().getSelectedItem().getName();
				for (int j = i+1; j < rows.size(); j++) {
					if (categoryNameToCheck.equals(rows.get(j).categoryCombo.getSelectionModel().getSelectedItem().getName())){
						message.setText("Duplicate category.");
						rows.get(j).categoryCombo.requestFocus();
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private class CategoryRow {
		public HBox categoryRowBar = new HBox();
		public ComboBox<Category> categoryCombo = new ComboBox<Category>();
		public TextField categoryAmount = new TextField();
		public Button categoryDeleteButton = new Button();
		public int categoryRowIndex;
		
		public CategoryRow(Money rowAmount, Category rowCategory) {
			categoryRowIndex = rows.size();
			categoryDeleteButton.setPrefWidth(BUTTON_WIDTH);
			categoryDeleteButton.setPrefHeight(ROW_HEIGHT);
			categoryDeleteButton.setGraphic(new ImageView(DisplayServiceImplJavaFX.deleteIcon));
			categoryDeleteButton.setTooltip(new Tooltip("Click the X to remove this category"));
			categoryDeleteButton.setOnAction(getDeleteButtonHandler());
			categoryRowBar.getChildren().add(categoryDeleteButton);
			categoryCombo.setMinWidth(CATEGORY_WIDTH);
			categoryCombo.setPrefWidth(CATEGORY_WIDTH);
			categoryCombo.setPrefHeight(ROW_HEIGHT);
			categoryCombo.setPromptText("Choose a Category");
			for (Category category : categories) {
				categoryCombo.getItems().add(category);
			}
			if (rowCategory != null) categoryCombo.getSelectionModel().select(rowCategory);
			categoryCombo.setOnAction(getCategoryComboHandler());
			categoryRowBar.getChildren().add(categoryCombo);
			categoryAmount.setMinWidth(AMOUNT_WIDTH);
			categoryAmount.setPrefWidth(AMOUNT_WIDTH);
			categoryAmount.setAlignment(Pos.CENTER_RIGHT);
			categoryAmount.setPrefHeight(ROW_HEIGHT);
			if (rowAmount != null) {
				categoryAmount.setText(rowAmount.toString());
				categoryAmount.setStyle(amountStyle);
			}else {
				categoryAmount.setText("");
			}
			categoryAmount.focusedProperty().addListener((obs, oldVal, gotFocus) -> handleAmountFocusLost(gotFocus, categoryAmount));
			categoryAmount.setOnKeyPressed(new EventHandler<KeyEvent>(){
		        @Override
		        public void handle(KeyEvent ke){
		            if (ke.getCode().equals(KeyCode.ENTER)) handleAmountEntered(categoryAmount);
		        }
		    });			
			categoryRowBar.getChildren().add(categoryAmount);
		}
		
		private EventHandler<ActionEvent> getCategoryComboHandler() {
			return new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					dataChanged = true;
			    }
			};
		}

		private EventHandler<ActionEvent> getDeleteButtonHandler() {
			return new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					dataChanged = true;
					dataBox.getChildren().remove(rows.get(categoryRowIndex).categoryRowBar); 
					rows.remove(categoryRowIndex);
					reIndexRows();
					if (rows.size() == 1) {
						rows.get(0).categoryAmount.setText(transaction.getAmount().toString());
						categoryAmount.setStyle("-fx-text-inner-color: black;");
						message.setText("");
					}
					if (rows.size() > 0) categoryAmountsOK();
			    }
			};
		}
		
		private Object handleAmountFocusLost(Boolean gotFocus, TextField categoryAmount) {
			if (!gotFocus) handleAmountEntered(categoryAmount);
			return null;
		}
		
		private Object handleAmountEntered(TextField categoryAmount) {
			dataChanged = true;
			Money amount;
			try {
				amount = new Money(categoryAmount.getText());
				categoryAmount.setText(amount.toString());
			} catch (Exception e) {
				categoryAmount.setStyle("-fx-text-inner-color: red;");
				message.setText("Invalid category amount.");
				return null;
			}
			message.setText("");
			categoryAmount.setStyle(amountStyle);
			BigDecimal transactionAmount = transaction.getAmount().getValue().abs();
			if (rows.size() == 2 && amount.getValue().compareTo(transactionAmount) < 0) {
				Money otherCategoryAmount = new Money(transactionAmount);
				otherCategoryAmount.subtract(amount);
				int otherRow = (categoryRowIndex == 0)? 1:0;
				rows.get(otherRow).categoryAmount.setText(otherCategoryAmount.toString());
			}
			if (!categoryAmountsOK()) {
				categoryAmount.setStyle("-fx-text-inner-color: red;");
			}else {
				categoryAmount.setStyle("-fx-text-inner-color: black;");
			}
			return null;
		}
		
		private void reIndexRows() {
			for (int i = 0; i < rows.size(); i++) {
				rows.get(i).categoryRowIndex = i;
			}
		}
	}

}
