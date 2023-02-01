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

import static org.bluewindows.figures.domain.persistence.Persistence.CATEGORY;
import static org.bluewindows.figures.domain.persistence.Persistence.DESCRIPTION;
import static org.bluewindows.figures.domain.persistence.Persistence.MEMO;
import static org.bluewindows.figures.domain.persistence.Persistence.MONTH;
import static org.bluewindows.figures.domain.persistence.Persistence.YEAR;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.enums.CategoryInclusion;
import org.bluewindows.figures.enums.CheckInclusion;
import org.bluewindows.figures.enums.DeductibleInclusion;
import org.bluewindows.figures.enums.TransactionInclusion;
import org.bluewindows.figures.service.ServiceFactory;
import org.controlsfx.control.CheckComboBox;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class ReportSettingsPane extends AbstractNewDataPane {
	
	public static int HEIGHT = 450;
	public static int WIDTH = 800;
	private SummaryReport summaryReport;
	private TextField reportName;
	private boolean dataChanged;
	private boolean loadingData;
	private CheckComboBox<Account> accountCombo;
	private Label summarizationDisplay;
	private CheckBox summarizationYear;
	private CheckBox summarizationMonth;
	private CheckBox summarizationCategory;
	private CheckBox summarizationDescription;
	private CheckBox summarizationMemo;
	private RadioButton transactionsAll;
	private RadioButton transactionsWithdrawals;
	private RadioButton transactionsDeposits;
	private CheckBox deductible;
	private DeductibleInclusion deductibleInclusion = DeductibleInclusion.NONE;
	private CheckBox checks;
	private CheckInclusion checkInclusion = CheckInclusion.NONE;
	private CheckComboBox<Category> categoryCombo;
	private boolean categoryControlCrossCheck = true;
	private CheckBox categorizedOnly;
	private RadioButton allCategories;
	private RadioButton includeCategories;
	private RadioButton excludeCategories;
	
	private boolean newReport;

	private static final int LABEL_WIDTH = 100;
	private double controlWidth;
	
	public Pane getSummaryReportSettingsPane(SummaryReport report) {
		initialize();
		
		if (report == null) {
			newReport = true;
			summaryReport = new SummaryReport();
		}else {
			newReport = false;
			summaryReport = report;
		}
		
		HBox nameBar = new HBox();
		dataBox.getChildren().add(nameBar);
		Label nameLabel = new Label("Report Name:");
		nameLabel.setMinWidth(LABEL_WIDTH);
		nameBar.getChildren().add(nameLabel);
		reportName = new TextField();
		reportName.textProperty().addListener((observable, oldValue, newValue) -> {
			dataChanged = true;
		});
		nameBar.getChildren().add(reportName);

		HBox summarizationHeaderBar = new HBox();
		dataBox.getChildren().add(summarizationHeaderBar);
		Label summarizationLabel = new Label("Summarize By");
		summarizationHeaderBar.getChildren().add(summarizationLabel);
		summarizationLabel.setMinWidth(LABEL_WIDTH);
		summarizationLabel.setId("BoldText");
		Separator summarizationSeparator = new Separator();
		summarizationHeaderBar.getChildren().add(summarizationSeparator);
		
		HBox summarizationDisplayBar = new HBox();
		dataBox.getChildren().add(summarizationDisplayBar);
		Region spacer = new Region();
		spacer.setMinWidth(LABEL_WIDTH);
		spacer.setMaxWidth(LABEL_WIDTH);
		summarizationDisplayBar.getChildren().add(spacer);
		summarizationDisplay = new Label();
		summarizationDisplay.setId("BoldText");
		summarizationDisplay.setText(summaryReport.getSummaryFields());

		summarizationDisplayBar.getChildren().add(summarizationDisplay);
		
		HBox summarizationSelectionBar = new HBox();
		dataBox.getChildren().add(summarizationSelectionBar);
		Label summarizationSpacer = new Label();
		summarizationSelectionBar.getChildren().add(summarizationSpacer);
		summarizationSpacer.setMinWidth(LABEL_WIDTH);
		summarizationYear = new CheckBox("Year");
		summarizationYear.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				if (!loadingData) {
					if (new_val) {
						addSummarization(YEAR);
					}else {
						removeSummarization(YEAR);
					}
				}
	    });
		summarizationSelectionBar.getChildren().add(summarizationYear);
		summarizationMonth = new CheckBox("Month");
		summarizationMonth.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				if (!loadingData) {
					if (new_val) {
						addSummarization(MONTH);
					}else {
						removeSummarization(MONTH);
					}
				}
		});
		summarizationSelectionBar.getChildren().add(summarizationMonth);
		summarizationCategory = new CheckBox("Category");
		summarizationCategory.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				if (!loadingData) {
					if (new_val) {
						addSummarization(CATEGORY);
					}else {
						removeSummarization(CATEGORY);
					}
				}
		});
		summarizationSelectionBar.getChildren().add(summarizationCategory);
		summarizationDescription = new CheckBox("Description");
		summarizationDescription.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				if (!loadingData) {
					if (new_val) {
						addSummarization(DESCRIPTION);
					}else {
						removeSummarization(DESCRIPTION);
					}
				}
		});
		summarizationSelectionBar.getChildren().add(summarizationDescription);
		summarizationMemo = new CheckBox("Memo");
		summarizationMemo.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				if (!loadingData) {
					if (new_val) {
						addSummarization(MEMO);
					}else {
						removeSummarization(MEMO);
					}
				}
		});
		summarizationSelectionBar.getChildren().add(summarizationMemo);
		
		HBox inclusionHeaderBar = new HBox();
		dataBox.getChildren().add(inclusionHeaderBar);
		Label inclusionLabel = new Label("Include");
		inclusionHeaderBar.getChildren().add(inclusionLabel);
		inclusionLabel.setMinWidth(LABEL_WIDTH);
		inclusionLabel.setId("BoldText");
		Separator inclusionSeparator = new Separator();
		inclusionHeaderBar.getChildren().add(inclusionSeparator);
		
		HBox accountBar = new HBox();
		dataBox.getChildren().add(accountBar);
		Label accountLabel = new Label("Accounts:");
		accountLabel.setMinWidth(LABEL_WIDTH);
		accountBar.getChildren().add(accountLabel);
		accountCombo = new CheckComboBox<Account>();
		accountCombo.setTitle("Select Accounts");
		accountCombo.setShowCheckedCount(true);
		accountCombo.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Account>() {
			@Override
			public void onChanged(Change<? extends Account> arg0) {
				dataChanged = true;
		    	if (accountCombo.getCheckModel().getCheckedItems().size() > 0) {
		    		accountCombo.setStyle("-fx-font-weight: bold;");
		    	}else {
		    		accountCombo.setStyle("-fx-font-weight: normal;");
		    	}
			}
	     });
		accountBar.getChildren().add(accountCombo);
		
		HBox transactionTypeBar = new HBox();
		dataBox.getChildren().add(transactionTypeBar);
		Label transactionTypeLabel = new Label("Transactions");
		summarizationSelectionBar.getChildren().add(transactionTypeLabel);
		transactionTypeLabel.setMinWidth(LABEL_WIDTH);
		transactionTypeBar.getChildren().add(transactionTypeLabel);
		ToggleGroup transactionTypeGroup = new ToggleGroup();
		transactionTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
	        @Override
	        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				dataChanged = true;
	        }
	    });
		transactionsAll = new RadioButton("All Transactions");
		RadioButtonToggleHandler allTransactionsToggler = new RadioButtonToggleHandler(transactionsAll);
		transactionsAll.setOnMousePressed(allTransactionsToggler.getMousePressed());
		transactionsAll.setOnMouseReleased(allTransactionsToggler.getMouseReleased());
		transactionsAll.setToggleGroup(transactionTypeGroup);
		transactionTypeBar.getChildren().add(transactionsAll);
		transactionsWithdrawals = new RadioButton("Withdrawals");
		RadioButtonToggleHandler withdrawalsToggler = new RadioButtonToggleHandler(transactionsWithdrawals);
		transactionsWithdrawals.setOnMousePressed(withdrawalsToggler.getMousePressed());
		transactionsWithdrawals.setOnMouseReleased(withdrawalsToggler.getMouseReleased());
		transactionsWithdrawals.setToggleGroup(transactionTypeGroup);
		transactionTypeBar.getChildren().add(transactionsWithdrawals);
		transactionsDeposits = new RadioButton("Deposits");
		RadioButtonToggleHandler depositsToggler = new RadioButtonToggleHandler(transactionsDeposits);
		transactionsDeposits.setOnMousePressed(depositsToggler.getMousePressed());
		transactionsDeposits.setOnMouseReleased(depositsToggler.getMouseReleased());
		transactionsDeposits.setToggleGroup(transactionTypeGroup);
		transactionTypeBar.getChildren().add(transactionsDeposits);
		deductible = new CheckBox("Deductible");
		deductible.setOnAction((ActionEvent e) -> {
			dataChanged = true;
		    if (deductibleInclusion.equals(DeductibleInclusion.EXCLUDED)) {
		    	deductibleInclusion = DeductibleInclusion.NONE;
		    	deductible.setSelected(false);
		    	deductible.getStyleClass().removeAll("red-text");
		    	message.setText("");
		    }else if (deductibleInclusion.equals(DeductibleInclusion.INCLUDED)){
		    	deductibleInclusion = DeductibleInclusion.EXCLUDED;
		    	deductible.setSelected(true);
		    	deductible.getStyleClass().add("red-text");
		    	message.setText("Deductible transactions are excluded.");
		    }else {
		    	deductibleInclusion = DeductibleInclusion.INCLUDED;
		    	deductible.setSelected(true);
		    	deductible.getStyleClass().removeAll("red-text");
		    	message.setText("");
		    }
		});

		transactionTypeBar.getChildren().add(deductible);
		checks = new CheckBox("Checks");
		checks.setOnAction((ActionEvent e) -> {
			dataChanged = true;
		    if (checkInclusion.equals(CheckInclusion.EXCLUDED)) {
		    	checkInclusion = CheckInclusion.NONE;
		    	checks.setSelected(false);
		    	checks.getStyleClass().removeAll("red-text");
		    	message.setText("");
		    }else if (checkInclusion.equals(CheckInclusion.INCLUDED)){
		    	checkInclusion = CheckInclusion.EXCLUDED;
		    	checks.setSelected(true);
		    	checks.getStyleClass().add("red-text");
		    	message.setText("Checks are excluded.");
		    }else {
		    	checkInclusion = CheckInclusion.INCLUDED;
		    	checks.setSelected(true);
		    	checks.getStyleClass().removeAll("red-text");
		    	message.setText("");
		    }
		});
		transactionTypeBar.getChildren().add(checks);
		
		HBox categorySelectionBar = new HBox();
		dataBox.getChildren().add(categorySelectionBar);
		Label categoryLabel = new Label("Categories:");
		categoryLabel.setMinWidth(LABEL_WIDTH);
		categorySelectionBar.getChildren().add(categoryLabel);
		categorizedOnly = new CheckBox("Categorized Only");
		categorizedOnly.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				dataChanged = true;
	    });
		categorySelectionBar.getChildren().add(categorizedOnly);

		HBox categoryInclusionBar = new HBox();
		dataBox.getChildren().add(categoryInclusionBar);
		Region inclusionSpacer = new Region();
		categoryInclusionBar.getChildren().add(inclusionSpacer);
		inclusionSpacer.setMinWidth(LABEL_WIDTH);

		ToggleGroup inclusionTypeGroup = new ToggleGroup();
		transactionTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
	        @Override
	        public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				dataChanged = true;
	        }
	    });
		allCategories = new RadioButton("All");
		allCategories.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
			dataChanged = true;
			if (new_val && categoryControlCrossCheck) {
				categoryControlCrossCheck = false;
				categoryCombo.getCheckModel().clearChecks();
				categoryCombo.setDisable(true);
				categoryControlCrossCheck = true;
			}
		});
		RadioButtonToggleHandler allCategoryToggler = new RadioButtonToggleHandler(allCategories);
		allCategories.setOnMousePressed(allCategoryToggler.getMousePressed());
		allCategories.setOnMouseReleased(allCategoryToggler.getMouseReleased());
		allCategories.setToggleGroup(inclusionTypeGroup);
		categoryInclusionBar.getChildren().add(allCategories);

		includeCategories = new RadioButton("Include These Categories");
		includeCategories.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
			dataChanged = true;
			if (new_val) {
				categoryCombo.setDisable(false);
			}
		});
		RadioButtonToggleHandler inclusionToggler = new RadioButtonToggleHandler(includeCategories);
		includeCategories.setOnMousePressed(inclusionToggler.getMousePressed());
		includeCategories.setOnMouseReleased(inclusionToggler.getMouseReleased());
		includeCategories.setToggleGroup(inclusionTypeGroup);
		categoryInclusionBar.getChildren().add(includeCategories);
		
		excludeCategories = new RadioButton("Exclude These Categories");
		excludeCategories.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
			dataChanged = true;
			if (new_val) {
				categoryCombo.setDisable(false);
			}
		});
		RadioButtonToggleHandler exclusionToggler = new RadioButtonToggleHandler(excludeCategories);
		excludeCategories.setOnMousePressed(exclusionToggler.getMousePressed());
		excludeCategories.setOnMouseReleased(exclusionToggler.getMouseReleased());
		excludeCategories.setToggleGroup(inclusionTypeGroup);
		categoryInclusionBar.getChildren().add(excludeCategories);

		HBox categoryDropdownBar = new HBox();
		dataBox.getChildren().add(categoryDropdownBar);
		Region categorySpacer = new Region();
		categoryDropdownBar.getChildren().add(categorySpacer);
		categorySpacer.setMinWidth(LABEL_WIDTH);
		
		categoryCombo = new CheckComboBox<Category>();
		categoryCombo.setTitle("Select Categories");
		categoryCombo.setShowCheckedCount(true);
		categoryCombo.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Category>() {
			@Override
			public void onChanged(Change<? extends Category> arg0) {
				dataChanged = true;
		    	if (categoryCombo.getCheckModel().getCheckedItems().size() > 0) {
			    	categoryCombo.setStyle("-fx-font-weight: bold;");
			    	if (categoryControlCrossCheck) {
			    		categoryControlCrossCheck = false;
			    		allCategories.setSelected(false);
			    		categoryControlCrossCheck = true;
			    	}
		    	}else {
			    	categoryCombo.setStyle("-fx-font-weight: normal;");
		    		categoryControlCrossCheck = false;
			    	allCategories.setSelected(true);
		    		categoryControlCrossCheck = true;
		    	}
			}
	     });
		categoryDropdownBar.getChildren().add(categoryCombo);
		
		Platform.runLater(() -> {
			controlWidth = transactionsAll.getWidth() + transactionsWithdrawals.getWidth() + 
				transactionsDeposits.getWidth() + deductible.getWidth() +
				deductible.getWidth() + checks.getWidth(); 
			reportName.setPrefWidth(controlWidth);
			accountCombo.setMinWidth(controlWidth);
			categoryCombo.setMinWidth(controlWidth);
			summarizationSeparator.setMinWidth(controlWidth);
			inclusionSeparator.setMinWidth(controlWidth);
			summarizationDisplay.setMinWidth(controlWidth);
		});
		
		CallResult result = populateFields();
		if (result.isBad()) return null;
		dataChanged = false;
		return basePane;
	}
	
	private CallResult populateFields() {
		loadingData = true;
		reportName.setText(summaryReport.getName());
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getAccounts();
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Accounts", result.getErrorMessage());
    		return result;
		}
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>) result.getReturnedObject();
		if (accounts.size() > 0) {
			for (Account account : accounts) {
				accountCombo.getItems().add(account);
			}
		}
		result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Categories", result.getErrorMessage());
    		return result;
		}
		@SuppressWarnings("unchecked")
		List<Category> categories = (List<Category>) result.getReturnedObject();
		if (categories.size() > 0) {
			for (Category category : categories) {
				categoryCombo.getItems().add(category);
			}
		}
		if (!summaryReport.getAccounts().isEmpty()) {
			for (Account account : summaryReport.getAccounts()) {
				accountCombo.getCheckModel().check(account);
			}
		}
		if (summaryReport.getSummaryFields().contains(YEAR)) summarizationYear.setSelected(true);
		if (summaryReport.getSummaryFields().contains(MONTH)) summarizationMonth.setSelected(true);
		if (summaryReport.getSummaryFields().contains(CATEGORY)) summarizationCategory.setSelected(true);
		if (summaryReport.getSummaryFields().contains(DESCRIPTION)) summarizationDescription.setSelected(true);
		if (summaryReport.getSummaryFields().contains(MEMO)) summarizationMemo.setSelected(true);
		if (summaryReport.getTransactionInclusion() == null) summaryReport.setTransactionInclusion(TransactionInclusion.ALL);
		switch (summaryReport.getTransactionInclusion()) {
			case ALL:
				transactionsAll.setSelected(true);
				break;
			case WITHDRAWALS:
				transactionsWithdrawals.setSelected(true);
				break;
			case DEPOSITS:
				transactionsDeposits.setSelected(true);
				break;
		}
		if (summaryReport.getCategoryInclusion() == null) summaryReport.setCategoryInclusion(CategoryInclusion.ALL);
		switch (summaryReport.getCategoryInclusion()) {
		case ALL:
			allCategories.setSelected(true);
			break;
		case INCLUDED:
			includeCategories.setSelected(true);
			break;
		case EXCLUDED:
			excludeCategories.setSelected(true);
			break;
		}
		if (summaryReport.isCategorizedOnly()) {
			categorizedOnly.selectedProperty().set(true);
		}
		if (summaryReport.getDeductibleInclusion() == null) summaryReport.setDeductibleInclusion(DeductibleInclusion.NONE);
		deductibleInclusion = summaryReport.getDeductibleInclusion();
		switch (deductibleInclusion) {
			case NONE:
				deductible.setSelected(false);
		    	deductible.getStyleClass().removeAll("red-text");
				break;
			case INCLUDED:
				deductible.setSelected(true);
				break;
			case EXCLUDED:
				deductible.setSelected(true);
		    	deductible.getStyleClass().add("red-text");
				break;
		}
		if (summaryReport.getCheckInclusion() == null) summaryReport.setCheckInclusion(CheckInclusion.NONE);
		checkInclusion = summaryReport.getCheckInclusion();
		switch (checkInclusion) {
			case NONE:
				checks.setSelected(false);
		    	checks.getStyleClass().removeAll("red-text");
				break;
			case INCLUDED:
				checks.setSelected(true);
		    	checks.getStyleClass().removeAll("red-text");
				break;
			case EXCLUDED:
				checks.setSelected(true);
		    	checks.getStyleClass().add("red-text");
				break;
		}
		for (Category category : summaryReport.getCategories()) {
			categoryCombo.getCheckModel().check(category);
		}
		loadingData = false;
		return result;
	}

	@Override
	protected CallResult validateData() {
		CallResult result = new CallResult();
		if (StringUtils.isEmpty(reportName.getText())) {
			Platform.runLater(()->reportName.requestFocus());
			return result.setCallBad("Missing Report Name", "You need to specify a report name.");
		}
		if (!(summarizationYear.isSelected() || summarizationMonth.isSelected() || summarizationCategory.isSelected() 
				|| summarizationDescription.isSelected() || summarizationMemo.isSelected())) {
			return result.setCallBad("Missing SummaryPeriod", "You need to specify how you want to summarize.");
		}
		if (accountCombo.getCheckModel().isEmpty()) {
			Platform.runLater(()->accountCombo.requestFocus());
			return result.setCallBad("Missing Account", "You need to select one or more accounts.");
		}
		if (!(transactionsAll.isSelected() || transactionsWithdrawals.isSelected() || transactionsDeposits.isSelected() ||
			deductible.isSelected())) {
			return result.setCallBad("Missing Transaction Inclusion", "You need to select a transactions subset.");
		}
		if (!(allCategories.isSelected() || includeCategories.isSelected() || excludeCategories.isSelected())) {
				return result.setCallBad("Missing Category Inclusion", "You need to select a categories subset.");
		}
		if (!allCategories.isSelected()){
			String inclusionText;
			if (includeCategories.isSelected()) {
				inclusionText = "include.";
			}else {
				inclusionText = "exclude.";
			}
			if (categoryCombo.getCheckModel().getCheckedItems().size() == 0) {
				return result.setCallBad("Missing Category Inclusion", "You need to pick the categories you want to " + inclusionText);
			}
		}
		return result;
	}

	@Override
	protected CallResult saveData() {
		populateReport();
		CallResult result = new CallResult();
		if (newReport) {
			result = ServiceFactory.getInstance().getPersistenceSvc().saveReport(summaryReport);
		}else {
			result = ServiceFactory.getInstance().getPersistenceSvc().updateReport(summaryReport);
		}
		return result;
	}
	
	private void populateReport() {
		summaryReport.setName(reportName.getText());
		summaryReport.setSummaryFields(summarizationDisplay.getText());
		setAccounts();
		setTransactionInclusion();
		setCategoryInclusion();
		summaryReport.setDeductibleInclusion(deductibleInclusion);
		summaryReport.setCheckInclusion(checkInclusion);
		setCategories();
	}
	
	private void addSummarization(String summaryField) {
		if (summarizationDisplay.getText().isEmpty()) {
			summarizationDisplay.setText(summaryField);
		}else {
			summarizationDisplay.setText(summarizationDisplay.getText() + ", " + summaryField);
		}
	}
	
	private void removeSummarization(String summaryField) {
		summarizationDisplay.setText(StringUtils.remove(summarizationDisplay.getText(), summaryField));
		summarizationDisplay.setText(StringUtils.replace(summarizationDisplay.getText(), ", ,", ", "));
		summarizationDisplay.setText(summarizationDisplay.getText().trim());
		if (StringUtils.startsWith(summarizationDisplay.getText(), ",")) {
			summarizationDisplay.setText(StringUtils.removeStart(summarizationDisplay.getText(), ","));
		}
		if (StringUtils.endsWith(summarizationDisplay.getText(), ",")) {
			summarizationDisplay.setText(StringUtils.removeEnd(summarizationDisplay.getText(), ","));
		}
		summarizationDisplay.setText(summarizationDisplay.getText().trim());
	}
	
	private void setAccounts() {
		if (summaryReport.getAccounts().isEmpty()) {
			for (Account account : accountCombo.getCheckModel().getCheckedItems()) {
				summaryReport.addAccount(account);
			}
		}else {
			for (Account account : summaryReport.getAccounts()) {
				if (!accountCombo.getCheckModel().getCheckedItems().contains(account)) {
					summaryReport.deleteAccount(account);
				}
			}
			for (Account account : accountCombo.getCheckModel().getCheckedItems()) {
				if (!summaryReport.getAccounts().contains(account)) {
					summaryReport.addAccount(account);
				}
			}
		}
	}
	
	private void setTransactionInclusion() {
		if (transactionsAll.isSelected()){
			summaryReport.setTransactionInclusion(TransactionInclusion.ALL);
		}else if (transactionsWithdrawals.isSelected()) {
			summaryReport.setTransactionInclusion(TransactionInclusion.WITHDRAWALS);
		}else if (transactionsDeposits.isSelected()) {
			summaryReport.setTransactionInclusion(TransactionInclusion.DEPOSITS);
		}
	}
	
	private void setCategoryInclusion() {
		summaryReport.setCategorizedOnly(categorizedOnly.isSelected());
		if (allCategories.isSelected()){
			summaryReport.setCategoryInclusion(CategoryInclusion.ALL);
			if (summaryReport.getCategories().size() > 0) {
				summaryReport.deleteAllCategories();
			}
		}else if (includeCategories.isSelected()) {
			summaryReport.setCategoryInclusion(CategoryInclusion.INCLUDED);
		}else {
			summaryReport.setCategoryInclusion(CategoryInclusion.EXCLUDED);
		}
	}
	
	private void setCategories() {
		if (allCategories.isSelected()) return;
		summaryReport.deleteAllCategories();
		for (Category category : categoryCombo.getCheckModel().getCheckedItems()) {
			summaryReport.addCategory(category);
		}
	}

	@Override
	protected boolean isDataChanged() {
		return dataChanged;
	}
	
	public SummaryReport getSummaryReport() {
		return summaryReport;
	}

}
