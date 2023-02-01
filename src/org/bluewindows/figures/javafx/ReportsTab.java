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

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.Account;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.DateRange;
import org.bluewindows.figures.domain.Summary;
import org.bluewindows.figures.domain.SummaryReport;
import org.bluewindows.figures.domain.TransactionDate;
import org.bluewindows.figures.enums.CursorType;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.PersistenceServiceImpl;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.type.OrientationEnum;

public class ReportsTab {
	
	private static final float PORTRAIT_ZOOM = (float)1.70;
	private static final float LANDSCAPE_ZOOM = (float)1.32;
	
	private Tab reportsTab;
	private VBox baseVBox;
	private ReportSettingsPane summaryReportSettingsPane = new ReportSettingsPane();
	private ComboBox<SummaryReport> reportCombo;
	private boolean reportComboBeingUpdated = false;
	private SummaryReport selectedReport;
	private Button newButton;
	private Button modifyButton;
	private Button deleteButton;
	private Button helpButton;
	private Button generateButton;
	private Button resetButton;
	private ScrollPane generatePane;
	private Pane reportPane;
	private HBox generateBar;
	private VBox generateVBox;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private ToggleGroup timePeriodGroup;
	private boolean toggleGroupActive = true;
	private RadioButton thisMonthButton;
	private RadioButton lastMonthButton;
	private RadioButton thisYearButton;
	private RadioButton lastYearButton;
	private Timestamp reportTimestamp = new Timestamp(0);
	private Timestamp transactionTimestamp = new Timestamp(0);
	private Map<Integer, DateRange> accountDateRangeMap = new HashMap<Integer, DateRange>();
	private boolean isDefaultDateRange = false;
	
	public Tab getReportsTab() throws Exception {
		reportsTab = new Tab("Reports");
		baseVBox = new VBox();
		reportsTab.setContent(baseVBox);
		
		buildReportBar();
		
		generateBar = new HBox();
		baseVBox.getChildren().add(generateBar);
		
		buildGeneratePanel();
		
		reportPane = new Pane();
		generateBar.getChildren().add(reportPane);
		reportPane.setVisible(false);

		loadReports();
		
		reportsTab.selectedProperty().addListener(obs -> {
            if(reportsTab.isSelected()) {
            	ServiceFactory.getInstance().getDisplaySvc().clearStatus();
         		reportPane.prefWidthProperty().bind(DisplayServiceImplJavaFX.getStage().widthProperty().subtract(generatePane.getWidth()));
         		reportPane.maxWidthProperty().bind(DisplayServiceImplJavaFX.getStage().widthProperty());
         		reportPane.prefHeightProperty().bind(DisplayServiceImplJavaFX.getStage().heightProperty().subtract(280));
         		reportPane.maxHeightProperty().bind(DisplayServiceImplJavaFX.getStage().heightProperty().subtract(280));
            	if (reportTimestamp.before(Figures.reportTimestamp)) loadReports();
        		if (transactionTimestamp.before(Figures.transactionTimestamp)) {
        			if (selectedReport != null) {
        				accountDateRangeMap.clear();
        				CallResult result = getReportDateRange();
        				if (result.isBad()) return;
        				setDatePickers();
             			transactionTimestamp = Figures.transactionTimestamp;
        			}
        		}
            }
		});
		
		return reportsTab;
	}

