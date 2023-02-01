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

import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.enums.FilterField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class NewFiltersOptionsPane {
	
	public static final int HEIGHT = 300;
	public static final int WIDTH = 280;
	private boolean canceled = false;
	private boolean descriptionSelected = true;
	private Pane basePane;
	private RadioButton newTransactionsButton;
	private RadioButton depositsOnlyButton;
	private RadioButton withdrawalsOnlyButton;
	private Button okButton;
	private Button cancelButton;
	
	public Pane getNewFiltersOptionsPane(FilterSet filterSet) {
		basePane = new Pane();
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
		
		ToggleGroup filterTimeFrameGroup = new ToggleGroup();
		newTransactionsButton = new RadioButton("New Transactions Only");
		newTransactionsButton.setTooltip(new Tooltip("Include only the transactions loaded in the last import"));
		newTransactionsButton.setSelected(true);
		newTransactionsButton.setToggleGroup(filterTimeFrameGroup);
		scopeBox.getChildren().add(newTransactionsButton);
		RadioButton allTransactions = new RadioButton("All Transactions");
		allTransactions.setTooltip(new Tooltip("Include every transaction in the data file"));
		allTransactions.setToggleGroup(filterTimeFrameGroup);
		scopeBox.getChildren().add(allTransactions);
		
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

		if (filterSet.getDefaultColumn().equals(FilterField.DESCRIPTION.toString())) {
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

	public boolean isNewTransactionsSelected() {
		return newTransactionsButton.isSelected();
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
