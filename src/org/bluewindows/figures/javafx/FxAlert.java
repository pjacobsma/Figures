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

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Modality;

public class FxAlert extends Alert {

	public FxAlert(AlertType arg0) {
		super(arg0);
		this.initModality(Modality.APPLICATION_MODAL);
		//Remove spacing above buttons
		this.getDialogPane().getChildren().remove(1);
		// Set to auto resize
		this.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
	}
	
	public FxAlert(AlertType arg0, String title, String message, ButtonType button1, ButtonType button2) {
		super(arg0, null, button1, button2);
		this.setTitle(title);
		this.setHeaderText(message);
		this.initModality(Modality.APPLICATION_MODAL);
		//Remove spacing above buttons
		this.getDialogPane().getChildren().remove(1);
		// Set to auto resize
		this.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
		// Set default to second button
		((Button)this.getDialogPane().lookupButton(button2)).setDefaultButton(true);
	}
		
}
