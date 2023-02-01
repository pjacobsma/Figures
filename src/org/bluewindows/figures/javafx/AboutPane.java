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

import org.bluewindows.figures.app.Figures;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AboutPane {
	
	public static double WIDTH = 524;
	public static double HEIGHTH = 500;
	private Button okButton;
	
	public Pane getAboutPane() {
		Pane basePane = new Pane();
		basePane.setPrefWidth(WIDTH);
		basePane.setPrefHeight(HEIGHTH);
		
		VBox baseBox = new VBox();
		basePane.getChildren().add(baseBox);
		
		Region spacer = new Region();
		spacer.setMaxHeight(30);
		baseBox.getChildren().add(spacer);
		
		HBox labelBox = new HBox();
		baseBox.getChildren().add(labelBox);
		Label versionLabel = new Label("Version " + Figures.VERSION);
		labelBox.getChildren().add(versionLabel);
		
		HBox licenseBox = new HBox();
		baseBox.getChildren().add(licenseBox);
		TextArea licenseText = new TextArea();
		licenseText.getStyleClass().add("transparentTextArea");
		licenseText.setPrefWidth(530);
		licenseText.setPrefHeight(400);
		licenseText.setWrapText(true);
		licenseText.setEditable(false);
		licenseText.setMouseTransparent(true);
		
		StringBuffer sb = new StringBuffer();
		sb.append("Figures is a free money management and expense tracking application.\n");
		sb.append("Copyright (C) 2010  Phil Jacobsma\n\n");
		sb.append("This program is free software; you can redistribute it and/or modify\n");
		sb.append("it under the terms of the GNU General Public License as published by\n");
		sb.append("the Free Software Foundation; either version 2 of the License, or\n");
		sb.append("(at your option) any later version.\n\n");
		sb.append("This program is distributed in the hope that it will be useful,\n");
		sb.append("but WITHOUT ANY WARRANTY; without even the implied warranty of\n");
		sb.append("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n");
		sb.append("GNU General Public License for more details.\n\n");
		sb.append("You should have received a copy of the GNU General Public License along\n");
		sb.append("with this program; if not, write to the Free Software Foundation, Inc.,\n");
		sb.append("51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.\n");

		licenseText.setText(sb.toString());
		licenseBox.getChildren().add(licenseText);
		
		HBox buttonBox = new HBox();
		baseBox.getChildren().add(buttonBox);
		okButton = new Button("OK");
		okButton.setOnAction(getOkButtonHandler());
		buttonBox.getChildren().add(okButton);

		return basePane;
	}
	
	protected EventHandler<ActionEvent> getOkButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		    }
		};
	}

}
