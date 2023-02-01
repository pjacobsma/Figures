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
import java.util.List;
import java.util.Optional;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

public class CategoriesTab {

	private Tab categoryTab;
	private VBox baseVBox;
	private NewCategoryPane newCategoryPane;
	private HBox buttonBar;
	private Button newCategoryButton;
	private Button helpButton;
	private EnhancedTableView<Category> categoryTable;
	private static final int CELL_SIZE = 25;
	private List<TableColumn<Category, ?>> sortOrder = new ArrayList<TableColumn<Category, ?>>();
	
	@SuppressWarnings("unchecked")
	public Tab getCategoriesTab() throws Exception {
		categoryTab = new Tab("Categories");
		baseVBox = new VBox();
		// This allows clicking anywhere on the baseVBox to trigger unfocused event for child nodes
		baseVBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{ baseVBox.requestFocus();});
		categoryTab.setContent(baseVBox);
		categoryTab.selectedProperty().addListener(obs -> {
            if(categoryTab.isSelected()) {
            	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
         		categoryTable.prefHeightProperty().bind(DisplayServiceImplJavaFX.getTabPane().heightProperty());
           		loadCategories();
            }
        });

		buttonBar = new HBox();

		baseVBox.getChildren().add(buttonBar);
		
		baseVBox.getChildren().add(new Separator());
		
		newCategoryButton = new Button("New Category");
		newCategoryButton.setOnAction(getNewCategoryButtonHandler());
		buttonBar.getChildren().add(newCategoryButton);

		helpButton = new Button("Help");
		helpButton.setOnAction(getHelpButtonHandler());
		buttonBar.getChildren().add(helpButton);

		HBox categoryTableBar = new HBox();
		baseVBox.getChildren().add(categoryTableBar);
		categoryTable = new EnhancedTableView<Category>();
		categoryTableBar.getChildren().add(categoryTable);
		categoryTable.setEditable(true);
		categoryTable.setFixedCellSize(CELL_SIZE);
		categoryTable.getSelectionModel().cellSelectionEnabledProperty().set(true);

