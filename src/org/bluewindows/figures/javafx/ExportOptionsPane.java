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

import org.bluewindows.figures.enums.ExportType;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ExportOptionsPane {
	
	public static double WIDTH = 350;
	public static double HEIGHTH = 120;
	private boolean canceled = false;
	private ExportType exportType;
	private Button okButton;
	private Button cancelButton;
	private Pane basePane;
	
	public Pane getExportOptionsPane() {
		exportType = ExportType.QIF;
		basePane = new Pane();
		basePane.setPrefWidth(WIDTH);
		basePane.setPrefHeight(HEIGHTH);
		
		VBox baseBox = new VBox();
		basePane.getChildren().add(baseBox);
		
		Region spacer = new Region();
		spacer.setMaxHeight(30);
		baseBox.getChildren().add(spacer);
		
		// The following box isn't really needed but is here to make the radio buttons line
		// up with the OK and Cancel buttons.
		HBox qifButtonBar = new HBox();
		baseBox.getChildren().add(qifButtonBar);

		ToggleGroup exportTypeGroup = new ToggleGroup();
		RadioButton exportQIFButton = new RadioButton("Export QIF (Quicken Format)");
		exportQIFButton.setSelected(true);
		exportQIFButton.setToggleGroup(exportTypeGroup);
		exportQIFButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent arg0) {
	            exportType = ExportType.QIF;
	        }
	    });
		qifButtonBar.getChildren().add(exportQIFButton);
		
		// The following box isn't really needed but is here to make the radio buttons line
		// up with the OK and Cancel buttons.
		HBox ofxButtonBar = new HBox();
		baseBox.getChildren().add(ofxButtonBar);
		
		RadioButton exportOFXButton = new RadioButton("Export OFX (Open Financial Exchange Format)");
		exportOFXButton.setToggleGroup(exportTypeGroup);
		exportOFXButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override
	        public void handle(ActionEvent arg0) {
	            exportType = ExportType.OFX;
	        }
	    });
		ofxButtonBar.getChildren().add(exportOFXButton);
		
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
	
	public boolean isCanceled() {
		return canceled;
	}

	public ExportType getExportType() {
		return exportType;
	}

}
