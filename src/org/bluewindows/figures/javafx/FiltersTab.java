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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.filter.Filter;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;

public class FiltersTab {

	private static final int CELL_SIZE = 25;
	private Tab filterTab;
	private VBox baseVBox;
	private FilterSetSettingsPane newFilterSetSettingsPane = new FilterSetSettingsPane();
	private FilterSettingsPane newFilterPane;
	private ComboBox<FilterSet> filterSetCombo;
	private HBox buttonBar;
	private Button newSetButton;
	private Button settingsButton;
	private Button deleteSetButton;
	private Button helpButton;
	private Button newFilterButton;
	private TableView<DisplayableFilter> filterTable;
	private FilterSet openFilterSet;
	private List<Category> categories;
	private Timestamp categoryTimestamp = new Timestamp(0);
	private List<TableColumn<DisplayableFilter, ?>> sortOrder = new ArrayList<TableColumn<DisplayableFilter, ?>>();
	
	public Tab getFiltersTab() throws Exception {
		filterTab = new Tab("Filters");
		baseVBox = new VBox();
		// This allows clicking anywhere on the basePane to trigger unfocused event for child nodes
		baseVBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{ focusOnBaseGrid();});

		filterTab.setContent(baseVBox);

		HBox filterSetBar = new HBox();
		filterSetBar.getStyleClass().add("top-button-bar");

		baseVBox.getChildren().add(filterSetBar);
		
		filterSetCombo = new ComboBox<FilterSet>();
		filterSetCombo.setPromptText("Choose a Filter Set");
		filterSetCombo.setButtonCell(new ListCell<FilterSet>() {
	        @Override
	        protected void updateItem(FilterSet item, boolean empty) {
	            super.updateItem(item, empty) ;
	            if (empty || item == null) {
	                setText("Choose a Filter Set");
	            } else {
	                setText(item.getName());
	            }
	        }
	    });
		filterSetBar.getChildren().add(filterSetCombo);
		filterSetCombo.setOnAction(getFilterSetComboHandler());

		newSetButton = new Button("New Filter Set");
		newSetButton.setOnAction(getNewSetButtonHandler());
		filterSetBar.getChildren().add(newSetButton);

		settingsButton = new Button("Settings");
		settingsButton.setOnAction(getSettingsButtonHandler());
		settingsButton.setDisable(true);
		filterSetBar.getChildren().add(settingsButton);

		helpButton = new Button("Help");
		helpButton.setOnAction(getHelpButtonHandler());
		filterSetBar.getChildren().add(helpButton);
		
		Region spacer = new Region();
		spacer.prefWidthProperty().bind(DisplayServiceImplJavaFX.getStage().widthProperty().subtract(600));
		spacer.minWidthProperty().bind(DisplayServiceImplJavaFX.getStage().widthProperty().subtract(600));
		filterSetBar.getChildren().add(spacer);

		deleteSetButton = new Button("Delete Set");
		deleteSetButton.setOnAction(getDeleteSetButtonHandler());
		filterSetBar.getChildren().add(deleteSetButton);
		deleteSetButton.setDisable(true);
		
		baseVBox.getChildren().add(new Separator());
		
		buttonBar = new HBox();

		buttonBar.setVisible(false);
		baseVBox.getChildren().add(buttonBar);
		
		newFilterButton = new Button("New Filter");
		newFilterButton.setOnAction(getNewFilterButtonHandler());
		buttonBar.getChildren().add(newFilterButton);
		buildFilterTable(openFilterSet);
		baseVBox.getChildren().add(filterTable);

		filterTab.selectedProperty().addListener(obs -> {
            if(filterTab.isSelected()) {
            	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
            	filterTable.prefHeightProperty().bind(DisplayServiceImplJavaFX.getTabPane().heightProperty().multiply(.85));
            	if (categoryTimestamp.before(Figures.categoryTimestamp)) {
            		CallResult result = loadCategories();
					if (result.isBad()) {
						ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Categories", result.getErrorMessage());
					}
            	}
           		if (openFilterSet == null) {
           			loadFilterSets();
           		}else {
					CallResult result = loadFilterSet(openFilterSet.getID());
					if (result.isBad()) {
						ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Filter Set", result.getErrorMessage());
					}
					deleteSetButton.setDisable(false);
					displayFilters(((FilterSet)result.getReturnedObject()).getFilters());
           		}
            }
    		if (filterSetCombo.getItems().size() == 0) {
    			ServiceFactory.getInstance().getDisplaySvc().setStatusHelp("Use the New Filter Set button to create a new filter set, for example Checking.");
    		}
	    });
		