		TableColumn<Category, Image> deleteColumn = new TableColumn<Category, Image>();
		deleteColumn.setCellValueFactory(new PropertyValueFactory<>("deleteIcon"));
		deleteColumn.setCellFactory(new Callback<TableColumn<Category, Image>, TableCell<Category, Image>>() {
            public TableCell<Category, Image> call(TableColumn<Category, Image> param) {
                final ImageView imageView = new ImageView();
                imageView.setFitHeight(15);
                imageView.setFitWidth(15);

                TableCell<Category, Image> cell = new TableCell<Category, Image>(){
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
            	        DisplayableCategory category = (DisplayableCategory) categoryTable.getSelectionModel().getSelectedItem();
            	        CallResult callResult = ServiceFactory.getInstance().getPersistenceSvc().checkCategoryUsage(category.getID());
                    	if (callResult.isBad()) {
                    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Deleting Category", callResult.getErrorMessage());
                			return;
                    	}
                    	Integer categoryUsageCount = (Integer)callResult.getReturnedObject();
                    	if (categoryUsageCount.intValue() > 0){
                    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Cannot Delete Category", "This category is currently in use and cannot be deleted.");
                    		return;
                    	}
                    	ButtonType yesButton = new ButtonType("Yes");
                    	ButtonType noButton = new ButtonType("No");
                    	FxAlert confirmationAlert = new FxAlert(AlertType.CONFIRMATION, "Delete Category", "This cannot be undone.  Are you sure you want to delete this category?", yesButton, noButton);
                        Optional<?> result = confirmationAlert.showAndWait();
                    	if (result.isPresent() && result.get().equals(yesButton)) {
                        	callResult = ServiceFactory.getInstance().getPersistenceSvc().deleteCategory(category.getID());
                        	if (callResult.isBad()) {
                        		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Deleting Category", callResult.getErrorMessage());
                    			return;
                        	}
                        	loadCategories();
                    	}
                     }
                });
                cell.setTooltip(new Tooltip("Click the X to delete this category"));
                return cell;
            }
        });
		deleteColumn.getStyleClass().add("center-aligned-column");
		deleteColumn.setStyle("-fx-alignment: CENTER;");

		TableColumn<Category, String> categoryColumn = new TableColumn<Category, String>("Category");
		categoryColumn.setEditable(true);
		categoryColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		categoryColumn.setCellFactory(TextFieldTableCellAutoCommit.forTableColumn(new DefaultStringConverter()));
		categoryColumn.setOnEditStart( t -> {
	        // Disable other tabs so this tab can commit edit changes before focus moves to other tab
			DisplayServiceImplJavaFX.accountsTab.setDisable(true);
			DisplayServiceImplJavaFX.filtersTab.setDisable(true);
	    });
		categoryColumn.setOnEditCommit( t -> {
	        int index = ((TableColumn.CellEditEvent<Category, String>) t).getTablePosition().getRow();
	        Category category = ((TableColumn.CellEditEvent<Category, String>) t).getTableView().getItems().get(index);
	        category.setName(((TableColumn.CellEditEvent<Category, String>) t).getNewValue());
	        CallResult result = ServiceFactory.getInstance().getPersistenceSvc().updateCategory(category);
			DisplayServiceImplJavaFX.accountsTab.setDisable(false);
			DisplayServiceImplJavaFX.filtersTab.setDisable(false);
			DisplayServiceImplJavaFX.getInstance().setStatusGood("Category updated");
	        if (result.isBad()) return;
			loadCategories();
	    });
		categoryColumn.setOnEditCancel( t -> {
			DisplayServiceImplJavaFX.accountsTab.setDisable(false);
			DisplayServiceImplJavaFX.filtersTab.setDisable(false);
	    });

		categoryColumn.getStyleClass().add("left-aligned-column");
		categoryColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		categoryColumn.setPrefWidth(300);
		
		categoryTable.getColumns().addAll(deleteColumn, categoryColumn);
		categoryTable.setMaxWidth(deleteColumn.getWidth()+categoryColumn.getWidth() - 20);
    	categoryTable.setVisible(false);

		return categoryTab;
	}

	private EventHandler<ActionEvent> getNewCategoryButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Scene scene = null;
				try {
					newCategoryPane = new NewCategoryPane();
					scene = new Scene(newCategoryPane.getNewCategoryPane(), 500, 200);
				} catch (Exception e) {
					Figures.logStackTrace(e);
				}
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Create New Category");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
				stage.setX(categoryTab.getTabPane().getLayoutX()+100);
				stage.setY(categoryTab.getTabPane().getLayoutY()+220);
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!newCategoryPane.isCanceled()) {
						loadCategories();
			        }
			    });
				stage.show();
			}
		};
	}

	private EventHandler<ActionEvent> getHelpButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().displayHelp("CategoriesTab.html");
			}
		};
	}

	public CallResult loadCategories() {
		sortOrder.clear();
		if (categoryTable.getSortOrder().size() > 0) {
			sortOrder.addAll(categoryTable.getSortOrder());
		}
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Categories", result.getErrorMessage());
			return result;
		}
		@SuppressWarnings("unchecked")
		List<Category> categories  = (List<Category>) result.getReturnedObject();
		List<DisplayableCategory> displayableCategories = new ArrayList<DisplayableCategory>();
		if (categories.size() != 0) {
			for (Category category : categories) {
				if (!category.equals(Category.NONE)) displayableCategories.add(new DisplayableCategory(category));
			}
			ObservableList<Category> displayCategories = FXCollections.observableArrayList(displayableCategories);
			categoryTable.setEditable(false);
			categoryTable.setItems(displayCategories);
			categoryTable.setEditable(true);
			categoryTable.setVisible(true);
			// Preserve the sort order, if any
			if (sortOrder.size() > 0) {
		        Platform.runLater(new Runnable() {
		            @Override public void run() {
		            	categoryTable.getSortOrder().clear();
		            	categoryTable.getSortOrder().addAll(sortOrder);
		            	categoryTable.sort();
		            }
		        });
			}
			Platform.runLater(()->categoryTable.refresh());
		}
		return result;
	}

}
