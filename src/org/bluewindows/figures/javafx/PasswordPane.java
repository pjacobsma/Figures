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

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PasswordPane {
	
	public static double NEW_WIDTH = 600;
	public static double NEW_HEIGHT = 200;
	public static double WIDTH = 600;
	public static double HEIGHT = 130;
	private static final int LABEL_WIDTH = 135;
	private static final int DATA_WIDTH = 400;

	@SuppressWarnings("unused")
	private boolean firstTime = false;
	private boolean canceled = false;
	private Pane basePane;
	private TextField password;
	private TextField passwordClear;
	private Button okButton;
	private Button cancelButton;
	
	public Pane getPasswordPane(boolean firstTime, boolean invalidPassword) {
		this.firstTime = firstTime;
		Platform.runLater(()->setCloseHandler());
		basePane = new Pane();
		basePane.setPrefWidth(NEW_WIDTH);
		basePane.setPrefHeight(NEW_HEIGHT);
		VBox baseBox = new VBox();
		basePane.getChildren().add(baseBox);
		
		Region spacer = new Region();
		spacer.setMaxHeight(10);
		baseBox.getChildren().add(spacer);

		HBox passwordBar = new HBox();
		baseBox.getChildren().add(passwordBar);
		Label passwordLabel = new Label("Enter Password");
		passwordLabel.setMinWidth(LABEL_WIDTH);
		passwordBar.getChildren().add(passwordLabel);
		
		StackPane passwordStack = new StackPane();
		passwordBar.getChildren().add(passwordStack);
		password = new PasswordField();
		password.setMinWidth(DATA_WIDTH);
		passwordClear = new TextField();
		password.setMinWidth(DATA_WIDTH);
		passwordClear.setVisible(false);
		password.textProperty().bindBidirectional(passwordClear.textProperty());
		// Allow enter key to submit the password
		password.setOnAction(e ->{
			canceled = false;
			Stage stage = (Stage) password.getScene().getWindow();
			stage.close();
		});
		passwordClear.setOnAction(e ->{
			canceled = false;
			Stage stage = (Stage) password.getScene().getWindow();
			stage.close();
		});
		passwordStack.getChildren().addAll(password, passwordClear);
		
		if (firstTime) {
			Label helpLabel = new Label("Remember the password you choose.  It cannot be recovered if you forget it.");
			baseBox.getChildren().add(helpLabel);
		}
		
		HBox showPasswordBar = new HBox();
		baseBox.getChildren().add(showPasswordBar);
		Region spacer2 = new Region();
		spacer2.setMinWidth(LABEL_WIDTH);
		showPasswordBar.getChildren().add(spacer2);
		CheckBox showPassword = new CheckBox();
		showPassword.setText("Show Password");
		showPassword.selectedProperty().addListener(
			(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
				if (new_val) {
					passwordClear.setVisible(true);
					passwordClear.toFront();
				}else {
					password.toFront();
					passwordClear.setVisible(false);
				}
		});
		showPasswordBar.getChildren().add(showPassword);

		if (firstTime) showPassword.setSelected(true);
		
		HBox buttonBar = new HBox();
		baseBox.getChildren().add(buttonBar);
		
		okButton = new Button("OK");
		okButton.setOnAction(getOkButtonHandler());
		buttonBar.getChildren().add(okButton);

		cancelButton = new Button("Cancel");
		cancelButton.setOnAction(getCancelButtonHandler());
		buttonBar.getChildren().add(cancelButton);
		
		Region spacer3 = new Region();
		spacer3.setMinWidth(20);
		buttonBar.getChildren().add(spacer3);

		Label badPassword = new Label("Invalid password");
		badPassword.setTextFill(Color.RED);
		if (!invalidPassword) badPassword.setVisible(false);
		buttonBar.getChildren().add(badPassword);

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

	public String getPassword() {
		if (password.getText().isEmpty()) {
			return null;
		}else {
			return password.getText();
		}
	}

}