	private void buildReportBar() {
		HBox reportBar = new HBox();
		reportBar.getStyleClass().add("top-button-bar");
		baseVBox.getChildren().add(reportBar);
		
		baseVBox.getChildren().add(new Separator());
		
		reportCombo = new ComboBox<SummaryReport>();
		reportBar.getChildren().add(reportCombo);
		reportCombo.setPromptText("Choose a Report");
		reportCombo.setMinWidth(300);
		reportCombo.setMaxWidth(300);
		reportCombo.setButtonCell(new ListCell<SummaryReport>() {
	        @Override
	        protected void updateItem(SummaryReport item, boolean empty) {
	            super.updateItem(item, empty) ;
	            if (empty || item == null) {
	                setText("Choose a Report");
	            } else {
	                setText(item.getName());
	            }
	        }
	    });
		reportCombo.setOnAction(getReportComboHandler());

		newButton = new Button("New Report");
		reportBar.getChildren().add(newButton);
		newButton.setOnAction(getNewReportButtonHandler());
		modifyButton = new Button("Modify Report");
		modifyButton.setDisable(true);
		modifyButton.setOnAction(getModifyReportButtonHandler());
		reportBar.getChildren().add(modifyButton);
		deleteButton = new Button("Delete Report");
		deleteButton.setOnAction(getDeleteButtonHandler());
		deleteButton.setDisable(true);
		reportBar.getChildren().add(deleteButton);
		helpButton = new Button("Help");
		helpButton.setOnAction(getHelpButtonHandler());
		reportBar.getChildren().add(helpButton);

	}
	
