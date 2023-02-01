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

import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

@SuppressWarnings("rawtypes")
public class EnhancedTableView<S> extends TableView<S> {

	@SuppressWarnings("unchecked")
	public EnhancedTableView() {
		// Enable cell editing with single mouse click
		this.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
		    TablePosition<S, ?> focusedCellPos = getFocusModel().getFocusedCell();
		    if (getEditingCell() == null) {
		    	edit(focusedCellPos.getRow(), focusedCellPos.getTableColumn());
		    }
		});
		
		// Allows clicking on empty cell to unfocus selection
		this.setRowFactory(categoryTable -> {
			TableRow<S> row = new TableRow<>();
			row.setOnMouseClicked(getUnfocusHandler(row));
			return row;
		});	

	}
	
	// This handler removes the focus from the last selected cell when the user 
	// clicks on an empty row.  It allows the user to click away from a cell 
	// even when they click on an empty row
	private EventHandler<MouseEvent> getUnfocusHandler(TableRow row) {
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if (row.emptyProperty().getValue()) { 
					getSelectionModel().clearSelection();
		        	return; 
				};
			}
		};
	}
	
}
