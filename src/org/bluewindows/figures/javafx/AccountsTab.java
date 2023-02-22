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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.AmountBetweenCriterion;
import org.bluewindows.figures.domain.AmountEqualsCriterion;
import org.bluewindows.figures.domain.AmountGreaterThanCriterion;
import org.bluewindows.figures.domain.AmountLessThanCriterion;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.domain.CategoryCriterion;
import org.bluewindows.figures.domain.ChecksExcludedCriterion;
import org.bluewindows.figures.domain.ChecksOnlyCriterion;
import org.bluewindows.figures.domain.DateRangeCriterion;
import org.bluewindows.figures.domain.DeductibleExcludedCriterion;
import org.bluewindows.figures.domain.DeductibleOnlyCriterion;
import org.bluewindows.figures.domain.DepositsCriterion;
import org.bluewindows.figures.domain.DescriptionCriterion;
import org.bluewindows.figures.domain.FilterSet;
import org.bluewindows.figures.domain.MemoCriterion;
import org.bluewindows.figures.domain.Money;
import org.bluewindows.figures.domain.SearchCriterion;
import org.bluewindows.figures.domain.Transaction;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.domain.UncategorizedExcludedCriterion;
import org.bluewindows.figures.domain.UncategorizedOnlyCriterion;
import org.bluewindows.figures.domain.UnfilteredCriterion;
import org.bluewindows.figures.domain.WithdrawalsCriterion;
import org.bluewindows.figures.enums.CheckInclusion;
import org.bluewindows.figures.enums.CursorType;
import org.bluewindows.figures.enums.Deductible;
import org.bluewindows.figures.enums.DeductibleInclusion;
import org.bluewindows.figures.enums.ExportType;
import org.bluewindows.figures.enums.FilterField;
import org.bluewindows.figures.enums.UncategorizedInclusion;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;
import org.controlsfx.control.CheckComboBox;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import net.sf.jasperreports.engine.JasperPrint;

public class AccountsTab {

	private VBox baseVBox;
	private HBox searchTransactionBar;
	private Tab accountTab;
	private AccountSettingsPane accountSettingsPane = new AccountSettingsPane();
	private ExportOptionsPane exportOptionsPane = new ExportOptionsPane();
	private PrintTitlePane printTitlePane = new PrintTitlePane();
	private NewFiltersOptionsPane newFiltersOptionsPane = new NewFiltersOptionsPane();
	private NewTransactionCategoryPane newCategorySplitPane = new NewTransactionCategoryPane();
	private ComboBox<Account> accountCombo;
	private HBox accountButtonBar;
	private Button newAccountButton;
	private Button importButton;
	private Button exportButton;
	private Button deleteAccountButton;
	private Button helpButton;
	private Button applyFiltersButton;
	private Button newFiltersButton;
	private Button printExportButton;
	private Button settingsButton;
	private ScrollPane searchPane;
	private boolean searchActive;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private ToggleGroup timePeriodGroup;
	private RadioButton thisMonthButton;
	private RadioButton lastMonthButton;
	private RadioButton thisYearButton;
	private RadioButton lastYearButton;
	private ToggleGroup amountGroup;
	private boolean toggleGroupActive = true;
	private ToggleButton equals;
	private ToggleButton lessThan;
	private ToggleButton greaterThan;
	private ToggleButton withdrawals;
	private ToggleButton deposits;
	private ToggleButton checks;
	private CheckInclusion checkInclusion = CheckInclusion.NONE;
	private TextField fromAmount;
	private TextField toAmount;
	private Text andText;
	private ToggleButton between;
	private TextField description;
	private TextField memo;
	private ToggleButton deductible;
	private DeductibleInclusion deductibleInclusion = DeductibleInclusion.NONE;
	private ToggleButton uncategorized;
	private UncategorizedInclusion uncategorizedInclusion = UncategorizedInclusion.NONE;
	private ToggleButton unfiltered;
	private CheckComboBox<Category> categoryCombo;
	private List<Category> categories;
	private Text totalsHeader;
	private GridPane withdrawalsGrid;
	private GridPane depositsGrid;
	private Text withdrawalsCountLabel;
	private TextField withdrawalsCount;
	private Text withdrawalsTotalLabel;
	private TextField withdrawalsTotal;
	private Text depositsCountLabel;
	private TextField depositsCount;
	private Text depositsTotalLabel;
	private TextField depositsTotal;
	private int totalsWidth = 160;
	private EnhancedTableView<DisplayableTransaction> transactionTable;
	private List<Transaction> transactions = new ArrayList<Transaction>();
	private NumberFormat numberFormat = NumberFormat.getInstance();
	private Account openAccount;
	private Timestamp accountTimestamp = new Timestamp(0);
	private Timestamp categoryTimestamp = new Timestamp(0);
	private Timestamp transactionTimestamp = new Timestamp(0);
	private List<TableColumn<DisplayableTransaction, ?>> sortOrder = new ArrayList<TableColumn<DisplayableTransaction, ?>>();
	
	public Tab getAccountsTab() throws Exception {
		accountTab = new Tab("Accounts");

		openAccount = null;
		baseVBox = new VBox();
		// This allows clicking anywhere on the baseVBox to trigger unfocused event for child nodes
		baseVBox.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{ focusOnBaseGrid();});
		accountTab.setContent(baseVBox);
		
		buildAccountButtonBar();
		
		searchTransactionBar = new HBox();
		baseVBox.getChildren().add(searchTransactionBar);
		searchTransactionBar.setVisible(false);
		
		buildSearchPanel();
		
		buildTransactionTable();
		