	private void buildGeneratePanel() {
		Region spacer = new Region();
		spacer.setMinWidth(1);
		spacer.setMaxWidth(1);
		generateBar.getChildren().add(spacer);
		generatePane = new ScrollPane();
		generatePane.setMinWidth(280);
		generatePane.setMaxWidth(280);
		generatePane.setStyle("-fx-background-color:transparent;");
		generateBar.getChildren().add(generatePane);
		generateVBox = new VBox();
		generatePane.setContent(generateVBox);
		generateVBox.setMinWidth(278);
		generateVBox.setMaxWidth(278);
		generateVBox.setStyle("-fx-border-color: grey");
		generateVBox.setVisible(false);

		Text dateRangeLabel = new Text(" Report Date Range");
		generateVBox.getChildren().add(dateRangeLabel);
		generateVBox.getChildren().add(new Separator());
		
		GridPane timeBox = new GridPane();
		timeBox.getStyleClass().add("paddedGrid");
		generateVBox.getChildren().add(timeBox);

		timeBox.add(new Text("Start Date"), 0, 0);
		timeBox.add(new Text("End Date"), 1, 0);

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
		RadioButtonToggleHandler ytdToggler = new RadioButtonToggleHandler(thisYearButton);
		thisYearButton.setOnMousePressed(ytdToggler.getMousePressed());
		thisYearButton.setOnMouseReleased(ytdToggler.getMouseReleased());
		
		lastYearButton = new RadioButton("Last Year");
		timeBox.add(lastYearButton, 1, 3);
		lastYearButton.setToggleGroup(timePeriodGroup);
		RadioButtonToggleHandler tp90Toggler = new RadioButtonToggleHandler(lastYearButton);
		lastYearButton.setOnMousePressed(tp90Toggler.getMousePressed());
		lastYearButton.setOnMouseReleased(tp90Toggler.getMouseReleased());

		HBox generateButtonHBar = new HBox();
		generateVBox.getChildren().add(generateButtonHBar);
		generateButton = new Button("Generate Report");
		generateButton.setOnAction(getGenerateReportButtonHandler());
		generateButtonHBar.getChildren().add(generateButton);
		resetButton = new Button("Reset");
		resetButton.setOnAction(getResetButtonHandler());
		generateButtonHBar.getChildren().add(resetButton);
		
		TextArea helpMessage = new TextArea();
		generateVBox.getChildren().add(helpMessage);
		helpMessage.setText("Dates above are based on account transaction dates.");
		helpMessage.setWrapText(true);
		helpMessage.getStyleClass().add("transparentTextArea");
		helpMessage.setMinHeight(50);
		helpMessage.setMaxHeight(50);
		helpMessage.setEditable(false);
		helpMessage.setMouseTransparent(true);
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

	private ChangeListener<Toggle> getTimePeriodChangeListener() {
		return new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				if (!toggleGroupActive) return;
				TransactionDate mostRecentLoadedDate = new TransactionDate(LocalDate.now());
				CallResult result = getReportDateRange();
				if (result.isGood()) {
					DateRange reportDateRange = (DateRange)result.getReturnedObject();
					mostRecentLoadedDate = reportDateRange.getEndDate();
				}
				if (timePeriodGroup.getSelectedToggle() != null) {
			         if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("This Month")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 startDatePicker.setValue(mostRecentLoadedDate.value().withDayOfMonth(1));
		        			 endDatePicker.setValue(startDatePicker.getValue().withDayOfMonth(startDatePicker.getValue().lengthOfMonth()));
		        		 } else {
		        			 setDatePickers();
		        		 }
			         } else if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("Last Month")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 startDatePicker.setValue(mostRecentLoadedDate.value().minusMonths(1).withDayOfMonth(1));
		        			 endDatePicker.setValue(startDatePicker.getValue().withDayOfMonth(startDatePicker.getValue().lengthOfMonth()));
		        		 } else {
		        			 setDatePickers();
		        		 }
			         } else if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("This Year")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 startDatePicker.setValue(mostRecentLoadedDate.value().withDayOfYear(1));
		        			 endDatePicker.setValue(startDatePicker.getValue().withDayOfYear(startDatePicker.getValue().lengthOfYear()));
		        		 } else {
		        			 setDatePickers();
		        		 }
			         } else if (((ToggleButton)timePeriodGroup.getSelectedToggle()).getText().equals("Last Year")){
		        		 if (timePeriodGroup.getSelectedToggle().isSelected()) {
		        			 endDatePicker.setValue(mostRecentLoadedDate.value().withDayOfYear(1).minusDays(1));
		        			 startDatePicker.setValue(endDatePicker.getValue().withDayOfYear(1));
		        		 } else {
		        			 setDatePickers();
		        		 }
					} else {
	       			 	setDatePickers();
					}
				}
			}
		};
	}
	
	private void setDatePickers() {
		if (selectedReport.getDateRange().getStartDate() != null) {
			startDatePicker.setValue(selectedReport.getDateRange().getStartDate().value());
			endDatePicker.setValue(selectedReport.getDateRange().getEndDate().value());
			return;
		}
		CallResult result = getReportDateRange();
		if (result.isBad()) return;
		DateRange reportDateRange = (DateRange)result.getReturnedObject();
		startDatePicker.setValue(reportDateRange.getStartDate().value());
		endDatePicker.setValue(reportDateRange.getEndDate().value());
	}
	
	private CallResult getReportDateRange() {
		CallResult result = new CallResult();
		DateRange reportDateRange = new DateRange();
		reportDateRange.setStartDate(new TransactionDate(LocalDate.MAX));
		reportDateRange.setEndDate(new TransactionDate(LocalDate.MIN));
		for (Account account : selectedReport.getAccounts()) {
			DateRange accountDateRange;
			if (accountDateRangeMap.get(Integer.valueOf(account.getID())) == null) {
				result = getAccountDateRange(account);
				if (result.isBad()) {
		    		return result;
				}
			}
			accountDateRange = accountDateRangeMap.get(Integer.valueOf(account.getID()));
			// If the account has no transactions, the date range start date will be the minimum date
			if (!accountDateRange.getStartDate().equals(TransactionDate.MINIMUM_DATE)) { 
				if (accountDateRange.getStartDate().compareTo(reportDateRange.getStartDate()) < 0) reportDateRange.setStartDate(accountDateRange.getStartDate());
				if (accountDateRange.getEndDate().compareTo(reportDateRange.getEndDate()) > 0) reportDateRange.setEndDate(accountDateRange.getEndDate());
			}
		}
		if (reportDateRange.getStartDate().value().equals(LocalDate.MAX)) {
			reportDateRange.setStartDate(new TransactionDate(LocalDate.now()));
			reportDateRange.setEndDate(new TransactionDate(LocalDate.now()));
			isDefaultDateRange = true;
		}else {
			isDefaultDateRange = false;
		}
		result.setReturnedObject(reportDateRange);
		return result;
	}
	
	private CallResult getAccountDateRange(Account account) {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getAccountDateRange(account);
		if (result.isBad()) {
    		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Date Range", result.getErrorMessage());
    		return result;
		}
		accountDateRangeMap.put(Integer.valueOf(account.getID()), (DateRange)result.getReturnedObject());
		return result;
	}
	
	private EventHandler<ActionEvent> getNewReportButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				Scene scene = new Scene(summaryReportSettingsPane.getSummaryReportSettingsPane(null), 800, 400);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Create New Summary Report");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!summaryReportSettingsPane.isCanceled()) {
						loadReports();
			        }
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
				// Set focus on base grid rather than any particular control
				Platform.runLater(()->summaryReportSettingsPane.getBasePane().requestFocus());
			}
		};
	}
	
	private EventHandler<ActionEvent> getDeleteButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
            	ButtonType yesButton = new ButtonType("Yes");
            	ButtonType noButton = new ButtonType("No");
            	FxAlert confirmationAlert = new FxAlert(AlertType.CONFIRMATION, "Delete Report", "This cannot be undone.  Are you sure you want to delete this report?", yesButton, noButton);
                Optional<?> result = confirmationAlert.showAndWait();
            	if (result.isPresent() && result.get().equals(yesButton)) {
                	CallResult callResult = ServiceFactory.getInstance().getPersistenceSvc().deleteReport(selectedReport);
                	if (callResult.isBad()) {
                		ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Deleting Category", callResult.getErrorMessage());
            			return;
                	}
                	selectedReport = null;
                	loadReports();
            	}
			}
		};
	}

	
	private EventHandler<ActionEvent> getHelpButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceFactory.getInstance().getDisplaySvc().displayHelp("ReportsTab.html");
			}
		};
	}
	
	private EventHandler<ActionEvent> getModifyReportButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				SummaryReport report = reportCombo.getSelectionModel().getSelectedItem();
				Scene scene = new Scene(summaryReportSettingsPane.getSummaryReportSettingsPane(report), 
					ReportSettingsPane.WIDTH, ReportSettingsPane.HEIGHT);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Modify Summary Report");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				stage.setOnHidden((WindowEvent event1) -> {
			        if (!summaryReportSettingsPane.isCanceled()) {
						loadReports();
			        }
			    });
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
				// Set focus on base grid rather than any particular control
				Platform.runLater(()->summaryReportSettingsPane.getBasePane().requestFocus());
			}
		};
	}
	
	private EventHandler<ActionEvent> getReportComboHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (reportComboBeingUpdated) return;
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				if (reportCombo.getSelectionModel().getSelectedItem() != null && reportCombo.getItems().size() > 0) {
					reportCombo.getStyleClass().add("selected-bold");
					selectedReport = reportCombo.getSelectionModel().getSelectedItem();
					modifyButton.setDisable(false);
					deleteButton.setDisable(false);
					generateVBox.setVisible(true);
					resetDateToggles();
					setDatePickers();
				}else {
					reportCombo.getStyleClass().remove("selected-bold");
					modifyButton.setDisable(true);
					deleteButton.setDisable(true);
					generateVBox.setVisible(false);
					reportPane.setVisible(false);
				}
				reportPane.setVisible(false);
			}
		};
	}
	
	private EventHandler<ActionEvent> getGenerateReportButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				SummaryReport report = reportCombo.getSelectionModel().getSelectedItem();
				// The following can happen after an account is deleted
				if (report.getAccounts().size() == 0) {
					ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Generating Summary Report", "This report has no accounts selected.");
					return;
				}
				Task<CallResult> task = getGenerateSummaryTask(report);
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
				    public void handle(WorkerStateEvent t) {
						ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.DEFAULT);
						ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				        CallResult result = task.getValue();
						if (result.isGood()){
							if (result.getReturnedObject() != null) {
								JasperPrint reportPrint = (JasperPrint)result.getReturnedObject();
								float zoom;
								if (reportPrint.getOrientationValue().equals(OrientationEnum.PORTRAIT)){
									zoom = PORTRAIT_ZOOM;
								}else {
									zoom = LANDSCAPE_ZOOM;
								}
								JasperFXport jasperExport = new JasperFXport(reportPrint, reportPane, zoom, 
									DisplayServiceImplJavaFX.getCss(), DisplayServiceImplJavaFX.getTabPane().getWidth() * .8, DisplayServiceImplJavaFX.getTabPane().getHeight() * .95);
								reportPane.setVisible(true);
								try {
									jasperExport.show();
								} catch (IOException e) {
									Figures.logStackTrace(e);
								}
								Platform.runLater(()->reportPane.requestFocus());
							}else {
								reportPane.setVisible(false);
								ServiceFactory.getInstance().getDisplaySvc().displayInformationMessage("Report Generation Canceled", "No data found for that report.");
								Platform.runLater(()->baseVBox.requestFocus());
							}
						}
				    }
				});
	        	ServiceFactory.getInstance().getDisplaySvc().setStatusGood("Generating report...");
				ServiceFactory.getInstance().getDisplaySvc().setCursor(CursorType.WAIT);
				new Thread(task).start();
			}
		};
	}
	
	private Task<CallResult> getGenerateSummaryTask(SummaryReport report){
		return new Task<CallResult>() {
			@Override protected CallResult call() throws Exception {
				CallResult result = new CallResult();
				DateRange dateRange = new DateRange();
				dateRange.setStartDate(new TransactionDate(startDatePicker.getValue()));
				dateRange.setEndDate(new TransactionDate(endDatePicker.getValue()));
				report.setDateRange(dateRange);
				if (!isDefaultDateRange) {
					result = ServiceFactory.getInstance().getPersistenceSvc().updateReport(report);
					if (result.isBad()){
						ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Saving Summary Report", result.getErrorMessage());
						return result;
					}
					reportTimestamp = new Timestamp(System.currentTimeMillis());
				}
				result = PersistenceServiceImpl.getInstance().getSummaryDao().getSummaries(report);
				if (result.isBad()){
					ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Retrieving Summaries", result.getErrorMessage());
					return result;
				}
				@SuppressWarnings("unchecked")
				List<Summary> summaries = (List<Summary>)result.getReturnedObject();
				if (summaries.size() > 0) {
					result = ServiceFactory.getInstance().getReportSvc().createSummaryReport(report, summaries);
					if (result.isBad()){
						ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Creating Summary Report", result.getErrorMessage());
						return result;
					}
				}else {
					result.setReturnedObject(null);
				}
				return result;
			}
		};
	}
	
	private EventHandler<ActionEvent> getResetButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ServiceFactory.getInstance().getDisplaySvc().clearStatus();
				resetDateToggles();
				setDatePickers();
			}
		};
	}
	
	public void resetForNewFile() {
		resetDateToggles();
		selectedReport = null;
		reportCombo.getItems().clear();
		reportPane.setVisible(false);
		isDefaultDateRange = false;
	}

	private void resetDateToggles() {
		toggleGroupActive = false;
		lastYearButton.setSelected(false);
		thisMonthButton.setSelected(false);
		lastMonthButton.setSelected(false);
		thisYearButton.setSelected(false);
		toggleGroupActive = true;
	}
	
	private CallResult loadReports() {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().getSummaryDao().getReports();
		if (result.isBad()){
			ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Error Loading Summary Reports", result.getErrorMessage());
			return result;
		}
		@SuppressWarnings("unchecked")
		List<SummaryReport> reports = (List<SummaryReport>) result.getReturnedObject();
		if (reports.size() > 0) {
			reportComboBeingUpdated = true;
			reportCombo.getItems().clear();
			for (SummaryReport report : reports) {
				reportCombo.getItems().add(report);
			}
			if (selectedReport != null) {
				if (reportCombo.getItems().contains(selectedReport)){
					reportCombo.getSelectionModel().select(selectedReport);
				}else {
					selectedReport = null;
				}
			}
			if (reports.size() == 1) {
				Platform.runLater(()->reportCombo.getSelectionModel().selectFirst());
				selectedReport = reports.get(0);
			}
		}else {
			reportCombo.getItems().clear();
			selectedReport = null;
		}
		reportComboBeingUpdated = false;
		reportTimestamp = new Timestamp(System.currentTimeMillis());
		reportPane.setVisible(false);
		return result;
	}
}
