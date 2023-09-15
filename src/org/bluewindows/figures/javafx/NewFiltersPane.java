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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class NewFiltersPane {
	
	public static double WIDTH = 650;
	public static double MAX_HEIGHTH = 800;
	private static final int DEFINE_COLUMN_WIDTH = 18;
	
	private Pane basePane;
	private VBox baseBox;
	private HBox buttonBar;
	private TableView<NewFilterCandidate> filterCandidateTable;
	private Button closeButton;
	private TextArea statusMessage;
	private boolean dataChanged;
	
	public Pane getNewFiltersPane(Account account, List<String> prospects, FilterField field) {
		basePane = new Pane();
		basePane.setPrefWidth(WIDTH);

		baseBox = new VBox();
		baseBox.setPrefWidth(WIDTH);
		basePane.getChildren().add(baseBox);
		
		filterCandidateTable = new TableView<NewFilterCandidate>();
		filterCandidateTable.setFixedCellSize(25);
		filterCandidateTable.setMinWidth(WIDTH - 10);
		filterCandidateTable.setMaxHeight(MAX_HEIGHTH-150);
		filterCandidateTable.setEditable(false);
		filterCandidateTable.getSelectionModel().cellSelectionEnabledProperty().set(true);
		filterCandidateTable.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.RIGHT ||
                event.getCode() == KeyCode.TAB) {
            	filterCandidateTable.getSelectionModel().selectNext();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                // Workaround for TableView.getSelectionModel().selectPrevious() due to a bug
                // stopping it from working on the first column in the last row of the table
                selectPrevious();
                event.consume();
            }
        });

		TableColumn<NewFilterCandidate, Image> defineColumn = new TableColumn<NewFilterCandidate, Image>();
		defineColumn.setCellValueFactory(new PropertyValueFactory<>("addIcon"));
		defineColumn.setCellFactory(new Callback<TableColumn<NewFilterCandidate, Image>, TableCell<NewFilterCandidate, Image>>() {
            public TableCell<NewFilterCandidate, Image> call(TableColumn<NewFilterCandidate, Image> param) {
                final ImageView imageView = new ImageView();
                imageView.setFitHeight(DEFINE_COLUMN_WIDTH - 4);
                imageView.setFitWidth(DEFINE_COLUMN_WIDTH - 4);
                TableCell<NewFilterCandidate, Image> cell = new TableCell<NewFilterCandidate, Image>(){
                   @Override
                    protected void updateItem(Image image, boolean empty) {
                        if(image != null) {
                         	imageView.setImage(image);
                        }
                    }
                };
                cell.setGraphic(imageView);
                cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        @SuppressWarnings("unchecked")
						TableCell<NewFilterCandidate, Image> cell = (TableCell<NewFilterCandidate, Image>) event.getSource();
                        int index = cell.getIndex();
                        if (index >= filterCandidateTable.getItems().size()) {
                        	return;
                        }
                        NewFilterCandidate filterCandidate = cell.getTableView().getItems().get(index);
                        handleNewFilterEvent(filterCandidate, account, field);
                     }
                });
                cell.setTooltip(new Tooltip("Click to define a filter for this"));
                return cell;
            }
        });
		defineColumn.getStyleClass().add("center-aligned-column");
		defineColumn.setStyle("-fx-alignment: CENTER;");
		defineColumn.setMinWidth(DEFINE_COLUMN_WIDTH);
		defineColumn.setMaxWidth(DEFINE_COLUMN_WIDTH);
		filterCandidateTable.getColumns().add(defineColumn);

		TableColumn<NewFilterCandidate, String> textValueColumn = new TableColumn<NewFilterCandidate, String>(field.name());
		textValueColumn.setCellValueFactory(new PropertyValueFactory<NewFilterCandidate, String>("value"));
		textValueColumn.setMinWidth(WIDTH - DEFINE_COLUMN_WIDTH - 10);
		textValueColumn.setMaxWidth(WIDTH - DEFINE_COLUMN_WIDTH - 10);
		textValueColumn.setCellFactory(new Callback<TableColumn<NewFilterCandidate, String>, TableCell<NewFilterCandidate, String>>() {
		    @SuppressWarnings("rawtypes")
			@Override
		    public TableCell call(TableColumn<NewFilterCandidate, String> p) {
		        TableCell<?, ?> cell = TableColumn.DEFAULT_CELL_FACTORY.call(p);
                int index = cell.getIndex();
                if (index >= filterCandidateTable.getItems().size()) {
                	return cell;
                }
                cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        @SuppressWarnings("unchecked")
						TableCell<NewFilterCandidate, Image> cell = (TableCell<NewFilterCandidate, Image>) event.getSource();
                        int index = cell.getIndex();
                        if (index >= filterCandidateTable.getItems().size()) {
                        	return;
                        }
                        NewFilterCandidate filterCandidate = cell.getTableView().getItems().get(index);
                        handleNewFilterEvent(filterCandidate, account, field);
                     }
                });
		        return cell;
		    }
		});	

		filterCandidateTable.getColumns().add(textValueColumn);

		baseBox.getChildren().add(filterCandidateTable);
		
		buttonBar = new HBox();
		baseBox.getChildren().add(buttonBar);

		closeButton = new Button("Close");
		closeButton.setOnAction(getCloseButtonHandler());
		buttonBar.getChildren().add(closeButton);

		statusMessage = new TextArea();
		statusMessage.prefWidthProperty().bind(baseBox.widthProperty());
		statusMessage.setEditable(false);
		statusMessage.setMouseTransparent(true);
		statusMessage.setPrefHeight(15);
		statusMessage.setMaxHeight(15);
		statusMessage.setText("");
		baseBox.getChildren().add(statusMessage);

		List<NewFilterCandidate> filterList = new ArrayList<NewFilterCandidate>();
		for (String prospect : prospects) {
			NewFilterCandidate possibleFilter = new NewFilterCandidate(prospect);
			filterList.add(possibleFilter);
		}
		ObservableList<NewFilterCandidate> displayableProspects = FXCollections.observableArrayList(filterList); 
		filterCandidateTable.setItems(displayableProspects);
		resizeUI();
		filterCandidateTable.refresh();
		filterCandidateTable.setVisible(true);
		
		return basePane;
	}
	
	private void resizeUI() {
    	filterCandidateTable.prefHeightProperty().bind(filterCandidateTable.fixedCellSizeProperty().multiply(Bindings.size(filterCandidateTable.getItems()).add(2.0)));
		if (closeButton.getScene() != null) {
	    	closeButton.getScene().getWindow().setHeight(getHeight(filterCandidateTable.getItems().size()));
		}
	}
	
	public double getHeight(int filterCandidateCount) {
		double height = filterCandidateTable.fixedCellSizeProperty().intValue() * filterCandidateCount +
			buttonBar.getPrefHeight() + statusMessage.getPrefHeight() + 200;
		if (height > MAX_HEIGHTH) height = MAX_HEIGHTH;
		return height;
	}
	
	private void handleNewFilterEvent(NewFilterCandidate filterCandidate, Account account, FilterField field) {
    	CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getMaxFilterSequence();
    	if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error retrieving filter sequence",  
    			result.getErrorMessage());
    		return;
    	}
    	FilterSettingsPane newFilterPane = new FilterSettingsPane(account.getFilterSet(), (Integer)result.getReturnedObject(), 
    		field.toString(), filterCandidate.getValue());
    	Scene scene = null;
		scene = new Scene(newFilterPane.getNewFilterPane(), FilterSettingsPane.WIDTH, FilterSettingsPane.HEIGHT);
		Transaction lastTransaction;
		if (field == FilterField.DESCRIPTION) {
			lastTransaction = getLastTransactionByDescription(account, filterCandidate.getValue());
		}else {
			lastTransaction = getLastTransactionByMemo(account, filterCandidate.getValue());
		}
		newFilterPane.message.setText("Last transaction using this value - Date: " + lastTransaction.getDate() + ", Amount: " +
			lastTransaction.getAmount());
		scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("Create New Filter");
		stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
		stage.setOnHidden((WindowEvent event1) -> {
	        if (!newFilterPane.isCanceled()) {
	        	dataChanged = true;
            	filterCandidateTable.getItems().remove(filterCandidateTable.getSelectionModel().getSelectedItem());
            	filterCandidateTable.refresh();
            	CallResult filterSetResult = ServiceFactory.getInstance().getPersistenceSvc().getFilterSet(account.getFilterSet().getID());
            	if (result.isBad()) {
            		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error retrieving filter set", 
            			result.getErrorMessage());
            		return;
            	}
            	account.setFilterSet((FilterSet)filterSetResult.getReturnedObject());
        		resizeUI();
	        }
	    });
		DisplayServiceImplJavaFX.center(stage, scene);
		stage.show();
	}
	
	private Transaction getLastTransactionByDescription(Account account, String fieldValue) {
		for (Transaction transaction : account.getTransactions()) {
			if (transaction.getDescription().equals(fieldValue)) {
				return transaction;
			}
		}
		return null;
	}
	
	private Transaction getLastTransactionByMemo(Account account, String fieldValue) {
		for (Transaction transaction : account.getTransactions()) {
			if (transaction.getMemo().equals(fieldValue)) {
				return transaction;
			}
		}
		return null;
	}

	
	protected EventHandler<ActionEvent> getCloseButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
			Stage stage = (Stage) closeButton.getScene().getWindow();
			stage.getOnCloseRequest().handle(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			stage.close();
		    }
		};
	}

    @SuppressWarnings("unchecked")
    private void selectPrevious() {
        if (filterCandidateTable.getSelectionModel().isCellSelectionEnabled()) {
            // in cell selection mode, we have to wrap around, going from
            // right-to-left, and then wrapping to the end of the previous line
            TablePosition <NewFilterCandidate, ?> pos = filterCandidateTable.getFocusModel().getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                // go to previous row
            	filterCandidateTable.getSelectionModel().select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
            } else if (pos.getRow() < filterCandidateTable.getItems().size()) {
                // wrap to end of previous row
            	filterCandidateTable.getSelectionModel().select(pos.getRow() - 1, filterCandidateTable.getVisibleLeafColumn(filterCandidateTable.getVisibleLeafColumns().size() - 1));
            }
        } else {
            int focusIndex = filterCandidateTable.getFocusModel().getFocusedIndex();
            if (focusIndex == -1) {
            	filterCandidateTable.getSelectionModel().select(filterCandidateTable.getItems().size() - 1);
            } else if (focusIndex > 0) {
            	filterCandidateTable.getSelectionModel().select(focusIndex - 1);
            }
        }
    }
    
    private TableColumn <NewFilterCandidate, ?> getTableColumn(
            final TableColumn <NewFilterCandidate, ?> column, int offset) {
            int columnIndex = filterCandidateTable.getVisibleLeafIndex(column);
            int newColumnIndex = columnIndex + offset;
            return filterCandidateTable.getVisibleLeafColumn(newColumnIndex);
    }
    
    public boolean isDataChanged() {
    	return dataChanged;
    }

}
