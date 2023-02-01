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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.AccountType;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class AccountSettingsPane extends AbstractNewDataPane {
	
	private Account account;
	private TextField accountName;
	private boolean accountNameChanged = false;
	private ComboBox<AccountType> accountTypeCombo;
	private boolean accountTypeChanged = false;
	private ComboBox<FilterSet> filterSetCombo;
	private boolean filterSetChanged = false;
	private TextField initialBalance;
	private boolean initialBalanceChanged = false;
	private Button applyInitialBalanceButton;
	private TextField currentBalance;
	private boolean currentBalanceChanged = false;
	private Button applyCurrentBalanceButton;
	private boolean initialBalanceApplied = false;
	private boolean currentBalanceApplied = false;
	private static final int LABEL_WIDTH = 125;
	private static final int COMBO_WIDTH = 240;
	
	public Pane getAccountSettingsPane(Account account) {
		initialize();
		accountNameChanged = false;
		accountTypeChanged = false;
		filterSetChanged = false;
		initialBalanceChanged = false;
		currentBalanceChanged = false;
		initialBalanceApplied = false;
		currentBalanceApplied = false;
		this.account = account;
		if (account != null) {
			CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
			if (result.isBad()) {
	    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Could not load transactions.", result.getErrorMessage());
	    		return null;
			}
		}

		HBox nameBar = new HBox();
		dataBox.getChildren().add(nameBar);

		Label accountLabel = new Label("Account Name:");
		accountLabel.setMinWidth(LABEL_WIDTH);
		nameBar.getChildren().add(accountLabel);
		
		accountName = new TextField();
		accountName.setPrefWidth(COMBO_WIDTH);
		if (account != null) {
			accountName.setText(account.getName());
			accountNameChanged = false;
		}
		accountName.textProperty().addListener((observable, oldValue, newValue) -> {
		    accountNameChanged = true;
		    if (account != null) account.setName(accountName.getText());
		});
		nameBar.getChildren().add(accountName);
			
		HBox typeBar = new HBox();
		dataBox.getChildren().add(typeBar);
		
		Label typeLabel = new Label("Account Type:");
		typeLabel.setMinWidth(LABEL_WIDTH);
		typeBar.getChildren().add(typeLabel);

		if (account == null) {
			accountTypeCombo = new ComboBox<AccountType>();
			accountTypeCombo.setPrefWidth(COMBO_WIDTH);
			accountTypeCombo.setPromptText("Choose an Account Type");
			accountTypeCombo.getItems().addAll(AccountType.values());
			accountTypeCombo.setOnAction(getAccountTypeComboHandler());
			typeBar.getChildren().add(accountTypeCombo);
		}else {
			Label typeName = new Label(account.getType().getText());
			typeName.setMinWidth(LABEL_WIDTH);
			typeBar.getChildren().add(typeName);
		}
		
		HBox filterSetBar = new HBox();
		dataBox.getChildren().add(filterSetBar);

		Label filterSetLabel = new Label("Filter Set:");
		filterSetLabel.setMinWidth(LABEL_WIDTH);
		filterSetBar.getChildren().add(filterSetLabel);

		filterSetCombo = new ComboBox<FilterSet>();
		filterSetCombo.setPrefWidth(COMBO_WIDTH);
		filterSetCombo.setPromptText("Choose a Filter Set");
		filterSetCombo.setOnAction(getFilterSetComboHandler());
		loadFilterSets();
		filterSetBar.getChildren().add(filterSetCombo);
		
		HBox initialBalanceBar = new HBox();
		dataBox.getChildren().add(initialBalanceBar);
		
		Label initialBalanceLabel = new Label("Initial Balance:");
		initialBalanceLabel.setMinWidth(LABEL_WIDTH);
		initialBalanceBar.getChildren().add(initialBalanceLabel);
		
		initialBalance = new TextField();
		initialBalance.setPrefWidth(100);
		initialBalance.setAlignment(Pos.CENTER_RIGHT);
		if (account != null) {
			initialBalance.setText(account.getInitialBalance().toStringNegative());
		}else {
			initialBalance.setText(new Money("0.00").toString());
		}
		initialBalance.textProperty().addListener((observable, oldValue, newValue) -> {
			initialBalanceChanged = true;
		});
		initialBalanceChanged = false;
		initialBalanceBar.getChildren().add(initialBalance);
		
		currentBalance = new TextField();
		currentBalance.setPrefWidth(100);
		currentBalance.setAlignment(Pos.CENTER_RIGHT);
		if (account != null) {
			applyInitialBalanceButton = new Button("Apply");
			applyInitialBalanceButton.setOnAction(getApplyInitialBalanceButtonHandler());
			initialBalanceBar.getChildren().add(applyInitialBalanceButton);

			HBox currentBalanceBar = new HBox();
			dataBox.getChildren().add(currentBalanceBar);
			
			Label currentBalanceLabel = new Label("Current Balance:");
			currentBalanceLabel.setMinWidth(LABEL_WIDTH);
			currentBalanceBar.getChildren().add(currentBalanceLabel);
			
			currentBalance.setPrefWidth(100);
			if (account.getTransactionCount() == 0) {
				currentBalance.setText(new Money("0.00").toString());
			}else {
				currentBalance.setText(account.getTransactions().get(0).getBalance().toStringNegative());
			}
			currentBalance.textProperty().addListener((observable, oldValue, newValue) -> {
				currentBalanceChanged = true;
			});
			currentBalanceChanged = false;
			currentBalanceBar.getChildren().add(currentBalance);
	
			applyCurrentBalanceButton = new Button("Apply");
			applyCurrentBalanceButton.setOnAction(getApplyCurrentBalanceButtonHandler());
			currentBalanceBar.getChildren().add(applyCurrentBalanceButton);
		}else {
			currentBalance.setVisible(false);
		}

		return basePane;
	}
	
	private EventHandler<ActionEvent> getAccountTypeComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				accountTypeCombo.getStyleClass().add("selected-bold");
				if (account != null) {
					account.setType(getAccountType());
				}
				accountTypeChanged = true;
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public CallResult loadFilterSets() {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getFilterSets();
		if (result.isBad()) return result;
		filterSetCombo.getItems().clear();
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		filterSets.add(FilterSet.NONE);
		Collections.sort(filterSets, new Comparator<FilterSet>() {
			@Override
			public int compare(FilterSet f1, FilterSet f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});	
		for (FilterSet filterSet : filterSets) {
			filterSetCombo.getItems().add(filterSet);
		}
		if (account != null) {
			if (account.getFilterSet().getID() == 0) {
				filterSetCombo.getSelectionModel().select(FilterSet.NONE); 
			}else {
				filterSetCombo.getSelectionModel().select(account.getFilterSet());
			}
		}
		filterSetChanged = false;
		return result;
	}
	
	private EventHandler<ActionEvent> getFilterSetComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				filterSetCombo.getStyleClass().add("selected-bold");
				if (account != null) {
					account.setFilterSet(getFilterSet());
				}
				if (!filterSetCombo.getSelectionModel().isEmpty()) {
					filterSetChanged = true;
				}
			}
		};
	}
	
	private EventHandler<ActionEvent> getApplyCurrentBalanceButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Money balance = null;
				try {
					balance = new Money(currentBalance.getText());
				} catch (IllegalArgumentException e) {
					Figures.logStackTrace(e);
		    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Invalid Current Balance", currentBalance.getText() + " is not a valid balance value.");
					Platform.runLater(()->currentBalance.requestFocus());
				}
				CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
				if (result.isBad()) {
		    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Could not load transactions.", result.getErrorMessage());
		    		return;
				}
				Money calculatedInitialBalance = ServiceFactory.getInstance().getMaintenanceSvc().applyCurrentBalance(account, balance);
				initialBalance.setText(calculatedInitialBalance.toStringNegative());
				account.setInitialBalance(calculatedInitialBalance);
				currentBalanceApplied = true;
				initialBalanceChanged = false;
			}
		};
	}

	private EventHandler<ActionEvent> getApplyInitialBalanceButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Money newInitialbalance = null;
				try {
					newInitialbalance = new Money(initialBalance.getText());
				} catch (IllegalArgumentException e) {
					Figures.logStackTrace(e);
		    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Invalid Initial Balance", initialBalance.getText() + " is not a valid balance value.");
					Platform.runLater(()->initialBalance.requestFocus());
				}
				Money calculatedCurrentBalance = ServiceFactory.getInstance().getMaintenanceSvc().applyInitialBalance(account.getTransactions(), newInitialbalance);
				currentBalance.setText(calculatedCurrentBalance.toStringNegative());
				account.setInitialBalance(newInitialbalance);
				initialBalanceApplied = true;
				currentBalanceChanged = false;
			}
		};
	}

	public String getAccountName() {
		return accountName.getText();
	}
	
	public AccountType getAccountType() {
		if (account != null && account.getType() != null) {
			return account.getType();
		}else {
			return accountTypeCombo.getSelectionModel().getSelectedItem();
		}
	}

	public FilterSet getFilterSet() {
		if (filterSetCombo.getValue() == null) {
			return FilterSet.NONE;
		}
		return filterSetCombo.getSelectionModel().getSelectedItem();
	}

	@Override
	protected CallResult validateData() {
		CallResult result = new CallResult();
		if (StringUtils.isEmpty(accountName.getText())) {
			Platform.runLater(()->accountName.requestFocus());
			return result.setCallBad("Missing Account Name", "You need to specify an account name.");
		}
		if (getAccountType() == null) {
			Platform.runLater(()->accountTypeCombo.requestFocus());
			return result.setCallBad("Missing Account Type", "You need to select an account type.");
		}
		try {
			new Money(initialBalance.getText());
		} catch (IllegalArgumentException e) {
			Figures.logStackTrace(e);
			Platform.runLater(()->initialBalance.requestFocus());
    		return result.setCallBad("Invalid Initial Balance", initialBalance.getText() + " is not a valid balance value.");
		}
		if (currentBalance.isVisible()) {
			try {
				new Money(currentBalance.getText());
			} catch (IllegalArgumentException e) {
				Figures.logStackTrace(e);
				Platform.runLater(()->currentBalance.requestFocus());
	    		return result.setCallBad("Invalid Current Balance", currentBalance.getText() + " is not a valid balance value.");
			}
		}
		if (initialBalanceChanged && applyInitialBalanceButton != null && !initialBalanceApplied) {
			Platform.runLater(()->applyInitialBalanceButton.requestFocus());
			return result.setCallBad("Unapplied data changes.", "The initial balance value has not been applied.  Use the Apply button next to the field.");
		}
		if (currentBalanceChanged && applyCurrentBalanceButton != null && !currentBalanceApplied) {
			Platform.runLater(()->applyCurrentBalanceButton.requestFocus());
			return result.setCallBad("Unapplied data changes.", "The current balance value has not been applied.  Use the Apply button next to the field.");
		}
		return result;
	}

	@Override
	protected boolean isDataChanged() {
		return accountNameChanged || accountTypeChanged || initialBalanceChanged || currentBalanceChanged || filterSetChanged;
	}

	@Override
	protected CallResult saveData() {
		CallResult result = new CallResult();
		if (accountNameChanged || initialBalanceApplied || currentBalanceApplied || filterSetChanged) {
			if (account == null) {
				account = new Account(0, accountName.getText(), getAccountType(), getFilterSet(), new Money(initialBalance.getText()), 
				TransactionDate.MINIMUM_DATE);
				result = ServiceFactory.getInstance().getPersistenceSvc().addAccount(account);
			}else {
				result = ServiceFactory.getInstance().getPersistenceSvc().updateAccount(account);
			}
		}
		if (result.isBad()) return result;
		if (initialBalanceApplied || currentBalanceApplied) {
			result = ServiceFactory.getInstance().getPersistenceSvc().updateTransactions(account.getTransactions());
		}
		return result;
	}

}
