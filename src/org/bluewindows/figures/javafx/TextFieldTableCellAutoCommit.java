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

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class TextFieldTableCellAutoCommit<S, T> extends TextFieldTableCell<S, T> {

    protected TextField txtFldRef;
    protected boolean isEdit;

    public TextFieldTableCellAutoCommit() {
        this(null);
    }

    public TextFieldTableCellAutoCommit(final StringConverter<T> conv) {
        super(conv);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(final StringConverter<T> conv) {
        return list -> new TextFieldTableCellAutoCommit<S, T>(conv);
    }
    
    @Override
    public void startEdit() {
        super.startEdit();
        isEdit = true;
         if (updTxtFldRef()) {
            txtFldRef.focusedProperty().addListener(this::onFocusChg);
            txtFldRef.setOnKeyPressed(this::onKeyPrs);
        }
    }

    /**
     * @return whether {@link #txtFldRef} has been updated
     */
    protected boolean updTxtFldRef() {
        final Node g = getGraphic();
        final boolean isUpd = g != null && txtFldRef != g;
        if (isUpd) {
            txtFldRef = g instanceof TextField ? (TextField) g : null;
        }
        return isUpd;
    }

    @Override
    public void commitEdit(final T valNew) {
        isEdit = false; // Prevents double commit on enter key pressed
        if (isEditing()) {
            super.commitEdit(valNew);
        } else {
            final TableView<S> tbl = getTableView();
            if (tbl != null) {
                final TablePosition<S, T> pos = new TablePosition<>(tbl, getTableRow().getIndex(), getTableColumn()); // instead of tbl.getEditingCell()
                final CellEditEvent<S, T> ev  = new CellEditEvent<>(tbl, pos, TableColumn.editCommitEvent(), valNew);
                Event.fireEvent(getTableColumn(), ev);
            }
            updateItem(valNew, false);
            if (tbl != null) {
                tbl.edit(-1, null);
            }
         }
    }

    public void onFocusChg(final ObservableValue<? extends Boolean> obs, final boolean v0, final boolean v1) {
        if (isEdit && !v1) {
             commitEdit(getConverter().fromString(txtFldRef.getText()));
        }
    }

    protected void onKeyPrs(final KeyEvent e) {
        switch (e.getCode()) {
        case ESCAPE:
            isEdit = false;
            cancelEdit(); // see CellUtils#createTextField(...)
            e.consume();
            break;
        case TAB:
            if (e.isShiftDown()) {
                getTableView().getSelectionModel().selectPrevious();
            } else {
                getTableView().getSelectionModel().selectNext();
            }
            e.consume();
            break;
        case UP:
            getTableView().getSelectionModel().selectAboveCell();
            e.consume();
            break;
        case DOWN:
            getTableView().getSelectionModel().selectBelowCell();
            e.consume();
            break;
        case ENTER:
        	isEdit = false;
            e.consume();
            break;
        default:
            break;
        }
    }
    
}