		return filterTab;
	}
	
	private void focusOnBaseGrid() {
		baseVBox.requestFocus();
		if (filterTable != null) filterTable.getSelectionModel().clearSelection(); 
	}
	
	private EventHandler<ActionEvent> getDeleteSetButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    	ButtonType yesButton = new ButtonType("Yes");
		    	ButtonType noButton = new ButtonType("No");
		    	FxAlert confirmationAlert = new FxAlert(AlertType.CONFIRMATION, "Delete " + openFilterSet.getName() + " Filter Set", "This cannot be undone.  Are you sure you want to delete this filter set?", yesButton, noButton);
		        Optional<?> result = confirmationAlert.showAndWait();
		    	if (result.isPresent() && result.get().equals(yesButton)) {
		    		CallResult deleteResult = ServiceFactory.getInstance().getPersistenceSvc().deleteFilterSet(openFilterSet);
		    		if (deleteResult.isBad()){
		    			ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Deleting Filter Set", deleteResult.getErrorMessage());
		    			return;
		    		}
		    		openFilterSet = null;
		    		loadFilterSets();
		    	}
			}
		};
	}
	
	private EventHandler<ActionEvent> getHelpButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().displayHelp("FiltersTab.html");
			}
		};
	}

	@SuppressWarnings("unchecked")
	public CallResult loadFilterSets() {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getFilterSets();
		if (result.isBad()){
			ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Filter Sets", result.getErrorMessage());
			return result;
		}
		newFilterButton.setVisible(false);
		if (filterTable != null) filterTable.setVisible(false);
		filterSetCombo.getItems().clear();
		List<FilterSet> filterSets = (List<FilterSet>)result.getReturnedObject();
		if (filterSets.size() > 0) {
			for (FilterSet filterSet : filterSets) {
				filterSetCombo.getItems().add(filterSet);
			}
			if (openFilterSet != null) {
				filterSetCombo.getSelectionModel().select(openFilterSet);
			}else if (filterSets.size() == 1) {
				filterSetCombo.getSelectionModel().select(filterSets.get(0));
				openFilterSet = filterSetCombo.getSelectionModel().getSelectedItem();
				buttonBar.setVisible(true);
				displayFilters(openFilterSet.getFilters());
			}
		}
		filterSetCombo.requestLayout();
		return result;
	}
	
	public CallResult loadFilterSet(int filterSetID) {
		return ServiceFactory.getInstance().getPersistenceSvc().getFilterSet(filterSetID);
	}
	
	private EventHandler<ActionEvent> getFilterSetComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				if (filterSetCombo.getSelectionModel().getSelectedIndex() != -1 && filterSetCombo.getItems().size() > 0) {
					filterSetCombo.getStyleClass().add("selected-bold");
				}else {
					filterSetCombo.getStyleClass().remove("selected-bold");
					settingsButton.setDisable(true);
					deleteSetButton.setDisable(true);
				}
				buttonBar.setVisible(true);
				if (filterTable != null) filterTable.setVisible(false);
				if (filterSetCombo.getSelectionModel().getSelectedItem() != null) {
					openFilterSet = filterSetCombo.getSelectionModel().getSelectedItem();
					CallResult result = loadFilterSet(openFilterSet.getID());
					if (result.isBad()) {
						ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Filter Set", result.getErrorMessage());
					}
					openFilterSet = (FilterSet)result.getReturnedObject();
					settingsButton.setDisable(false);
					deleteSetButton.setDisable(false);
					displayFilters(openFilterSet.getFilters());
				}
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public void buildFilterTable(FilterSet filterSet) {
		filterTable = new EnhancedTableView<DisplayableFilter>();
		filterTable.setEditable(true);
		filterTable.setFixedCellSize(CELL_SIZE);
		filterTable.getSelectionModel().cellSelectionEnabledProperty().set(true);
		
		TableColumn<DisplayableFilter, Image> editColumn = new TableColumn<DisplayableFilter, Image>();
		editColumn.setCellValueFactory(new PropertyValueFactory<>("editIcon"));
		editColumn.setCellFactory(new Callback<TableColumn<DisplayableFilter, Image>, TableCell<DisplayableFilter, Image>>() {
            public TableCell<DisplayableFilter, Image> call(TableColumn<DisplayableFilter, Image> param) {
                final ImageView imageView = new ImageView();
                imageView.setFitHeight(14);
                imageView.setFitWidth(14);
                TableCell<DisplayableFilter, Image> cell = new TableCell<DisplayableFilter, Image>(){
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
                        if (cell.getIndex() >= filterTable.getItems().size()) {
                        	return;
                        }
                        DisplayableFilter filterSelected = filterTable.getSelectionModel().getSelectedItem();
    					FilterSettingsPane newFilterPane = new FilterSettingsPane(openFilterSet, filterSelected);
    					Scene scene = new Scene(newFilterPane.getNewFilterPane(), FilterSettingsPane.WIDTH, FilterSettingsPane.HEIGHT);
	    				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
	    				Stage stage = new Stage();
	    				stage.initModality(Modality.APPLICATION_MODAL);
	    				stage.setWidth(FilterSettingsPane.WIDTH);
	    				stage.setHeight(FilterSettingsPane.HEIGHT);
	    				stage.setScene(scene);
	    				stage.setTitle("Edit Filter");
	    				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
	    				DisplayServiceImplJavaFX.center(stage, scene);
	    				stage.setOnHidden((WindowEvent event1) -> {
	    			        if (!newFilterPane.isCanceled()) {
	    			    		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getFilters(openFilterSet.getID());
	    			    		if (result.isBad()) {
	    			    			ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Filters", result.getErrorMessage());
	    			    			return;
	    			    		}else {
	    			    			openFilterSet.setFilters((List<Filter>)result.getReturnedObject());
	    			    			displayFilters(openFilterSet.getFilters());
	    			    		}
	    			        }
	    			    });
	    				stage.show();
	    			}

                });
                cell.setTooltip(new Tooltip("Click to edit this filter"));
                return cell;
            }
        });
		editColumn.getStyleClass().add("center-aligned-column");
		editColumn.setStyle("-fx-alignment: CENTER;");
		editColumn.setPrefWidth(30);

		
		TableColumn<DisplayableFilter, Integer> sequenceColumn = new TableColumn<DisplayableFilter, Integer>("Sequence");
		sequenceColumn.setCellValueFactory(new PropertyValueFactory<>("sequence"));
		sequenceColumn.setEditable(true);
		sequenceColumn.setCellFactory(FilterSequenceAutoCommit.forTableColumn(new IntegerStringConverter()));
		sequenceColumn.setOnEditCommit( t -> {
        	int updatedFilterID =  ((DisplayableFilter) t.getTableView().getItems().get(t.getTablePosition().getRow())).getID();
        	int newSequenceValue = t.getNewValue();
           	CallResult result = ServiceFactory.getInstance().getMaintenanceSvc().resequenceFilters(openFilterSet, updatedFilterID, newSequenceValue);
           	List<Filter> filters = null;
        	if (result.isBad()) {
        		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Filter Update Failed", result.getErrorMessage());
        	}else {
        		result = ServiceFactory.getInstance().getPersistenceSvc().getFilters(openFilterSet.getID());
               	if (result.isBad()) {
               		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Filterset Load Failed", result.getErrorMessage());
               	}else {
               		filters = (List<Filter>)result.getReturnedObject();
               		openFilterSet.setFilters(filters);
               	}
        		displayFilters(filters);
        	}
	    });
		sequenceColumn.getStyleClass().add("center-aligned-column");
		sequenceColumn.setStyle("-fx-alignment: CENTER;");


		TableColumn<DisplayableFilter, String> fieldNameColumn = new TableColumn<DisplayableFilter, String>("Search Field");
		fieldNameColumn.setEditable(false);
		fieldNameColumn.setCellValueFactory(new PropertyValueFactory<>("fieldName"));
		fieldNameColumn.getStyleClass().add("left-aligned-column");
		fieldNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		fieldNameColumn.setPrefWidth(130);
		
		TableColumn<DisplayableFilter, String> expressionColumn = new TableColumn<DisplayableFilter, String>("Expression");
		expressionColumn.setEditable(false);
		expressionColumn.setCellValueFactory(new PropertyValueFactory<>("expression"));
		expressionColumn.getStyleClass().add("left-aligned-column");
		expressionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		expressionColumn.setPrefWidth(110);

		TableColumn<DisplayableFilter, String> searchValueColumn = new TableColumn<DisplayableFilter, String>("Search Value");
		searchValueColumn.setEditable(false);
		searchValueColumn.setCellValueFactory(new PropertyValueFactory<>("searchValue"));
		searchValueColumn.getStyleClass().add("left-aligned-column");
		searchValueColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		searchValueColumn.setPrefWidth(200);

		TableColumn<DisplayableFilter, String> resultActionColumn = new TableColumn<DisplayableFilter, String>("Result");
		resultActionColumn.setEditable(false);
		resultActionColumn.setCellValueFactory(new PropertyValueFactory<>("resultAction"));
		resultActionColumn.getStyleClass().add("left-aligned-column");
		resultActionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		resultActionColumn.setPrefWidth(160);

		TableColumn<DisplayableFilter, String> replacementValueColumn = new TableColumn<DisplayableFilter, String>("Replacement Value");
		replacementValueColumn.setEditable(false);
		replacementValueColumn.setCellValueFactory(new PropertyValueFactory<>("replacementValue"));
		replacementValueColumn.getStyleClass().add("left-aligned-column");
		replacementValueColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		replacementValueColumn.setPrefWidth(250);

		TableColumn<DisplayableFilter, String> categoryColumn = new TableColumn<DisplayableFilter, String>("Category");
		categoryColumn.setEditable(false);
		categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
		categoryColumn.getStyleClass().add("left-aligned-column");
		categoryColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		categoryColumn.setPrefWidth(140);
		
		TableColumn<DisplayableFilter, String> deductibleColumn = new TableColumn<DisplayableFilter, String>("Deductible");
		deductibleColumn.setEditable(false);
		deductibleColumn.setCellValueFactory(new PropertyValueFactory<>("deductible"));
		deductibleColumn.getStyleClass().add("center-aligned-column");
		deductibleColumn.setStyle("-fx-alignment: CENTER;");
		deductibleColumn.setPrefWidth(90);
		
		TableColumn<DisplayableFilter, Image> deleteColumn = new TableColumn<DisplayableFilter, Image>();
		deleteColumn.setCellValueFactory(new PropertyValueFactory<>("deleteIcon"));
		deleteColumn.setCellFactory(new Callback<TableColumn<DisplayableFilter, Image>, TableCell<DisplayableFilter, Image>>() {
            public TableCell<DisplayableFilter, Image> call(TableColumn<DisplayableFilter, Image> param) {
                final ImageView imageView = new ImageView();
                imageView.setFitHeight(14);
                imageView.setFitWidth(14);
                TableCell<DisplayableFilter, Image> cell = new TableCell<DisplayableFilter, Image>(){
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
                    	ButtonType yesButton = new ButtonType("Yes");
                    	ButtonType noButton = new ButtonType("No");
                    	FxAlert confirmationAlert = new FxAlert(AlertType.CONFIRMATION, "Delete Filter", "This cannot be undone.  Are you sure you want to delete this filter?", yesButton, noButton);
                        Optional<?> confirmResult = confirmationAlert.showAndWait();
                    	if (confirmResult.isPresent() && confirmResult.get().equals(yesButton)) {
                        	int index = filterTable.getSelectionModel().selectedIndexProperty().get();
                        	DisplayableFilter filterSelected = filterTable.getSelectionModel().getSelectedItem();
                        	CallResult result = ServiceFactory.getInstance().getPersistenceSvc().deleteFilter(filterSelected.getID());
                           	if (result.isBad()) {
                        		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Filter Delete Failed", result.getErrorMessage());
                        		return;
                           	}
                        	filterTable.getItems().remove(index);
                        	if (filterTable.getItems().size() == 0) {
                        		filterTable.setVisible(false);
                        	}else {
                            	filterTable.refresh();
                        	}
                    	}
                     }
                });
                cell.setTooltip(new Tooltip("Click to delete this filter"));
                return cell;
            }
        });
		deleteColumn.getStyleClass().add("center-aligned-column");
		deleteColumn.setStyle("-fx-alignment: CENTER;");
		deleteColumn.setPrefWidth(30);

		filterTable.getColumns().addAll(editColumn, sequenceColumn, fieldNameColumn, expressionColumn, searchValueColumn, 
				resultActionColumn, replacementValueColumn, categoryColumn, deductibleColumn, deleteColumn);
		double tableWidth = 0;
		for (TableColumn<DisplayableFilter, ?> column : filterTable.getColumns()) {
			tableWidth += column.getPrefWidth();
		}
		filterTable.setPrefWidth(tableWidth+20);
	}

	public void displayFilters(List<Filter> filters) {
		ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		newFilterButton.setVisible(true);
		if (filters.size() == 0) return;
		sortOrder.clear();
		if (filterTable.getSortOrder().size() > 0) {
			sortOrder.addAll(filterTable.getSortOrder());
		}
		List<DisplayableFilter> displayableFilters = new ArrayList<DisplayableFilter>();
		for (Filter filter : filters) {
			DisplayableFilter displayableFilter = new DisplayableFilter(filter);
			displayableFilter.setCategories(categories);
			displayableFilters.add(displayableFilter);
		}
		filterTable.setItems(FXCollections.observableArrayList(displayableFilters));
		// Preserve the sort order, if any
		if (sortOrder.size() > 0) {
	        Platform.runLater(new Runnable() {
	            @Override public void run() {
	            	filterTable.getSortOrder().clear();
	            	filterTable.getSortOrder().addAll(sortOrder);
	            	filterTable.sort();
	        		filterTable.refresh();
	            }
	        });
		}
		Platform.runLater(()->filterTable.refresh());
		filterTable.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	private CallResult loadCategories() {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		if (result.isGood()) {
			categories = (List<Category>)result.getReturnedObject();
		}
		return result;
	}
	
	private EventHandler<ActionEvent> getSettingsButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Scene scene = null;
				try {
					scene = new Scene(newFilterSetSettingsPane.getFilterSetSettingsPane(openFilterSet), FilterSetSettingsPane.WIDTH+20, FilterSetSettingsPane.HEIGHTH+20);
				} catch (Exception e) {
					Figures.logStackTrace(e);
				}
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Filter Set Settings");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!newFilterSetSettingsPane.isCanceled()) {
						loadFilterSets();
			        }
			    });
				stage.show();
			}
		};
	}

	private EventHandler<ActionEvent> getNewSetButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Scene scene = null;
				try {
					scene = new Scene(newFilterSetSettingsPane.getFilterSetSettingsPane(null), FilterSetSettingsPane.WIDTH+20, FilterSetSettingsPane.HEIGHTH+20);
				} catch (Exception e) {
					Figures.logStackTrace(e);
				}
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Create New Filter Set");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!newFilterSetSettingsPane.isCanceled()) {
						loadFilterSets();
			        }
			    });
				stage.show();
			}
		};
	}

	private EventHandler<ActionEvent> getNewFilterButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Scene scene = null;
				try {
					int newSequence = 1;
					if (openFilterSet.getFilters().size() > 0) {
						newSequence = openFilterSet.getFilters().get(openFilterSet.getFilters().size()-1).getSequence() + 1;
					}
					newFilterPane = new FilterSettingsPane(openFilterSet, newSequence);
					scene = new Scene(newFilterPane.getNewFilterPane(), FilterSettingsPane.WIDTH, FilterSettingsPane.HEIGHT);
				} catch (Exception e) {
					Figures.logStackTrace(e);
				}
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Create New Filter");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!newFilterPane.isCanceled()) {
						CallResult result = loadFilterSet(openFilterSet.getID());
						if (result.isBad()) {
							ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Filter Set", result.getErrorMessage());
						}
						openFilterSet = (FilterSet)result.getReturnedObject();
			        	displayFilters(openFilterSet.getFilters());
			        }
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
			}
		};
	}
	
	public void setDisable(boolean disable) {
		filterTab.setDisable(disable);
	}
	
	public void resetForNewFile() {
		if (buttonBar != null) {
			openFilterSet = null;
			buttonBar.setVisible(false);
			filterTable.setVisible(false);
		}
	}

}
