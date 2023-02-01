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


import java.util.Optional;

import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public abstract class AbstractNewDataPane {
	
	protected Pane basePane;
	protected VBox baseBox;
	protected VBox dataBox;
	protected Button saveButton;
	protected Button cancelButton;
	protected boolean canceled = true;
	protected boolean editPassed = true;
	protected Label message;

	protected void initialize() {
		basePane = new Pane();
		// This allows clicking anywhere on the basePane to trigger unfocused event for child nodes
		basePane.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{ basePane.requestFocus();});
		Platform.runLater(()->setCloseHandler());

		baseBox = new VBox();
		basePane.getChildren().add(baseBox);
		dataBox = new VBox();
		baseBox.getChildren().add(dataBox);
		HBox controlBar = new HBox();
		baseBox.getChildren().add(controlBar);
		
		saveButton = new Button("Save");
		saveButton.setOnAction(getSaveButtonHandler());
		controlBar.getChildren().add(saveButton);

		cancelButton = new Button("Cancel");
		cancelButton.setOnAction(getCancelButtonHandler());
		controlBar.getChildren().add(cancelButton);
		
		Region spacer = new Region();
		spacer.setMinWidth(15);
		controlBar.getChildren().add(spacer);
		
		HBox messageBar = new HBox();
		baseBox.getChildren().add(messageBar);

		message = new Label();
		message.getStyleClass().add("blue-text");
		message.setMaxHeight(60);
		message.setMinWidth(500);
		message.setPrefWidth(500);
		message.setWrapText(true);
		message.setText("");
		messageBar.getChildren().add(message);
		
	}
	
	private void setCloseHandler() {
		Stage stage = (Stage) basePane.getScene().getWindow();
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent e) {
		    	boolean closeOK = handleCancelOrClose();
		    	if (!closeOK) e.consume();
		    }
		});
	}
	
	protected EventHandler<ActionEvent> getSaveButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
			CallResult result = validateData();
			if (result.isBad()) {
				// Show error popup only if the new data pane set an error message
				if (result.getErrorMessage() != null) {
		    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage(result.getMessageDecorator(), result.getErrorMessage());
				}
				return;
			}
			canceled = false;
			result = saveData();
			if (result.isBad()) {
				return;
			}
			Stage stage = (Stage) saveButton.getScene().getWindow();
			stage.hide();
			stage.close();
		    }
		};
	}
	
	protected EventHandler<ActionEvent> getCancelButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				handleCancelOrClose();
		    }
		};
	}
	
	private boolean handleCancelOrClose() {
		if (isDataChanged()) {
		   	ButtonType yesButton = new ButtonType("Yes");
			ButtonType noButton = new ButtonType("No");
			FxAlert confirmationAlert = new FxAlert(AlertType.CONFIRMATION, "Cancel Update", "If you cancel, any changes you made will not be saved.  Do you want to cancel?", yesButton, noButton);
			confirmationAlert.initOwner(cancelButton.getScene().getWindow());
			Optional<?> result = confirmationAlert.showAndWait();
			if (result.isPresent() && result.get().equals(noButton)) {
				return false;
			}
		}
		canceled = true;
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
		return true;
	}
	
	protected boolean isCanceled() {
		return canceled;
	}
	
	public Pane getBasePane() {
		return basePane;
	}
	
	protected abstract CallResult validateData();
	
	protected abstract CallResult saveData();
	
	protected abstract boolean isDataChanged();
	
}
