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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PrintTitlePane  {
	
	private TextField title;
	private boolean canceled = false;
	
	public Pane getPrintTitlePane() {
		Pane basePane = new Pane();
		VBox baseBox = new VBox();
		basePane.getChildren().add(baseBox);
		
		Region spacer1 = new Region();
		baseBox.getChildren().add(spacer1);

		HBox helpBar = new HBox();
		baseBox.getChildren().add(helpBar);

		Label helpText = new Label("Enter an optional title, or you can leave it blank.");
		helpBar.getChildren().add(helpText);
		
		HBox titleBar = new HBox();
		baseBox.getChildren().add(titleBar);

		Label label = new Label("Title:");
		label.setMinWidth(60);
		titleBar.getChildren().add(label);
		
		title = new TextField();
		title.setPrefWidth(300);
		titleBar.getChildren().add(title);
		
		HBox buttonBar = new HBox();
		baseBox.getChildren().add(buttonBar);
		Region spacer2 = new Region();
		spacer2.setMinWidth(250);
		buttonBar.getChildren().add(spacer2);
		
		Button okButton = new Button("Ok");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent arg0) {
				Stage stage = (Stage) okButton.getScene().getWindow();
				stage.hide();
	        }
	    });

		buttonBar.getChildren().add(okButton);

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent arg0) {
				canceled = true;
				Stage stage = (Stage) cancelButton.getScene().getWindow();
				stage.close();
	        }
	    });
		buttonBar.getChildren().add(cancelButton);

		return basePane;
	}

	public String getTitle() {
		return title.getText();
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	
}
