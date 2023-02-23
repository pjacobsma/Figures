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

import java.time.LocalDate;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.FilterField;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

public class NewFiltersOptionsPane {
	
	public static final int HEIGHT = 360;
	public static final int WIDTH = 280;
	private boolean canceled = false;
	private boolean descriptionSelected = true;
	private Account account;
	private Pane basePane;
	private ToggleGroup filterTimeFrameGroup;
	private DatePicker datePicker;
	private RadioButton depositsOnlyButton;
	private RadioButton withdrawalsOnlyButton;
	private Button okButton;
	private Button cancelButton;
	
	public Pane getNewFiltersOptionsPane(Account selectedAccount) {
		account = selectedAccount;
		basePane = new Pane();
		basePane.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{ basePane.requestFocus();});
		Platform.runLater(()->setCloseHandler());
		VBox baseBox = new VBox();
		basePane.getChildren().add(baseBox);
		
		Region spacer1 = new Region();
		spacer1.setMaxHeight(10);
		baseBox.getChildren().add(spacer1);

		Label filterScopeLabel = new Label("Define Filters For");
		filterScopeLabel.setMinWidth(100);
		baseBox.getChildren().add(filterScopeLabel);
		
		VBox scopeBox = new VBox();
		VBox.setMargin(scopeBox, new Insets(0,3,3,10));
		baseBox.getChildren().add(scopeBox);
		
		filterTimeFrameGroup = new ToggleGroup();

		RadioButton newTransactionsButton = new RadioButton("New Transactions Only");
		newTransactionsButton.setTooltip(new Tooltip("Include only the transactions loaded in the last import"));
		newTransactionsButton.setSelected(true);
		newTransactionsButton.setToggleGroup(filterTimeFrameGroup);
		RadioButtonToggleHandler newTransactionsButtonToggler = new RadioButtonToggleHandler(newTransactionsButton);
		newTransactionsButton.setOnMousePressed(newTransactionsButtonToggler.getMousePressed());
		newTransactionsButton.setOnMouseReleased(newTransactionsButtonToggler.getMouseReleased());
		scopeBox.getChildren().add(newTransactionsButton);
		RadioButton allTransactionsButton = new RadioButton("All Transactions");
		allTransactionsButton.setTooltip(new Tooltip("Include every transaction in the data file"));
		allTransactionsButton.setToggleGroup(filterTimeFrameGroup);
		RadioButtonToggleHandler allTransactionsButtonToggler = new RadioButtonToggleHandler(allTransactionsButton);
		allTransactionsButton.setOnMousePressed(allTransactionsButtonToggler.getMousePressed());
		allTransactionsButton.setOnMouseReleased(allTransactionsButtonToggler.getMouseReleased());
		scopeBox.getChildren().add(allTransactionsButton);
		
		HBox dateBox = new HBox();
		scopeBox.getChildren().add(dateBox);
		
		Label dateLabel = new Label("Starting With Date: ");
		dateBox.getChildren().add(dateLabel);
		datePicker = getDatePicker();
		datePicker.setValue(account.getLastLoadedDate().value());
//		datePicker.getEditor().focusedProperty().addListener(new ChangeListener<Boolean>() {
		datePicker.valueProperty().addListener((ov, oldValue, newValue) -> {
//        	LocalDate date = datePicker.getConverter().fromString(datePicker.getEditor().getText());
//        	if (date != null) datePicker.setValue(date);
			if (datePicker.focusedProperty().getValue()) {
            	if (allTransactionsButton.isSelected()) allTransactionsButton.setSelected(false);
            	if (newTransactionsButton.isSelected()) newTransactionsButton.setSelected(false);
			}
	    });
		dateBox.getChildren().add(datePicker);
		filterTimeFrameGroup.selectedToggleProperty().addListener(getFilterTimeFrameGroupChangeListener());
		
		scopeBox.getChildren().add(new Separator());
		
		ToggleGroup transactionTypeGroup = new ToggleGroup();
		depositsOnlyButton = new RadioButton("Deposits Only");
		depositsOnlyButton.setSelected(false);
		depositsOnlyButton.setToggleGroup(transactionTypeGroup);
		RadioButtonToggleHandler depositsOnlyToggler = new RadioButtonToggleHandler(depositsOnlyButton);
		depositsOnlyButton.setOnMousePressed(depositsOnlyToggler.getMousePressed());
		depositsOnlyButton.setOnMouseReleased(depositsOnlyToggler.getMouseReleased());
		scopeBox.getChildren().add(depositsOnlyButton);