		accountTab.selectedProperty().addListener(obs -> {
            if(accountTab.isSelected()) {
            	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
            	searchTransactionBar.prefHeightProperty().bind(DisplayServiceImplJavaFX.getTabPane().heightProperty());
            	if (openAccount != null) {
                	refreshDisplay(openAccount);
            	}
            }
        });
		return accountTab;
	}
	
	public void resetForNewFile() {
		if (accountCombo != null) {
			openAccount = null;
			accountCombo.getSelectionModel().clearSelection();
			setAccountButtonsDisabled(true);
			setTransactionButtonsVisible(false);
			transactionTable.setVisible(false);
			transactionTable.getItems().clear();
			searchTransactionBar.setVisible(false);
			printExportButton.setDisable(false);
		}
	}
	
	private void focusOnBaseGrid() {
		baseVBox.requestFocus();
		if (transactionTable != null) transactionTable.getSelectionModel().clearSelection(); 
	}

	private void buildAccountButtonBar() {
		accountButtonBar = new HBox();
		baseVBox.getChildren().add(accountButtonBar);
		baseVBox.getChildren().add(new Separator());

		accountCombo = new ComboBox<Account>();
		accountCombo.setPromptText("Choose an Account");
		accountCombo.setOnAction(getAccountComboHandler());
		accountCombo.setButtonCell(new ListCell<Account>() {
	        @Override
	        protected void updateItem(Account item, boolean empty) {
	            super.updateItem(item, empty) ;
	            if (empty || item == null) {
	            	Platform.runLater(()->setText("Choose an Account"));
	            } else {
	            	Platform.runLater(()->setText(item.getName()));
	            }
	        }
	    });
		accountButtonBar.getChildren().add(accountCombo);

		newAccountButton = new Button("New Account");
		newAccountButton.setOnAction(getNewAccountButtonHandler());
		accountButtonBar.getChildren().add(newAccountButton);
		
		settingsButton = new Button("Account Settings");
		settingsButton.setMinWidth(140);
		settingsButton.setOnAction(getAccountSettingsButtonHandler());
		accountButtonBar.getChildren().add(settingsButton);

		importButton = new Button("Import");
		importButton.setMinWidth(70);
		importButton.setOnAction(getImportButtonHandler());
		importButton.setTooltip(new Tooltip("Import new transactions into this account"));
		importButton.setDisable(true);
		accountButtonBar.getChildren().add(importButton);
		
		exportButton = new Button("Export");
		exportButton.setMinWidth(70);
		exportButton.setOnAction(getExportButtonHandler());
		exportButton.setTooltip(new Tooltip("Export the transactions from this account"));
		accountButtonBar.getChildren().add(exportButton);

		helpButton = new Button("Help");
		helpButton.setMinWidth(70);
		helpButton.setOnAction(getHelpButtonHandler());
		accountButtonBar.getChildren().add(helpButton);
		
		Region spacer = new Region();
		spacer.prefWidthProperty().bind(DisplayServiceImplJavaFX.getStage().widthProperty().subtract(860));
		spacer.minWidthProperty().bind(DisplayServiceImplJavaFX.getStage().widthProperty().subtract(860));
		accountButtonBar.getChildren().add(spacer);
		
		deleteAccountButton = new Button("Delete Account");
		deleteAccountButton.setMinWidth(70);
		deleteAccountButton.setOnAction(getDeleteAccountButtonHandler());
		accountButtonBar.getChildren().add(deleteAccountButton);
		
		HBox transactionsButtonBar = new HBox();
		baseVBox.getChildren().add(transactionsButtonBar);

		applyFiltersButton = new Button("Apply Filters");
		applyFiltersButton.setMinWidth(110);
		applyFiltersButton.setOnAction(getApplyFiltersButtonHandler());
		applyFiltersButton.setTooltip(new Tooltip("Apply the filters for this account"));
		transactionsButtonBar.getChildren().add(applyFiltersButton);

		newFiltersButton = new Button("Make New Filters");
		newFiltersButton.setMinWidth(140);
		newFiltersButton.setOnAction(getNewFiltersButtonHandler());
		newFiltersButton.setTooltip(new Tooltip("Create new filters for unfiltered transactions"));
		transactionsButtonBar.getChildren().add(newFiltersButton);

		printExportButton = new Button("Print/Export");
		printExportButton.setMinWidth(60);
		printExportButton.setOnAction(getPrintExportButtonHandler());
		printExportButton.setTooltip(new Tooltip("Print/Export this list of transactions"));
		transactionsButtonBar.getChildren().add(printExportButton);
		setAccountButtonsDisabled(true);
		setTransactionButtonsVisible(false);
	}

	@SuppressWarnings({ "unchecked" })
	private void buildTransactionTable() {
		transactionTable = new EnhancedTableView<DisplayableTransaction>();
		searchTransactionBar.getChildren().add(transactionTable);
		transactionTable.setEditable(true);
		transactionTable.setPlaceholder(new Label("No transactions found."));
		transactionTable.setFixedCellSize(Region.USE_COMPUTED_SIZE);
		transactionTable.getSelectionModel().setCellSelectionEnabled(true);
		// Enable cell editing with single mouse click
		transactionTable.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
			TablePosition<DisplayableTransaction, ?> focusedCellPos = transactionTable.getFocusModel().getFocusedCell();
		    if (transactionTable.getEditingCell() == null) {
		    	transactionTable.edit(focusedCellPos.getRow(), focusedCellPos.getTableColumn());
		    }
		});
		// Enable copy to clipboard with Crtl-C
		transactionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		transactionTable.setOnKeyPressed(e -> {
			if (e.isControlDown() && e.getCode() == KeyCode.C) {
				StringBuilder buf = new StringBuilder();
				for (TablePosition<DisplayableTransaction,?> pos: transactionTable.getSelectionModel().getSelectedCells()) {
					Object cell = transactionTable.getColumns().get(pos.getColumn()).getCellData(pos.getRow());
					if (cell != null) {
						if (buf.length() > 0) {
							buf.append('\t');
						}
						buf.append(cell);
					}
				}
				if (buf.length() > 0) {
					ClipboardContent cbc = new ClipboardContent();
					cbc.putString(buf.toString());
					Clipboard.getSystemClipboard().setContent(cbc);
				}
			}
		});
		// Allows clicking on empty cell to unfocus selection
		transactionTable.setRowFactory(transactionTable -> {
			TableRow<DisplayableTransaction> row = new TableRow<>();
			row.setOnMouseClicked(getUnfocusHandler(row));
			return row;
		});	
		
		TableColumn<DisplayableTransaction, Integer> numberColumn = new TableColumn<DisplayableTransaction, Integer>("Number");
		numberColumn.setPrefWidth(70);
		numberColumn.setEditable(false);
		numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
		numberColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

		TableColumn<DisplayableTransaction, SortableTransactionDate> dateColumn = new TableColumn<DisplayableTransaction, SortableTransactionDate>("Date");
		dateColumn.setPrefWidth(90);
		dateColumn.setEditable(false);
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("sortableDate"));
		dateColumn.setStyle("-fx-alignment: CENTER;");

		TableColumn<DisplayableTransaction, String> descriptionColumn = new TableColumn<DisplayableTransaction, String>("Description");
		descriptionColumn.setPrefWidth(190);
		descriptionColumn.setEditable(true);
		descriptionColumn.getStyleClass().add("left-aligned-column");
		descriptionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
		descriptionColumn.setCellFactory (col -> {
			TextFieldTableCellAutoCommit<DisplayableTransaction, String> cell = new TextFieldTableCellAutoCommit<DisplayableTransaction, String>(new DefaultStringConverter()) {
		        @Override
		        public void updateItem(String item, boolean empty) {
		            super.updateItem(item, empty);
		            if (item != null) {
		                   Text text = new Text(item);
		                   text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
		                   this.setPrefHeight(text.getLayoutBounds().getHeight()+2);
		                   this.setGraphic(text);
		            }
		        }
		    };
		    return cell;
		});
		descriptionColumn.setOnEditCommit( t -> {
	        int index = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getTablePosition().getRow();
			String description = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getNewValue();
	        Transaction transaction = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getTableView().getItems().get(index);
			if (!description.equals(transaction.getDescription())) {
				transaction.setDescription(description);
				transaction.setUserChangedDesc(true);
		        CallResult result = ServiceFactory.getInstance().getPersistenceSvc().updateTransaction(transaction);
		        if (result.isBad()) {
            		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Updating Description", result.getErrorMessage());
		        	return; 
		        }
		        result = ServiceFactory.getInstance().getMaintenanceSvc().filterTransaction(transaction, openAccount);
		        if (result.isBad()) {
            		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Filtering Transaction", result.getErrorMessage());
		        	return; 
		        }
		        loadAndDisplayTransactions(openAccount);
			}
	    });
		
		TableColumn<DisplayableTransaction, Money> amountColumn = new TableColumn<DisplayableTransaction, Money>("Amount");
		amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
		amountColumn.setCellFactory(new Callback<TableColumn<DisplayableTransaction, Money>, TableCell<DisplayableTransaction, Money>>() {
			@Override
			public TableCell<DisplayableTransaction, Money> call(TableColumn<DisplayableTransaction, Money> param) {
				return new TableCell<DisplayableTransaction, Money>() {
					@Override
					public void updateItem(Money item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty()) {
							if (item.isCredit()) {
								this.setTextFill(Color.GREEN);
							}else {
								this.setTextFill(Color.BLACK);
							}
							setText(item.toString());
						}
					}
				};
			}
		});
		amountColumn.getStyleClass().add("right-aligned-column");
		amountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
	    amountColumn.setPrefWidth(100);
		amountColumn.setEditable(false);
		// Non-credits are stored as negative values.  Use absolute values here so we can sort the transaction table on amounts correctly.
		amountColumn.setComparator((v1, v2) -> v1.getValue().abs().compareTo(v2.getValue().abs()));
		
		TableColumn<DisplayableTransaction, String> categoryColumn = new TableColumn<DisplayableTransaction, String>("Categories");
		categoryColumn.setPrefWidth(240);
		categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryList"));
		categoryColumn.setCellFactory(new Callback<TableColumn<DisplayableTransaction, String>, TableCell<DisplayableTransaction, String>>() {
		    @SuppressWarnings("rawtypes")
			@Override
		    public TableCell call(TableColumn<DisplayableTransaction, String> p) {
		        TableCell<?, ?> cell = TableColumn.DEFAULT_CELL_FACTORY.call(p);
		        cell.setOnMouseClicked(getCategoryCellHandler(cell));
		        return cell;
		    }
		});	
		categoryColumn.getStyleClass().add("left-aligned-column");
		categoryColumn.setStyle("-fx-alignment: CENTER-LEFT;");
		@SuppressWarnings("rawtypes")
		ObservableList categoryNameList = FXCollections.observableArrayList();
		CallResult categoriesResult = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		if (categoriesResult.isGood()) {
			categories = (List<Category>)categoriesResult.getReturnedObject();
			for (Category category : categories) {
				categoryNameList.add(category.getName());
			}
		}else {
			categories = new ArrayList<Category>();
		}

		TableColumn<DisplayableTransaction, String> memoColumn = new TableColumn<DisplayableTransaction, String>("Memo");
		memoColumn.setPrefWidth(170);
		memoColumn.setEditable(true);
		memoColumn.setCellValueFactory(new PropertyValueFactory<>("memo"));
		memoColumn.setCellFactory (col -> {
			TextFieldTableCellAutoCommit<DisplayableTransaction, String> cell = new TextFieldTableCellAutoCommit<DisplayableTransaction, String>(new DefaultStringConverter()) {
		        @Override
		        public void updateItem(String item, boolean empty) {
		            super.updateItem(item, empty);
		            if (item != null) {
		                   Text text = new Text(item);
		                   text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
		                   this.setPrefHeight(text.getLayoutBounds().getHeight()+2);
		                   this.setGraphic(text);
		            }
		        }
		    };
		    return cell;
		});
		memoColumn.setOnEditCommit( t -> {
	        int index = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getTablePosition().getRow();
			String memo = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getNewValue();
	        Transaction transaction = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getTableView().getItems().get(index);
			if (!memo.equals(transaction.getMemo())) {
				transaction.setMemo(memo);
				transaction.setUserChangedMemo(true);
		        CallResult result = ServiceFactory.getInstance().getPersistenceSvc().updateTransaction(transaction);
		        if (result.isBad()) {
            		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Updating Memo", result.getErrorMessage());
		        	return; 
		        }
		        transactionTable.refresh();
			}
	    });
		memoColumn.getStyleClass().add("left-aligned-column");
		memoColumn.setStyle("-fx-alignment: CENTER-LEFT;");

		TableColumn<DisplayableTransaction, String> deductibleColumn = new TableColumn<DisplayableTransaction, String>("Deductible");
		deductibleColumn.setPrefWidth(90);
		deductibleColumn.setCellValueFactory(new PropertyValueFactory<>("deductibleLabel"));
		deductibleColumn.getStyleClass().add("center-aligned-column");
		deductibleColumn.setStyle("-fx-alignment: CENTER;");
		ObservableList<String> deductibleList = FXCollections.observableArrayList();
		for (Deductible deductible : Deductible.values()) {
			deductibleList.add(deductible.toString());
		}
		deductibleColumn.setCellFactory(ComboBoxTableCell.forTableColumn(deductibleList));
		deductibleColumn.setOnEditCommit( t -> {
	        int index = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getTablePosition().getRow();
	        DisplayableTransaction transaction = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getTableView().getItems().get(index);
	        boolean deductible = ((TableColumn.CellEditEvent<DisplayableTransaction, String>) t).getNewValue().equals(Deductible.YES.toString());
			if (deductible != transaction.isDeductible()) {
				transaction.setDeductible(deductible);
				transaction.setUserChangedDeductible(true);
		        CallResult result = ServiceFactory.getInstance().getPersistenceSvc().updateTransaction(transaction);
		        if (result.isBad()) {
            		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Updating Category", result.getErrorMessage());
		        	return; 
		        }
			}
	    });

		
		TableColumn<DisplayableTransaction, Money> balanceColumn = new TableColumn<DisplayableTransaction, Money>("Balance");
		balanceColumn.setPrefWidth(100);
		balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
		balanceColumn.setCellFactory(new Callback<TableColumn<DisplayableTransaction, Money>, TableCell<DisplayableTransaction, Money>>() {
			@Override
			public TableCell<DisplayableTransaction, Money> call(TableColumn<DisplayableTransaction, Money> param) {
				return new TableCell<DisplayableTransaction, Money>() {
					@Override
					public void updateItem(Money item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty()) {
							setText(item.toStringNegative());
						}
					}
				};
			}
		});
		balanceColumn.getStyleClass().add("right-aligned-column");
		balanceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

		transactionTable.getColumns().addAll(numberColumn, dateColumn, descriptionColumn, amountColumn, 
			categoryColumn, memoColumn, deductibleColumn, balanceColumn);
		double tableWidth = 0;
		for (TableColumn<DisplayableTransaction, ?> column : transactionTable.getColumns()) {
			tableWidth += column.getWidth();
		}
		transactionTable.setPrefWidth(tableWidth + 40);
	}
	
	private void buildSearchPanel() {
		searchPane = new ScrollPane();
		searchPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		searchTransactionBar.getChildren().add(searchPane);
		VBox searchVBox = new VBox();
		searchVBox.getStyleClass().add("unpadded");
		searchVBox.setPrefWidth(278);
		searchPane.setPrefWidth(300);
		searchPane.setMinWidth(300);
		searchPane.setContent(searchVBox);
		
		HBox searchBar = new HBox();
		searchVBox.getChildren().add(searchBar);
		
		Button searchButton = new Button("Search");
		searchButton.setOnAction(getSearchButtonHandler());
		searchBar.getChildren().add(searchButton);

		Button resetButton = new Button("Reset");
		resetButton.setOnAction(getResetButtonHandler());
		searchBar.getChildren().add(resetButton);

		searchVBox.getChildren().add(new Separator());

		GridPane timeBox = new GridPane();
		timeBox.getStyleClass().add("paddedGrid");
		searchVBox.getChildren().add(timeBox);

		timeBox.add(new Text(" Start Date"), 0, 0);
		timeBox.add(new Text(" End Date"), 1, 0);

		startDatePicker = getDatePicker();
		timeBox.add(startDatePicker, 0, 1);
		
		endDatePicker = getDatePicker();
		timeBox.add(endDatePicker, 1, 1);
		
		timePeriodGroup = new ToggleGroup();
		timePeriodGroup.selectedToggleProperty().addListener(getTimePeriodChangeListener()); 
		
		thisMonthButton = new RadioButton("This Month");
		timeBox.add(thisMonthButton, 0, 2);
		thisMonthButton.setToggleGroup(timePeriodGroup);
		RadioButtonToggleHandler thisMonthToggler = new RadioButtonToggleHandler(thisMonthButton);
		thisMonthButton.setOnMousePressed(thisMonthToggler.getMousePressed());
		thisMonthButton.setOnMouseReleased(thisMonthToggler.getMouseReleased());
		
		lastMonthButton = new RadioButton("Last Month");
		timeBox.add(lastMonthButton, 0, 3);
		lastMonthButton.setToggleGroup(timePeriodGroup);
		RadioButtonToggleHandler lastMonthToggler = new RadioButtonToggleHandler(lastMonthButton);
		lastMonthButton.setOnMousePressed(lastMonthToggler.getMousePressed());
		lastMonthButton.setOnMouseReleased(lastMonthToggler.getMouseReleased());
		
		thisYearButton = new RadioButton("This Year");
		timeBox.add(thisYearButton, 1, 2);
		thisYearButton.setToggleGroup(timePeriodGroup);
		RadioButtonToggleHandler thisYearToggler = new RadioButtonToggleHandler(thisYearButton);
		thisYearButton.setOnMousePressed(thisYearToggler.getMousePressed());
		thisYearButton.setOnMouseReleased(thisYearToggler.getMouseReleased());

		lastYearButton = new RadioButton("Last Year");
		timeBox.add(lastYearButton, 1, 3);
		lastYearButton.setToggleGroup(timePeriodGroup);
		RadioButtonToggleHandler lastYearToggler = new RadioButtonToggleHandler(lastYearButton);
		lastYearButton.setOnMousePressed(lastYearToggler.getMousePressed());
		lastYearButton.setOnMouseReleased(lastYearToggler.getMouseReleased());

		searchVBox.getChildren().add(new Separator());

		HBox amountControlBar = new HBox();
		searchVBox.getChildren().add(amountControlBar);
		
		amountControlBar.getChildren().add(new Text("Amount"));
		
		amountGroup = new ToggleGroup();
		amountGroup.selectedToggleProperty().addListener(getAmountGroupChangeListener());
		
		equals = new ToggleButton("=");
		equals.setToggleGroup(amountGroup);
		amountControlBar.getChildren().add(equals);
		
		lessThan = new ToggleButton("<");
		lessThan.setToggleGroup(amountGroup);
		amountControlBar.getChildren().add(lessThan);
		
		greaterThan = new ToggleButton(">");
		greaterThan.setToggleGroup(amountGroup);
		amountControlBar.getChildren().add(greaterThan);

		between = new ToggleButton("Between");
		between.setToggleGroup(amountGroup);
		amountControlBar.getChildren().add(between);
		
		HBox amountBar = new HBox();
		searchVBox.getChildren().add(amountBar);
		fromAmount = new TextField();
		fromAmount.setPrefWidth(90);
		fromAmount.setMaxWidth(90);
		amountBar.getChildren().add(fromAmount);
		
		andText = new Text("And");
		andText.managedProperty().bind(andText.visibleProperty());
		andText.setVisible(false);
		amountBar.getChildren().add(andText);

		toAmount = new TextField();
		toAmount.setPrefWidth(90);
		toAmount.setMaxWidth(90);
		toAmount.managedProperty().bind(toAmount.visibleProperty());
		toAmount.setVisible(false);
		amountBar.getChildren().add(toAmount);

		searchVBox.getChildren().add(new Separator());
		
		VBox descriptionBox = new VBox();
		searchVBox.getChildren().add(descriptionBox);

		descriptionBox.getChildren().add(new Text("Description"));
		
		description = new TextField();
		description.setPrefWidth(200);
		descriptionBox.getChildren().add(description);

		VBox memoBox = new VBox();
		searchVBox.getChildren().add(memoBox);
		memoBox.getChildren().add(new Text("Memo"));
		
		memo = new TextField();
		memo.setPrefWidth(200);
		memoBox.getChildren().add(memo);

		searchVBox.getChildren().add(new Separator());
		
		HBox transactionTypeBox = new HBox();
		searchVBox.getChildren().add(transactionTypeBox);
		transactionTypeBox.getChildren().add(new Text("Transaction Type"));
		
		HBox tranTypeBar = new HBox();
		searchVBox.getChildren().add(tranTypeBar);

		ToggleGroup tranTypeGroup = new ToggleGroup();
		withdrawals = new ToggleButton("Withdrawals");
		withdrawals.setToggleGroup(tranTypeGroup);
		tranTypeBar.getChildren().add(withdrawals);

		deposits = new ToggleButton("Deposits");
		deposits.setToggleGroup(tranTypeGroup);
		tranTypeBar.getChildren().add(deposits);

		checks = new ToggleButton("Checks");
		checks.setOnAction((ActionEvent e) -> {
		    if (checkInclusion.equals(CheckInclusion.EXCLUDED)) {
		    	checkInclusion = CheckInclusion.NONE;
		    	checks.setSelected(false);
		    	checks.getStyleClass().removeAll("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    }else if (checkInclusion.equals(CheckInclusion.INCLUDED)){
		    	checkInclusion = CheckInclusion.EXCLUDED;
		    	checks.setSelected(true);
		    	checks.getStyleClass().add("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().setStatusHelp("Checks are excluded.");
		    }else {
		    	checkInclusion = CheckInclusion.INCLUDED;
		    	checks.setSelected(true);
		    	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    }
		});
		tranTypeBar.getChildren().add(checks);

		HBox deductibleUncategorizedBar = new HBox();
		searchVBox.getChildren().add(deductibleUncategorizedBar);
		
		uncategorized = new ToggleButton("Uncategorized");
		uncategorized.setOnAction((ActionEvent e) -> {
		    if (uncategorizedInclusion.equals(UncategorizedInclusion.EXCLUDED)) {
		    	uncategorizedInclusion = UncategorizedInclusion.NONE;
		    	uncategorized.setSelected(false);
		    	uncategorized.getStyleClass().removeAll("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    }else if (uncategorizedInclusion.equals(UncategorizedInclusion.INCLUDED)){
		    	uncategorizedInclusion = UncategorizedInclusion.EXCLUDED;
		    	uncategorized.setSelected(true);
		    	uncategorized.getStyleClass().add("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().setStatusHelp("Uncategorized transactions are excluded.");
		    }else {
		    	uncategorizedInclusion = UncategorizedInclusion.INCLUDED;
		    	uncategorized.setSelected(true);
		    	uncategorized.getStyleClass().removeAll("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    }
		});
		deductibleUncategorizedBar.getChildren().add(uncategorized);

		deductible = new ToggleButton("Deductible");
		deductible.setOnAction((ActionEvent e) -> {
		    if (deductibleInclusion.equals(DeductibleInclusion.EXCLUDED)) {
		    	deductibleInclusion = DeductibleInclusion.NONE;
		    	deductible.setSelected(false);
		    	deductible.getStyleClass().removeAll("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    }else if (deductibleInclusion.equals(DeductibleInclusion.INCLUDED)){
		    	deductibleInclusion = DeductibleInclusion.EXCLUDED;
		    	deductible.setSelected(true);
		    	deductible.getStyleClass().add("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().setStatusHelp("Deductible transactions are excluded.");
		    }else {
		    	deductibleInclusion = DeductibleInclusion.INCLUDED;
		    	deductible.setSelected(true);
		    	deductible.getStyleClass().removeAll("red-text");
		    	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		    }
		});
		deductibleUncategorizedBar.getChildren().add(deductible);
		
		searchVBox.getChildren().add(new Separator());
		
		HBox categoryBar = new HBox();
		searchVBox.getChildren().add(categoryBar);
		categoryCombo = new CheckComboBox<Category>();
		categoryCombo.setPrefWidth(280);
		categoryCombo.setMaxWidth(280);
		categoryCombo.setTitle("Search by Categories");
		categoryCombo.setShowCheckedCount(true);
		categoryCombo.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Category>() {
		    @Override
		    public void onChanged(ListChangeListener.Change<? extends Category> c) {
		    	if (categoryCombo.getCheckModel().getCheckedItems().size() > 0) {
			    	categoryCombo.setStyle("-fx-font-weight: bold;");
		    	}else {
			    	categoryCombo.setStyle("-fx-font-weight: normal;");
		    	}
		    }
		});       		
		categoryBar.getChildren().add(categoryCombo);
		loadCategories();
		
		searchVBox.getChildren().add(new Separator());
		
		HBox unfilteredBar = new HBox();
		searchVBox.getChildren().add(unfilteredBar);
		
		unfiltered = new ToggleButton("Show Unfiltered");
		unfiltered.setOnAction((ActionEvent e) -> {
		    if (!unfiltered.isSelected()) { // Reload transactions to undo previous display of unfiltered data
		    	loadTransactions((Account) accountCombo.getSelectionModel().getSelectedItem());
		    }
		});
		unfilteredBar.getChildren().add(unfiltered);

		searchVBox.getChildren().add(new Separator());
		
		HBox searchResultTotalsBox = new HBox();
		searchVBox.getChildren().add(searchResultTotalsBox);
		totalsHeader = new Text("Search Result Totals");
		searchResultTotalsBox.getChildren().add(totalsHeader);
		
		searchVBox.getChildren().add(new Separator());
		
		withdrawalsGrid = new GridPane();
		ColumnConstraints cc = new ColumnConstraints();
        cc.setMinWidth(150);
        withdrawalsGrid.getColumnConstraints().add(cc);		
        searchVBox.getChildren().add(withdrawalsGrid);
		
		withdrawalsCountLabel = new Text("Withdrawals Count:");
		withdrawalsGrid.add(withdrawalsCountLabel, 0, 0);

		withdrawalsCount = new TextField("");
		withdrawalsCount.setEditable(false);
		withdrawalsCount.getStyleClass().remove("text-field");
		withdrawalsCount.getStyleClass().add("transparentTextField");
		withdrawalsCount.setPrefWidth(totalsWidth);
		withdrawalsCount.setAlignment(Pos.BASELINE_RIGHT);
		withdrawalsGrid.add(withdrawalsCount, 1, 0);

		withdrawalsTotalLabel = new Text("Withdrawals Total:");
		withdrawalsGrid.add(withdrawalsTotalLabel, 0, 1);

		withdrawalsTotal = new TextField("");
		withdrawalsTotal.setEditable(false);
		withdrawalsTotal.getStyleClass().remove("text-field");
		withdrawalsTotal.getStyleClass().add("transparentTextField");
		withdrawalsTotal.setPrefWidth(totalsWidth);
		withdrawalsTotal.setAlignment(Pos.BASELINE_RIGHT);

		withdrawalsGrid.add(withdrawalsTotal, 1, 1);
		
		depositsGrid = new GridPane();
		depositsGrid.getColumnConstraints().add(cc);		
		depositsGrid.managedProperty().bind(depositsGrid.visibleProperty());
		searchVBox.getChildren().add(depositsGrid);

		depositsCountLabel = new Text("Deposits Count:");
		depositsGrid.add(depositsCountLabel, 0, 0);

		depositsCount = new TextField("");
		depositsCount.setEditable(false);
		depositsCount.getStyleClass().remove("text-field");
		depositsCount.getStyleClass().add("transparentTextField");
		depositsCount.setPrefWidth(totalsWidth);
		depositsCount.setAlignment(Pos.BASELINE_RIGHT);
		depositsGrid.add(depositsCount, 1, 0);

		depositsTotalLabel = new Text("Deposits Total:");
		depositsGrid.add(depositsTotalLabel, 0, 1);

		depositsTotal = new TextField("");
		depositsTotal.setEditable(false);
		depositsTotal.getStyleClass().remove("text-field");
		depositsTotal.getStyleClass().add("transparentTextField");
		depositsTotal.setPrefWidth(totalsWidth);
		depositsTotal.setAlignment(Pos.BASELINE_RIGHT);
		depositsGrid.add(depositsTotal, 1, 1);
		
	}
	
	@SuppressWarnings("unchecked")
	private void loadCategories() {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		if (result.isBad()) {
        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
        	return;
		}
		List<Category> loadedCategories = (List<Category>)result.getReturnedObject();
		// Save list of selected categories
		List<Integer> selectedCategoryIDs = new ArrayList<Integer>();
		for (Category category : categoryCombo.getCheckModel().getCheckedItems()) {
			selectedCategoryIDs.add(category.getID());
		}
		categoryCombo.getCheckModel().clearChecks();
		categoryCombo.getItems().clear();
		for (Category category : loadedCategories) {
			categoryCombo.getItems().add(new Category(category.getID(), category.getName()));
		}
		// Re-select categories
		for (Integer categoryID : selectedCategoryIDs) {
			for (Category category : loadedCategories) {
				if (category.getID() == categoryID.intValue()) {
					categoryCombo.getCheckModel().check(category);				
				}
			}
		}
		categoryTimestamp = new Timestamp(System.currentTimeMillis());
	}
   
	private EventHandler<MouseEvent> getCategoryCellHandler(TableCell<?, ?> cell) {
		return new EventHandler<MouseEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public void handle(MouseEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				if (cell.emptyProperty().getValue()) { // For click on cell beyond end of data table
					transactionTable.getSelectionModel().clearSelection();
		        	return; 
				};
		        Transaction transaction = transactionTable.getSelectionModel().getSelectedItem();
		        CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getCategories();
		        if (result.isBad()){
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
		        	return;
		        }
				Scene scene = new Scene(newCategorySplitPane.getNewTransactionCategoryPane(transaction, (List<Category>)result.getReturnedObject()),
					540, 600);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				scene.focusOwnerProperty().addListener((prop, oldNode, newNode) -> scene.getWindow().requestFocus());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Assign Transaction Categories");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!newCategorySplitPane.isCanceled()) {
						loadAndDisplayTransactions(openAccount);
			        }
					Platform.runLater(()->baseVBox.requestFocus());
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
				// Set focus on base grid rather than any particular control
				Platform.runLater(()->newCategorySplitPane.getBasePane().requestFocus());
			}
		};
	}
	
	// This handler removes the focus from the last selected cell when the user 
	// clicks on an empty row.  It allows the user to click away from a cell 
	// even when they click on an empty row
	@SuppressWarnings("rawtypes")
	private EventHandler<MouseEvent> getUnfocusHandler(TableRow row) {
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if (row.emptyProperty().getValue()) { 
					transactionTable.getSelectionModel().clearSelection();
		        	return; 
				};
			}
		};
	}

	private EventHandler<ActionEvent> getAccountComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (accountCombo.getSelectionModel().getSelectedIndex() != -1 && accountCombo.getItems().size() > 0) {
					openAccount = (Account) accountCombo.getSelectionModel().getSelectedItem();
					Platform.runLater(()->accountCombo.getStyleClass().add("selected-bold"));
					Platform.runLater(()->importButton.setDisable(false));
				}else {
					Platform.runLater(()->accountCombo.getStyleClass().remove("selected-bold"));
					Platform.runLater(()->importButton.setDisable(true));
				}
				searchActive = false;
				if (openAccount != null) {
					transactionTimestamp = new Timestamp(0);
					refreshDisplay(openAccount);
					resetSearchControls();
				}else {
					setAccountButtonsDisabled(true);
				}
			}
		};
	}
	
	private EventHandler<ActionEvent> getNewAccountButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				openAccount = accountCombo.getSelectionModel().getSelectedItem();
				Scene scene = new Scene(accountSettingsPane.getAccountSettingsPane(null), 400, 300);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Create New Account");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!accountSettingsPane.isCanceled()) {
						loadAccounts();
			        }
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
				// Set focus on base grid rather than any particular control
				Platform.runLater(()->accountSettingsPane.getBasePane().requestFocus());
			}
		};
	}

	private EventHandler<ActionEvent> getAccountSettingsButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Scene scene = new Scene(accountSettingsPane.getAccountSettingsPane(openAccount), 400, 400);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Account Settings");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!accountSettingsPane.isCanceled()) {
			        	loadAccounts();
			        	if (openAccount != null) refreshDisplay(openAccount);
						resetSearchControls();
			        }
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
				// Set focus on base grid rather than any particular control
				Platform.runLater(()->accountSettingsPane.getBasePane().requestFocus());
			}
		};
	}

	private EventHandler<ActionEvent> getNewFiltersButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				if (openAccount.getFilterSet().getID() == 0) {
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad("No filter set has been selected for this account.  Use the Account Settings button to select a filter set.");
		        	return;
				}
				CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getFilterSet(openAccount.getFilterSet().getID());
				if (result.isBad()) {
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
				}
				openAccount.setFilterSet((FilterSet)result.getReturnedObject());
				Scene scene = new Scene(newFiltersOptionsPane.getNewFiltersOptionsPane(openAccount.getFilterSet()), 
					NewFiltersOptionsPane.WIDTH, NewFiltersOptionsPane.HEIGHT);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("New Filters Options");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!newFiltersOptionsPane.isCanceled()) {
			        	defineNewFilters(newFiltersOptionsPane.isDescriptionsSelected(), newFiltersOptionsPane.isNewTransactionsSelected(),
			        			newFiltersOptionsPane.isDepositsOnlySelected(), newFiltersOptionsPane.isWithdrawalsOnlySelected());
			        }
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.setOnCloseRequest(event2 -> {
			        stage.close();
			    });
				stage.show();
			}
		};
	}
	
	private EventHandler<ActionEvent> getPrintExportButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				CallResult result = getHeaders();
				if (result.isBad()) return;
				String title = (String)result.getReturnedObject();
				Task<CallResult> task = getPrintTask(title);
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
				    public void handle(WorkerStateEvent t) {
						ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
				        CallResult result = task.getValue();
				        if (result.isGood()) {
							Stage stage = new Stage();
							stage.initModality(Modality.APPLICATION_MODAL);
							stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
							stage.setTitle("Print/Export");
							stage.setWidth(DisplayServiceImplJavaFX.getStage().getWidth()-10);
							stage.setHeight(DisplayServiceImplJavaFX.getStage().getHeight()-10);
							stage.setX(DisplayServiceImplJavaFX.getStage().getX()+10);
							stage.setY(DisplayServiceImplJavaFX.getStage().getY()+10);
							JasperPrint jasperPrint = (JasperPrint)result.getReturnedObject();
							JasperFXport jasperExport = new JasperFXport(jasperPrint, stage, (float)1.48, DisplayServiceImplJavaFX.getCss());
							try {
								jasperExport.show();
							} catch (IOException e) {
								Figures.logStackTrace(e);
					    		ServiceFactory.getInstance().getDisplaySvc().setStatusBad("Error creating report: " + e.getLocalizedMessage());
							}
				        }else {
				    		ServiceFactory.getInstance().getDisplaySvc().setStatusBad("Error creating report: " + result.getErrorMessage());
				    		return;
				        }
					}
				});
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
				new Thread(task).start();
			}
		};
	}
	
	private Task<CallResult> getPrintTask(String title) {
		return new Task<CallResult>() {
			@Override protected CallResult call() throws Exception {
				List<Transaction> printTransactions = new ArrayList<Transaction>();
				for (DisplayableTransaction displayableTransaction : transactionTable.getItems()) {
					printTransactions.add((Transaction)displayableTransaction);
				}
				return ServiceFactory.getInstance().getReportSvc().createDetailReport(getSearchCriteria(), 
					printTransactions, openAccount.getName(), title);
			}
		};
	}
	
	private CallResult getHeaders() {
		CallResult result = new CallResult();
		Scene scene = new Scene(printTitlePane.getPrintTitlePane(), 460, 140);
		scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("Optional Title");
		stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
		stage.setOnHidden((WindowEvent event1) -> {
			if (printTitlePane.isCanceled()) {
				result.setCallBad();
			}else {
	        	result.setReturnedObject(printTitlePane.getTitle());
			}
        	stage.close();
	    });
		DisplayServiceImplJavaFX.center(stage, scene);
		stage.showAndWait();
		return result;
	}

	public CallResult loadAccounts() {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getAccounts();
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Accounts", result.getErrorMessage());
    		return result;
		}
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>) result.getReturnedObject();
		if (accounts.size() > 0) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                	setAccountButtonsVisible(true);                
           			accountCombo.getItems().clear();
        			for (Account account : accounts) {
        				accountCombo.getItems().add(account);
        				// Refresh openAccount with any saved changes
        				if (openAccount != null && openAccount.equals(account)) {
        					openAccount = account;
        				}
        			}
                }
            });
 			if (accounts.size() == 1) {
				openAccount = accounts.get(0);
				Platform.runLater(()->accountCombo.getSelectionModel().selectFirst());
			}else if (openAccount != null) {
				Platform.runLater(()->accountCombo.getSelectionModel().select(openAccount));
			}else {
				Platform.runLater(()->accountCombo.getSelectionModel().clearSelection());
			}
			setAccountButtonsDisabled(false);
        }else {
        	openAccount = null;
        	Platform.runLater(()->setAccountButtonsVisible(false));
        	Platform.runLater(()->accountCombo.getItems().clear());
        }
		accountTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}
	
	private EventHandler<ActionEvent> getSearchButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				printExportButton.setDisable(false);
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
            	if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
					ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Invalid Date Range", "Start date is after end date");
					return;
            	}
				List<SearchCriterion> searchCriteria = getSearchCriteria();
				List<DisplayableTransaction> transactions = getSearchResults(searchCriteria);
				displayTransactions(transactions);
				if (transactions.size() > 0) {
					transactionTable.scrollTo(transactionTable.getItems().get(0));
				}else {
					printExportButton.setDisable(true);
				}
				if (searchActive) {
					searchPane.setStyle("-fx-border-color: #0093ff; -fx-border-insets: -1.4");
				}else {
					searchPane.setStyle("-fx-border-color: grey");
				}
			}
		};
	}
	
	private List<DisplayableTransaction> getSearchResults(List<SearchCriterion> searchCriteria){
		List<DisplayableTransaction> displayableTransactions = new ArrayList<DisplayableTransaction>();
		boolean addTransaction;
		// Transaction must satisfy all search criteria to be included
		for (Transaction transaction : transactions) {
			addTransaction = true;
			for (SearchCriterion searchCriterion : searchCriteria) {
				if (!searchCriterion.matches(transaction)) {
					addTransaction = false;
					break;
				}
			}
			if (addTransaction) {
				displayableTransactions.add(new DisplayableTransaction(transaction));
			}
		}
		return displayableTransactions;
	}
	
	private void calculateTotals(List<DisplayableTransaction> transactions) {
		long withdrawals = 0;
		long deposits = 0;
		Money withdrawalsAmount = new Money(BigDecimal.valueOf(0));
		Money depositsAmount = new Money(BigDecimal.valueOf(0));
		for (DisplayableTransaction transaction : transactions) {
			if (transaction.getAmount().isCredit()) {
				deposits++;
				depositsAmount.add(transaction.getAmount());
			}else {
				withdrawals++;
				withdrawalsAmount.add(transaction.getAmount());
			}
		}
		withdrawalsCount.setText(numberFormat.format(withdrawals));
		depositsCount.setText(numberFormat.format(deposits));
		withdrawalsTotal.setText(withdrawalsAmount.toString());
		depositsTotal.setText(depositsAmount.toString());
	}
	
	private EventHandler<ActionEvent> getResetButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				resetSearchControls();
				transactionTimestamp = new Timestamp(0);
				if (openAccount != null) refreshDisplay(openAccount);
			}
		};
	}
	
	private ChangeListener<Toggle> getTimePeriodChangeListener() {
		return new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				if (!toggleGroupActive) return;
   			 	resetDatePickers();
				if (timePeriodGroup.getSelectedToggle() != null) {
			         if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("This Month")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 startDatePicker.setValue(transactions.get(0).getDate().value().withDayOfMonth(1));
		        			 endDatePicker.setValue(startDatePicker.getValue().withDayOfMonth(startDatePicker.getValue().lengthOfMonth()));
		        		 }
			         }else if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("Last Month")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 startDatePicker.setValue(transactions.get(0).getDate().value().minusMonths(1).withDayOfMonth(1));
		        			 endDatePicker.setValue(startDatePicker.getValue().withDayOfMonth(startDatePicker.getValue().lengthOfMonth()));
		        		 }
			         } else if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("This Year")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 startDatePicker.setValue(transactions.get(0).getDate().value().withDayOfYear(1));
		        			 endDatePicker.setValue(startDatePicker.getValue().withDayOfYear(startDatePicker.getValue().lengthOfYear()));
		        		 }
			         } else if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("Last Year")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 endDatePicker.setValue(transactions.get(0).getDate().value().withDayOfYear(1).minusDays(1));
		        			 startDatePicker.setValue(endDatePicker.getValue().withDayOfYear(1));
		        		 }
			         }
				}
			}
		};
	}
	
	private ChangeListener<Toggle> getAmountGroupChangeListener() {
		return new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		        if (amountGroup.getSelectedToggle() != null) {
					if (((ToggleButton)amountGroup.getSelectedToggle()).getText().equals("Between") &&
				        	amountGroup.getSelectedToggle().isSelected()) {
							andText.setVisible(true);
							toAmount.setVisible(true);
					}else {
						andText.setVisible(false);
						toAmount.setVisible(false);
				    }
		        }else {
					andText.setVisible(false);
					toAmount.setVisible(false);
		        }
			}
		};
	}

	private EventHandler<ActionEvent> getImportButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				CallResult result = ServiceFactory.getInstance().getDisplaySvc().getInputFileName(openAccount.getImportFolder(), "Open file to import");
				if (result.isGood()) {
					Task<CallResult> task = getImportTask((File)result.getReturnedObject(), (Account) accountCombo.getSelectionModel().getSelectedItem());
					task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
					    public void handle(WorkerStateEvent t) {
					        CallResult result = task.getValue();
							ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
					        if (result.isGood()) {
								@SuppressWarnings("unchecked")
								List<Integer> importCounts = ((List<Integer>)result.getReturnedObject());
								String importCount = NumberFormat.getNumberInstance(Locale.getDefault()).format(importCounts.get(0));
								String skipCount = NumberFormat.getNumberInstance(Locale.getDefault()).format(importCounts.get(0));
								if (importCounts.get(1).intValue() > 0) {
						        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Imported " + importCount + " transactions.  " +
						        		"Skipped " + skipCount + " transactions for dates previously loaded.");
								}else {
						        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Imported " + importCount + " transactions.");
								}
					            Platform.runLater(new Runnable() {
					                @Override public void run() {
					    				Account accountSelected = (Account) accountCombo.getSelectionModel().getSelectedItem();
					    				refreshDisplay(accountSelected);
					    				resetSearchControls();
					                }
					            });
					        }else {
					        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
					        }
					    }
					});
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Importing...");
					ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
					new Thread(task).start();
				}
			}
		};
	}
	
	private EventHandler<ActionEvent> getExportButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Account account = accountCombo.getSelectionModel().getSelectedItem();
				Scene scene = new Scene(exportOptionsPane.getExportOptionsPane());
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Export File Options");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!exportOptionsPane.isCanceled()) {
			        	ExportType exportType = exportOptionsPane.getExportType();
						CallResult result = ServiceFactory.getInstance().getDisplaySvc().getOutputFileName("", account.getName() + "." + exportType.getText(), "Choose Export File");
						if (result.isBad()) {
							return;
						}
						Task<CallResult> task = getExportTask((File)result.getReturnedObject(), account, exportType);
						task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							@Override
						    public void handle(WorkerStateEvent t) {
						        CallResult result = task.getValue();
								ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
						        if (result.isGood()) {
						        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Exported " + account.getTransactionCount() + " transactions.");
						        }else {
						        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
						        }
						    }
						});
			        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Exporting...");
						ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
						new Thread(task).start();
					}
			    });
				stage.show();
			}
		};
	}
	
	private EventHandler<ActionEvent> getDeleteAccountButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
            	ButtonType yesButton = new ButtonType("Yes");
            	ButtonType noButton = new ButtonType("No");
            	FxAlert confirmationAlert = new FxAlert(AlertType.CONFIRMATION, "Delete Account", "This cannot be undone.  Are you sure you want to delete this account?", yesButton, noButton);
                Optional<?> result = confirmationAlert.showAndWait();
            	if (result.isPresent() && result.get().equals(yesButton)) {
            		String accountName = openAccount.getName();
					Task<CallResult> task = getDeleteAccountTask(openAccount);
					task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
					    public void handle(WorkerStateEvent t) {
					        CallResult result = task.getValue();
							ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
					        if (result.isGood()) {
					            Platform.runLater(new Runnable() {
					                @Override public void run() {
					                	openAccount = null;
					    				transactionTable.setVisible(false);
					    				loadAccounts();
					    				resetSearchControls();
					    				searchTransactionBar.setVisible(false);
							        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood(accountName + " account deleted");
					                }
					            });
					        }else {
					        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
					        }
					    }
					});
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Deleting account...");
					ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
					new Thread(task).start();
				}
			}
		};
	}

	private EventHandler<ActionEvent> getHelpButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().displayHelp("AccountsTab.html");
			}
		};
	}

	private Task<CallResult> getImportTask(File file, Account account){
		return new Task<CallResult>() {
			@Override protected CallResult call() throws Exception {
				return ServiceFactory.getInstance().getMaintenanceSvc().importFile(file, account);
			}
		};
	}
	
	private Task<CallResult> getExportTask(File file, Account account, ExportType exportType){
		return new Task<CallResult>() {
			@Override protected CallResult call() throws Exception {
				if (exportType.equals(ExportType.QIF)) {
					return ServiceFactory.getInstance().getExportSvc().exportQIF(account, file);
				}else {
					return ServiceFactory.getInstance().getExportSvc().exportOFX(account, file);
				}
			}
		};
	}
	
	private Task<CallResult> getDeleteAccountTask(Account account){
		return new Task<CallResult>() {
			@Override protected CallResult call() throws Exception {
				return ServiceFactory.getInstance().getMaintenanceSvc().deleteAccount(account);
			}
		};
	}
	
	private EventHandler<ActionEvent> getApplyFiltersButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				applyFilters(false);
			}
		};
	}
	
	private void applyFilters(boolean newTransactionsFiltered) {
		ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		if (openAccount.getFilterSet().getID() == 0) {
        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad("No filter set has been selected for this account.  Use the Account Settings button to select a filter set.");
        	return;
		}
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getFilterSet(openAccount.getFilterSet().getID());
		if (result.isBad()) {
        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result);
		}
		openAccount.setFilterSet((FilterSet)result.getReturnedObject());
		if (openAccount.getFilterSet().getFilters().size() == 0) {
        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad("No filters have been defined for the filter set associated with this account.  Use the Make New Filters button to create filters.");
        	return;
		}
		Task<CallResult> task = getFilterTask(openAccount, newTransactionsFiltered);
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
		    public void handle(WorkerStateEvent t) {
		        CallResult result = task.getValue();
		        if (result.isBad()) {
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusBad(result.getErrorMessage());
	        	}else {
	        		@SuppressWarnings("unchecked")
					List<Integer> returnedCounts = (List<Integer>)result.getReturnedObject();
	        		String filteredCount = numberFormat.format(returnedCounts.get(0).intValue());
	        		String redundantFilterCount = numberFormat.format(returnedCounts.get(1).intValue());
	        		String totalCount = numberFormat.format(openAccount.getTransactionCount());
	        		String resultMessage = "Filtered " + totalCount + " transactions.  Updated " + filteredCount + " transactions.";
	        		if (returnedCounts.get(1).intValue() > 0) {
	        			resultMessage = resultMessage + "  Found " + redundantFilterCount + " redundant filters.  See /Figures/Figures.log for details.";
	        		}
		        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood(resultMessage);
		            Platform.runLater(new Runnable() {
		                @Override public void run() {
		    				loadAndDisplayTransactions(openAccount);
		    				resetSearchControls();
		    				ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
		                }
		            });
	        	}
		    }
		});
    	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Filtering...");
		ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
		new Thread(task).start();
	}

	private Task<CallResult> getFilterTask(Account account, boolean newTransactionsFiltered){
		return new Task<CallResult>() {
			@Override protected CallResult call() throws Exception {
				CallResult filterResult = ServiceFactory.getInstance().getMaintenanceSvc().filterAndUpdateTransactions(account);
				ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
				if (newTransactionsFiltered) {
					if (filterResult.isGood()) {
						account.setLastFilteredDate(account.getLastLoadedDate());
						CallResult result = ServiceFactory.getInstance().getPersistenceSvc().updateAccount(account);
						if (result.isBad()) filterResult = result;
					}
				}
				return filterResult;
			}
		};
	}
	
	private void defineNewFilters(boolean useDescription, boolean newTransactionsSelected, boolean depositsOnlySelected, boolean withdrawalsOnlySelected) {
		Account accountSelected = (Account) accountCombo.getSelectionModel().getSelectedItem();
		FilterField field = null;
		if (useDescription) {
			field = FilterField.DESCRIPTION;
		}else {
			field = FilterField.MEMO;
		}
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getDistinctValues(accountSelected, field.name(), 
			newTransactionsSelected, depositsOnlySelected, withdrawalsOnlySelected);
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading " + field.name() + "s", result.getErrorMessage());
			return;
		}
		@SuppressWarnings("unchecked")
		List<String> strings = (List<String>) result.getReturnedObject();
		if (strings.size() == 0) {
			ServiceFactory.getInstance().getDisplaySvc().setStatusGood("No new " + field.name() + "s found.");
			return;
		}
		NewFiltersPane newFiltersPane = new NewFiltersPane();
		ServiceFactory.getInstance().getDisplaySvc().clearStatus();
		Pane filtersPane = newFiltersPane.getNewFiltersPane(accountSelected, strings, field);
		Scene scene = new Scene(filtersPane, filtersPane.getPrefWidth()+6, newFiltersPane.getHeight(strings.size())+6);
		scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setHeight(newFiltersPane.getHeight(strings.size()));
		stage.setResizable(true);
		stage.setTitle("Create New Filters");
		stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
		stage.setOnHidden((WindowEvent event1) -> {
			stage.close();
        	if (newFiltersPane.isDataChanged()) {
        		applyFilters(true);
        	}
	    });
		stage.setOnCloseRequest(event2 -> {
	        stage.hide();
	    	stage.close();
	    });
		DisplayServiceImplJavaFX.center(stage, scene);
		filtersPane.prefWidthProperty().bind(stage.widthProperty());
		filtersPane.prefHeightProperty().bind(stage.heightProperty());
		stage.show();
	}
	
	private void refreshDisplay(Account account) {
		Task<Object> refreshTask = new Task<Object>() {
			@Override protected Object call() throws Exception {
           		if (accountTimestamp.before(Figures.accountTimestamp)) loadAccounts();
        		if (categoryTimestamp.before(Figures.categoryTimestamp)) {
        			loadCategories();
           			loadAndDisplayTransactions(account);
           			return null;
        		}
        		if (transactionTimestamp.before(Figures.transactionTimestamp)) {
            		loadAndDisplayTransactions(account);
            	}
        		return null;
			}
		};
		refreshTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
		    public void handle(WorkerStateEvent t) {
				ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
		    }
		});
		refreshTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
		    public void handle(WorkerStateEvent t) {
				ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
		    }
		});
		ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
		new Thread(refreshTask).start();
	}
	
	private CallResult loadAndDisplayTransactions(Account account) {
		sortOrder.clear();
		if (transactionTable.getSortOrder().size() > 0) {
			sortOrder.addAll(transactionTable.getSortOrder());
		}
		CallResult result = loadTransactions(account);
		if (result.isGood()) {
			if (account.getTransactionCount() == 0) {
				searchTransactionBar.setVisible(false);
				transactionTable.setVisible(false);
				setTransactionButtonsVisible(false);
				Platform.runLater(()->importButton.requestFocus());
				ServiceFactory.getInstance().getDisplaySvc().setStatusHelp("Download your transactions as a QIF (Quicken) or OFX (Microsoft Money) file, then use the Import button to import them.");
				return result;
			}
			List<DisplayableTransaction> displayTransactionList;
			if (searchActive) {
				displayTransactionList = getSearchResults(getSearchCriteria());
			}else {
				displayTransactionList = new ArrayList<DisplayableTransaction>();
				for (Transaction transaction : transactions) {
					displayTransactionList.add(new DisplayableTransaction(transaction));
				}
			}
			displayTransactions(displayTransactionList);
			transactionTable.prefHeightProperty().bind(transactionTable.fixedCellSizeProperty().multiply(transactionTable.getItems().size()).add(2.1));
			transactionTable.refresh();
			// Preserve the sort order, if any
			if (sortOrder.size() > 0) {
		        Platform.runLater(new Runnable() {
		            @Override public void run() {
		            	transactionTable.getSortOrder().clear();
		            	transactionTable.getSortOrder().addAll(sortOrder);
		            	transactionTable.sort();
		            }
		        });
			}
	        Platform.runLater(new Runnable() {
	            @Override public void run() {
	    			resetDatePickers();
	    			searchTransactionBar.setVisible(true);
	    			transactionTable.setVisible(true);
	    			setTransactionButtonsVisible(true);
	            }
	        });
			transactionTimestamp = new Timestamp(System.currentTimeMillis());
		}else {
			DisplayServiceImplJavaFX.statusMessage.setText(result.getErrorMessage());
		}
		return result;
	}

	private void setAccountButtonsVisible(boolean visible) {
		settingsButton.setVisible(visible);
		deleteAccountButton.setVisible(visible);
		exportButton.setVisible(visible);
		if (openAccount != null && openAccount.getTransactionCount() > 0) {
			exportButton.setDisable(false);
			deleteAccountButton.setDisable(false);
		}else{
			exportButton.setDisable(true);
			deleteAccountButton.setDisable(true);
		}
	}

	
	private void setAccountButtonsDisabled(boolean disabled) {
		settingsButton.setDisable(disabled);
		deleteAccountButton.setDisable(disabled);
		if (openAccount != null && openAccount.getTransactionCount() > 0) {
			exportButton.setDisable(false);
		}else{
			exportButton.setDisable(true);
		}
	}
	
	private void setTransactionButtonsVisible(boolean visible) {
		applyFiltersButton.setVisible(visible);
		newFiltersButton.setVisible(visible);
		printExportButton.setVisible(visible);
	}
	
	private CallResult loadTransactions(Account account) {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getTransactions(account);
		if (result.isGood()) {
			transactions = account.getTransactions();
		}else {
			DisplayServiceImplJavaFX.statusMessage.setText(result.getErrorMessage());
		}
		transactionTimestamp = new Timestamp(System.currentTimeMillis());
		return result;
	}
	
	private void displayTransactions(List<DisplayableTransaction> displayTransactionList) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
				setAccountButtonsDisabled(false);
            	transactionTable.setItems(FXCollections.observableArrayList(displayTransactionList));
            	transactionTable.refresh();
            	calculateTotals(displayTransactionList);
            }
        });
	}
	
	private void resetSearchControls() {
		searchPane.setStyle("-fx-border-color: grey");
		resetDatePickers();
		resetDateToggles();
		equals.setSelected(false);
		greaterThan.setSelected(false);
		lessThan.setSelected(false);
		withdrawals.setSelected(false);
		deposits.setSelected(false);
		checks.setSelected(false);
    	checks.getStyleClass().removeAll("red-text");
    	checkInclusion = CheckInclusion.NONE;
		deductible.setSelected(false);
		deductible.getStyleClass().removeAll("red-text");
		deductibleInclusion = DeductibleInclusion.NONE;
		uncategorized.setSelected(false);
		uncategorized.getStyleClass().removeAll("red-text");
		uncategorizedInclusion = UncategorizedInclusion.NONE;
		unfiltered.setSelected(false);
		categoryCombo.getCheckModel().clearChecks();
		fromAmount.clear();
		if (toAmount.isVisible()) toAmount.clear();
		between.setSelected(false);
		description.clear();
		memo.clear();
		searchActive = false;
		printExportButton.setDisable(false);
	}
	
	private void resetDateToggles() {
		toggleGroupActive = false;
		thisMonthButton.setSelected(false);
		lastMonthButton.setSelected(false);
		thisYearButton.setSelected(false);
		lastYearButton.setSelected(false);
		toggleGroupActive = true;
	}
	
	private DatePicker getDatePicker() {
		DatePicker datePicker = new DatePicker();
		datePicker.setEditable(true);
		datePicker.getStyleClass().add("datepicker");
		datePicker.setPrefWidth(110);
		datePicker.setOnMouseClicked(e -> {
			resetDateToggles();
		});
		datePicker.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
            	resetDateToggles();
            }
        });
		datePicker.focusedProperty().addListener(new ChangeListener<Boolean>() {
	        @Override
	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	            if (!newValue){
	            	LocalDate date = datePicker.getConverter().fromString(datePicker.getEditor().getText());
	            	if (date != null) datePicker.setValue(date);
	            }
	        }
	    });
		datePicker.setConverter(new StringConverter<LocalDate>() {
	            @Override
	            public String toString(LocalDate date) {
	                if (date == null) {
	                    return "" ;
	                }
	                return new TransactionDate(date).toString();
	            }

	            @Override
	            public LocalDate fromString(String string) {
	                if (string == null || string.isEmpty()) {
	                    return null ;
	                }
					return LocalDate.parse(string, Figures.dateFormat);
	            }

	        });
		return datePicker;
	}
	
	private void resetDatePickers() {
		if (transactions.size() > 0) {
			startDatePicker.setValue(transactions.get(transactions.size()-1).getDate().value());
			endDatePicker.setValue(transactions.get(0).getDate().value());
		}
	}
	
	private List<SearchCriterion> getSearchCriteria(){
		List<SearchCriterion> criterionList = new ArrayList<SearchCriterion>();
		// The Unfiltered criterion must always be the first in the list if selected
		// so that any subsequent criteria act on the unfiltered fields.
		if (unfiltered.isSelected()) {
			criterionList.add(new UnfilteredCriterion());
		}
		TransactionDate startDate = new TransactionDate(startDatePicker.getValue());
		TransactionDate endDate = new TransactionDate(endDatePicker.getValue());
		DateRangeCriterion dateRangeCriterion = new DateRangeCriterion(startDate, endDate);
		criterionList.add(dateRangeCriterion);
		if (StringUtils.isNotEmpty(fromAmount.getText())){
			Money fromValue;
			try {
				fromValue = new Money(fromAmount.getText());
			} catch (Exception e) {
				ServiceFactory.getInstance().getDisplaySvc().setStatusBad("INVALID AMOUNT: " +  fromAmount.getText());
				return null;
			}
			if (equals.isSelected()) {
				criterionList.add(new AmountEqualsCriterion(fromValue));
			}else if (lessThan.isSelected()) {
				criterionList.add(new AmountLessThanCriterion(fromValue));
			}else if (greaterThan.isSelected()) {
				criterionList.add(new AmountGreaterThanCriterion(fromValue));
			}else if (between.isSelected()) {
				Money toValue;
				try {
					toValue = new Money(toAmount.getText());
				} catch (Exception e) {
					ServiceFactory.getInstance().getDisplaySvc().setStatusBad("INVALID AMOUNT: " +  toAmount.getText());
					return null;
				}
				criterionList.add(new AmountBetweenCriterion(fromValue, toValue));
			}else {
				AmountEqualsCriterion equalsCriterion = new AmountEqualsCriterion(fromValue);
				criterionList.add(equalsCriterion);
			}
		}
		if (withdrawals.isSelected()) {
			criterionList.add(new WithdrawalsCriterion());
		}
		if (deposits.isSelected()) {
			criterionList.add(new DepositsCriterion());
		}
		if (checkInclusion.equals(CheckInclusion.INCLUDED)) {
			criterionList.add(new ChecksOnlyCriterion());
		}else if (checkInclusion.equals(CheckInclusion.EXCLUDED)) {
			criterionList.add(new ChecksExcludedCriterion());
		}
		if (StringUtils.isNotEmpty(description.getText())){
			criterionList.add(new DescriptionCriterion(description.getText()));
		}
		if (StringUtils.isNotEmpty(memo.getText())){
			criterionList.add(new MemoCriterion(memo.getText()));
		}
		if (deductibleInclusion.equals(DeductibleInclusion.INCLUDED)) {
			criterionList.add(new DeductibleOnlyCriterion());
		}else if (deductibleInclusion.equals(DeductibleInclusion.EXCLUDED)) {
			criterionList.add(new DeductibleExcludedCriterion());
		}
		if (uncategorizedInclusion.equals(UncategorizedInclusion.INCLUDED)) {
			criterionList.add(new UncategorizedOnlyCriterion());
		}else if (uncategorizedInclusion.equals(UncategorizedInclusion.EXCLUDED)) {
			criterionList.add(new UncategorizedExcludedCriterion());
		}
		if (categoryCombo.getCheckModel().getCheckedItems().size() > 0) {
			List<Category> categories = new ArrayList<Category>();
			for (Category category :categoryCombo.getCheckModel().getCheckedItems()) {
				categories.add(new Category(category.getID(), category.getName()));
			}
			criterionList.add(new CategoryCriterion(categories));
		}
		if (criterionList.size() > 1) {
			searchActive = true;
		}else {
			if (dateRangeCriterion.getStartDate().equals(transactions.get(transactions.size()-1).getDate()) &&
				dateRangeCriterion.getEndDate().equals(transactions.get(0).getDate())) {
				searchActive = false;
			}else {
				searchActive = true;
			}
		}
		return criterionList;
	}
	
	public void setDisable(boolean disable) {
		accountTab.setDisable(disable);
	}
}