		withdrawalsOnlyButton = new RadioButton("Withdrawals Only");
		withdrawalsOnlyButton.setSelected(false);
		withdrawalsOnlyButton.setToggleGroup(transactionTypeGroup);
		RadioButtonToggleHandler withdrawalsOnlyToggler = new RadioButtonToggleHandler(withdrawalsOnlyButton);
		withdrawalsOnlyButton.setOnMousePressed(withdrawalsOnlyToggler.getMousePressed());
		withdrawalsOnlyButton.setOnMouseReleased(withdrawalsOnlyToggler.getMouseReleased());
		scopeBox.getChildren().add(withdrawalsOnlyButton);
		
		Region spacer2 = new Region();
		spacer2.setMaxHeight(10);
		baseBox.getChildren().add(spacer2);

		Label filterSourceLabel = new Label("Define Filters On");
		filterSourceLabel.setMinWidth(100);
		baseBox.getChildren().add(filterSourceLabel);
		
		VBox sourceBox = new VBox();
		VBox.setMargin(sourceBox, new Insets(0,3,3,10));
		baseBox.getChildren().add(sourceBox);

		ToggleGroup filterSourceGroup = new ToggleGroup();
		RadioButton useDescriptions = new RadioButton("Descriptions");
		useDescriptions.setToggleGroup(filterSourceGroup);
		useDescriptions.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent arg0) {
	            	descriptionSelected = useDescriptions.isSelected();
	        }
	    });
		sourceBox.getChildren().add(useDescriptions);
		RadioButton useMemos = new RadioButton("Memos");
		useMemos.setToggleGroup(filterSourceGroup);
		useMemos.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent arg0) {
	            	descriptionSelected = !useMemos.isSelected();
	        }
	    });
		sourceBox.getChildren().add(useMemos);

		if (account.getFilterSet().getDefaultColumn().equals(FilterField.DESCRIPTION.toString())) {
			useDescriptions.setSelected(true);
			descriptionSelected = true;
		}else {
			useMemos.setSelected(true);
			descriptionSelected = false;  
		}

		Region spacer3 = new Region();
		spacer3.setMaxHeight(10);
		baseBox.getChildren().add(spacer3);

		HBox buttonBar = new HBox();
		baseBox.getChildren().add(buttonBar);
		
		okButton = new Button("OK");
		okButton.setOnAction(getOkButtonHandler());
		buttonBar.getChildren().add(okButton);

		cancelButton = new Button("Cancel");
		cancelButton.setOnAction(getCancelButtonHandler());
		buttonBar.getChildren().add(cancelButton);
		
		return basePane;
	}
	
	private ChangeListener<Toggle> getFilterTimeFrameGroupChangeListener() {
		return new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
		        if (filterTimeFrameGroup.getSelectedToggle() != null) {
					if (((ToggleButton)filterTimeFrameGroup.getSelectedToggle()).getText().equals("All Transactions")) { 
						datePicker.setValue(account.getTransactions().get(account.getTransactions().size()-1).getDate().value());
					}else{
						datePicker.setValue(account.getLastLoadedDate().value());
				    }
		        }
			}
		};
	}
	
	private DatePicker getDatePicker() {
		DatePicker datePicker = new DatePicker();
		datePicker.setEditable(true);
		datePicker.getStyleClass().add("datepicker");
		datePicker.setPrefWidth(110);
		datePicker.focusedProperty().addListener(new ChangeListener<Boolean>() {
	        @Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	            if (!newValue){
	            	LocalDate date = datePicker.getConverter().fromString(datePicker.getEditor().getText());
	            	if (date != null) datePicker.setValue(date);
	            }
	        }
	    });
		datePicker.setConverter(new StringConverter<LocalDate>() {
	            @Override
	            public String toString(LocalDate date) {
	                if (date == null) {
	                    return "" ;
	                }
	                return new TransactionDate(date).toString();
	            }

	            @Override
	            public LocalDate fromString(String string) {
	                if (string == null || string.isEmpty()) {
	                    return null ;
	                }
					return LocalDate.parse(string, Figures.dateFormat);
	            }

	        });
		return datePicker;
	}

	
	protected EventHandler<ActionEvent> getOkButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
			canceled = false;
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		    }
		};
	}
	
	protected EventHandler<ActionEvent> getCancelButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
			canceled = true;
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		    }
		};
	}
	
	private void setCloseHandler() {
		Stage stage = (Stage) basePane.getScene().getWindow();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent e) {
				canceled = true;
		    }
		});
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public TransactionDate getStartDate() {
		return new TransactionDate(datePicker.getValue());
	}

	public boolean isDescriptionsSelected() {
		return descriptionSelected;
	}
	
	public boolean isDepositsOnlySelected() {
		return depositsOnlyButton.isSelected();
	}
	
	public boolean isWithdrawalsOnlySelected() {
		return withdrawalsOnlyButton.isSelected();
	}

}
