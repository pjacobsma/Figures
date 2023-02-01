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

package org.bluewindows.figures.service.impl.javafx;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.enums.CursorType;
import org.bluewindows.figures.javafx.AboutPane;
import org.bluewindows.figures.javafx.AccountsTab;
import org.bluewindows.figures.javafx.CategoriesTab;
import org.bluewindows.figures.javafx.FileOpenOptionsPane;
import org.bluewindows.figures.javafx.FiltersTab;
import org.bluewindows.figures.javafx.FxAlert;
import org.bluewindows.figures.javafx.PasswordPane;
import org.bluewindows.figures.javafx.ReportsTab;
import org.bluewindows.figures.javafx.SettingsPane;
import org.bluewindows.figures.service.DisplayService;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class DisplayServiceImplJavaFX extends Application implements DisplayService  {

	public static final int PREFERRED_WIDTH = 1440;
	public static final int PREFERRED_HEIGHT = 1025;
	public static Image appIcon;
	public static Image addIcon;
	public static Image editIcon;
	public static Image deleteIcon;
	public static Image checkmarkIcon;
	public static TextArea statusMessage;
	
	private static DisplayServiceImplJavaFX instance; 
	private static Stage stage;
	private static Scene scene;
	private static String css;
	
    private static final String WINDOW_POSITION_X = "WindowPositionX";
    private static final String WINDOW_POSITION_Y = "WindowPositionY";
    private static final String WINDOW_WIDTH = "WindowWidth";
    private static final String WINDOW_HEIGHT = "WindowHeight";
    private static final String MAXIMIZED = "Maximized";

    private static Button fileButton;
    private static TabPane tabPane;
    public  static AccountsTab accountsTab = new AccountsTab();
	public  static FiltersTab filtersTab = new FiltersTab();
	private static CategoriesTab categoriesTab = new CategoriesTab();
	private static ReportsTab reportsTab = new ReportsTab();
	private static SettingsPane settingsPane;
	private static AboutPane aboutPane = new AboutPane();
	private static boolean uiActive = false;
	private static boolean dataLoaded = false;
	

	public static DisplayServiceImplJavaFX getInstance() {
		if (instance == null) {
			instance = new DisplayServiceImplJavaFX();
		}
		return instance;
	}
	
	public static Stage getStage() {
		return stage;
	}
	
	public static String getCss() {
		return css;
	}
	
	@Override
	public void start(Stage theStage) throws Exception {
		appIcon = getIcon("/icons/Figures.png");
		addIcon = getIcon("/icons/Add.png");
		editIcon = getIcon("/icons/Edit.png");
		deleteIcon = getIcon("/icons/Delete.png");
		checkmarkIcon = getIcon("/icons/Checkmark.png");
		stage = theStage;
		stage.hide();
		getWindowSettings();

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(0, 0, 0, 0));
		borderPane.setMinWidth(stage.getWidth() * .90);
		borderPane.prefWidthProperty().bind(stage.widthProperty());
		borderPane.prefHeightProperty().bind(stage.heightProperty().multiply(.87));
		borderPane.maxHeightProperty().bind(stage.heightProperty().multiply(.87));

		scene = new Scene(borderPane, stage.getWidth(), stage.getHeight());
		CallResult result = loadCSS("/css/application.css");
		if (result.isGood()) {
			css = (String)result.getReturnedObject();
			scene.getStylesheets().add(css);
		}
		stage.setScene(scene);
		stage.setResizable(true);
		stage.setTitle("Figures");
		stage.getIcons().add(appIcon);
		stage.setOnCloseRequest(event -> {
			saveWindowSettings();
            System.exit(0);
		});
		stage.maximizedProperty().addListener((w,o,n)->stageMaximizeListener(n));

		HBox buttonBar = getButtonBar();
		buttonBar.setMaxHeight(10);
		borderPane.setTop(buttonBar);
		BorderPane.setMargin(buttonBar, new Insets(4, 0, 0, 5));
		
		tabPane = new TabPane();
		borderPane.setCenter(tabPane);
		BorderPane.setAlignment(tabPane, Pos.BOTTOM_LEFT);
		BorderPane.setMargin(tabPane, new Insets(0, 0, 0, 0));
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.maxHeightProperty().bind(borderPane.heightProperty().subtract(10));
		tabPane.prefWidthProperty().bind(borderPane.widthProperty().subtract(20));
		tabPane.maxWidthProperty().bind(borderPane.widthProperty().subtract(20));
		
		statusMessage = new TextArea();
		statusMessage.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(2))));
		statusMessage.setWrapText(true);
		statusMessage.prefWidthProperty().bind(tabPane.widthProperty().subtract(10));
		statusMessage.minWidthProperty().bind(tabPane.widthProperty().subtract(10));
		statusMessage.maxWidthProperty().bind(tabPane.widthProperty().subtract(10));
		statusMessage.setMinHeight(40);
		statusMessage.setMaxHeight(40);
		statusMessage.setText("");
		statusMessage.setEditable(false);
		statusMessage.setMouseTransparent(true);

		borderPane.setBottom(statusMessage);
		BorderPane.setMargin(statusMessage, new Insets(5, 5, 5, 5));
		
		result = checkForMultipleInstances();
		if (result.isBad()) return;
		checkAppSettings();
		result = setFileOpenConditions();
		if (result.isBad()) return;
		if (Figures.persistenceExpected){
			result = openCurrentExisting();
		}else {
		    result = createNew(false);
		}
		if (result.isBad()){
			if (result.getErrorMessage().contains("Cancelled")) {
				displayErrorMessage("Cancelled", result.getErrorMessage());
			}else if (!result.getErrorMessage().isEmpty()){
				displayErrorMessage("Error opening data file.", result.getErrorMessage());
			}
		}else{
			if (!uiActive) initializeUI();
			if (!dataLoaded) loadData();
		}
		if (dataLoaded) {
			Platform.runLater(()->tabPane.requestFocus());
		}else {
			Platform.runLater(()->fileButton.requestFocus());
		}
		Figures.logger.info("Initialized display service...");
		stage.show();
	}
	
	public static TabPane getTabPane() {
		return tabPane;
	}
	
	private CallResult setFileOpenConditions() { // Check for the default persistence folder and file
		CallResult result = new CallResult();
		if (!Figures.persistenceExpected){
			if (new File(Figures.DEFAULT_PERSISTENCE_NAME).exists()) {
				Figures.persistenceExpected = true;
				Figures.setProperty(Figures.PERSISTENCE_NAME, Figures.DEFAULT_PERSISTENCE_NAME);
			}
		}
		return result;
	}

	private CallResult checkExistingFile(String fileName) {
		CallResult result = new CallResult();
		boolean isValid = false;
		File file = new File(fileName);
		try {
			isValid = ServiceFactory.getInstance().getPersistenceSvc().isPersistenceValid(file);
		} catch (IOException e) {
			Figures.logStackTrace(e);
			return result.setCallBad(e.getLocalizedMessage());
		}
		if (isValid) {
			result = initializePersistence(fileName, null, false);
			Figures.setProperty(Figures.PERSISTENCE_ENCRYPTED, "false");
		}else {
			result = checkForPassword(fileName);
		}
		return result;
	}
	
	private CallResult checkForPassword(String fileName) {
		CallResult result = new CallResult();
	   	ButtonType yesButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		FxAlert passwordAlert = new FxAlert(AlertType.ERROR, "File Open Error", "Couldn't open " + fileName + ".\nIs this file password protected?",
			yesButton, noButton);
		passwordAlert.initOwner(stage);
		Optional<?> alertResult = passwordAlert.showAndWait();
		CallResult passwordResult = new CallResult();
		if (alertResult.isPresent() && alertResult.get().equals(yesButton)) {
			do {
				passwordResult = getPassword(false, passwordResult.isBad());
				if (passwordResult.isBad()) return passwordResult;
				passwordResult = initializePersistence(fileName, (String)passwordResult.getReturnedObject(), false);
			} while (passwordResult.isBad() && !passwordResult.getErrorMessage().contains("Cancelled"));
			if (passwordResult.isGood()) {
				Figures.setProperty(Figures.PERSISTENCE_NAME, fileName);
				Figures.setProperty(Figures.PERSISTENCE_ENCRYPTED, "true");
			}
		}else {
			displayInformationMessage("Invalid File", "This is not a valid Figures data file.");
			result.setCallBad();
		}
		return result;
	}

	private HBox getButtonBar() {
		HBox buttonBar = new HBox();
		fileButton = new Button("File");
		fileButton.setOnAction((ActionEvent event) -> {
			FileOpenOptionsPane fileOpenOptionsPane = new FileOpenOptionsPane();
			Scene scene = new Scene(fileOpenOptionsPane.getFileOpenOptionsPane());
			scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);
			stage.setTitle("Open File Options");
			stage.getIcons().add(DisplayServiceImplJavaFX.appIcon);
			stage.setOnHidden((WindowEvent event1) -> {
		        if (!fileOpenOptionsPane.isCanceled()) {
					if (fileOpenOptionsPane.isOpenNew()) {
						createNew(false);
					}else {
						openNewExisting();
					}
					Platform.runLater(()->tabPane.getSelectionModel().select(0));
				}
		    });
			stage.showAndWait();
		});

		buttonBar.getChildren().add(fileButton);

		Button settingsButton = new Button("Settings");
		settingsButton.setOnAction(getSettingsButtonHandler());

		buttonBar.getChildren().add(settingsButton);

		Button helpButton = new Button("Help");
		helpButton.setOnAction(getHelpButtonHandler());
		buttonBar.getChildren().add(helpButton);

		Button aboutButton = new Button("About");
		aboutButton.setOnAction(getAboutButtonHandler());
		buttonBar.getChildren().add(aboutButton);

		Button exitButton = new Button("Exit");
		exitButton.setOnAction((ActionEvent event) -> {
			saveWindowSettings();
			stage.close();
			System.exit(0);
		});
		buttonBar.getChildren().add(exitButton);
		return buttonBar;
	}
	
	private Image getIcon(String filePath) {
		CallResult result = ServiceFactory.getInstance().getFileSvc().getFileResource(filePath);
		if (result.isBad()) {
			Figures.logger.severe("Unable to load " + filePath);
			System.exit(1);
		}
		return new Image((InputStream)result.getReturnedObject());
	}
	
	private CallResult loadCSS(String filePath) {
		CallResult result = ServiceFactory.getInstance().getFileSvc().getFileResource(filePath);
		if (result.isBad()) {
			Figures.logger.severe("Unable to load " + filePath);
			System.exit(1);
		}
		InputStream inputStream = (InputStream)result.getReturnedObject();
		File tempFile = null;
		try {
			tempFile = File.createTempFile("Figures", "css");
			tempFile.deleteOnExit();
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			IOUtils.copy(inputStream, outputStream);
			inputStream.close();
		} catch (IOException e) {
			Figures.logStackTrace(e);
			return result.setCallBad(e.getLocalizedMessage());
		}
		URL url;
		try {
			url = tempFile.toURI().toURL();
		} catch (MalformedURLException e) {
			Figures.logStackTrace(e);
			return result.setCallBad(e.getLocalizedMessage());
		}
		return result.setReturnedObject(url.toExternalForm());
	}

	@Override
	public CallResult initialize() {
		CallResult result = new CallResult();
		try {
			launch();
		} catch (Exception e) {
			Figures.logStackTrace(e);
			result.setCallBad("Display Service Initialization Failed", e.getLocalizedMessage());
		}
		return result;
	}
	
	@Override
	public CallResult initializePersistence(String fileName, String password, boolean isNew) {
		CallResult result = ServiceFactory.getInstance().getPersistenceSvc().initialize(fileName, password, isNew);
		if (result.isBad()) return result;
		if (isNew) result = ServiceFactory.getInstance().getPersistenceSvc().createDefaultData();
		return result;
	}
	
	private void initializeUI() {
		Tab acctTab = null;
		Tab filterTab = null;
		Tab categoryTab = null;
		Tab reportTab = null;
		try {
			acctTab = accountsTab.getAccountsTab();
			tabPane.getTabs().add(acctTab);
		} catch (Exception e) {
			handleException("Initialization Error", "Error loading Accounts", e, stage);
			return;
		}
		try {
			filterTab = filtersTab.getFiltersTab();
			tabPane.getTabs().add(filterTab);
		} catch (Exception e) {
			handleException("Initialization Error", "Error loading Filters", e, stage);
		}
		try {
			categoryTab = categoriesTab.getCategoriesTab(); 
			tabPane.getTabs().add(categoryTab);
		} catch (Exception e) {
			handleException("Initialization Error", "Error loading Categories", e, stage);
		}
		try {
			reportTab = reportsTab.getReportsTab();
			tabPane.getTabs().add(reportTab);
		} catch (Exception e) {
			handleException("Initialization Error", "Error loading Categories", e, stage);
		}
		scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
		tabPane.requestLayout();
        MouseEvent mouseClick = new MouseEvent(MouseEvent.MOUSE_CLICKED, 10, 10, 10, 10, 
            	MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
        Platform.runLater(()->tabPane.requestFocus());
        Platform.runLater(()->Event.fireEvent(tabPane, mouseClick));
		uiActive = true;
	}
	
	private CallResult openCurrentExisting() {
		CallResult result = new CallResult();
		String fileName = Figures.getProperty(Figures.PERSISTENCE_NAME);
		boolean isEncrypted = Boolean.valueOf(Figures.getProperty(Figures.PERSISTENCE_ENCRYPTED));
		if (isEncrypted) {
			do {
				result = getPassword(false, result.isBad());
				if (result.isBad()) return result;
				result = initializePersistence(fileName, (String)result.getReturnedObject(), false);
			} while (result.isBad() && !result.getErrorMessage().contains("Cancelled"));
			if (result.isBad()) return result;
		}else {
			result = initializePersistence(fileName, null, false);
		}
		return result;
	}
	
	private CallResult openNewExisting() {
		CallResult result = null;
		String persistenceFolder = "";
		if (Figures.persistenceExpected) {
			String persistenceName = Figures.getProperty(Figures.PERSISTENCE_NAME);
			persistenceFolder = persistenceName.substring(0, persistenceName.lastIndexOf("\\"));
		}
		result = getInputFileName(persistenceFolder, "Open Figures Data File");
		if (result.isBad()) return result;
		String fileName = ((File)result.getReturnedObject()).getAbsolutePath();
		result = checkExistingFile(fileName);
		if (result.isGood()) {
			if (!uiActive) {
				initializeUI();
			}
			resetForNewFile();
			result = loadData();
			if (result.isGood()) {
				setStatusGood("Current data file: " + fileName);
				Figures.setProperty(Figures.PERSISTENCE_NAME, fileName);
			}else {
				setStatusBad("Could not open " + fileName + ".  Error: " + result.getErrorMessage());
			}
		}else {
			setStatusBad("Could not open " + fileName + ".  Error: " + result.getErrorMessage());
		}
		return result;
	}
	
	private void resetForNewFile() {
		Figures.resetForNewFile();
		accountsTab.resetForNewFile();
		filtersTab.resetForNewFile();
		reportsTab.resetForNewFile();
	}
	
	private CallResult createNew(boolean showPrompt)  {
		CallResult result = new CallResult();
		File figuresFile = null;
		if (!Figures.persistenceExpected) { // First time use
			figuresFile = new File(Figures.DEFAULT_PERSISTENCE_NAME);
		}
		if (figuresFile == null) {
			if (showPrompt) {
				displayInformationMessage("Create Figures Data File", "Choose a location for the Figures data file.\nClick OK to continue.");
			}
			figuresFile = getUserFile();
			if (figuresFile == null) {
				return result.setCallBad("Figures data file not created.");
			}else {
				if (figuresFile.exists()) {
					figuresFile.delete();
				}
			}
		}
	   	ButtonType yesButton = new ButtonType("Yes");
		ButtonType noButton = new ButtonType("No");
		FxAlert passwordAlert = new FxAlert(AlertType.CONFIRMATION, "Password Protection?", "Do you want to protect your Figures file with a password?", yesButton, noButton);
		passwordAlert.initOwner(stage);
		passwordAlert.setHeight(40);
		Optional<?> passwordResult = passwordAlert.showAndWait();
		String password = null;
		if (passwordResult.isPresent() && passwordResult.get().equals(yesButton)) {
			result = getPassword(true, false);
			if (result.isBad()) return result;
			password = (String)result.getReturnedObject();
			Figures.setProperty(Figures.PERSISTENCE_ENCRYPTED, "true");
		}
		result = initializePersistence(figuresFile.getAbsolutePath(), password, true);
		if (result.isGood()) {
			if (!uiActive) initializeUI();
			resetForNewFile();
			loadData();
			Figures.setProperty(Figures.PERSISTENCE_NAME, figuresFile.getAbsolutePath());
			setStatusGood("Current data file: " + figuresFile.getName());
		}
		return result;
	}	
		
	private File getUserFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Create Figures Data File");
		String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		fileChooser.setInitialDirectory(new File(currentPath.substring(0, 3)));
		fileChooser.setInitialFileName("Figures.db");
		File file = fileChooser.showSaveDialog(stage);
		return file;
	}
	
	private CallResult getPassword(boolean firstTime, boolean invalidPassword) {
		CallResult result = new CallResult();
		PasswordPane passwordPane = new PasswordPane();
		Scene scene;
		if (firstTime) {
			scene = new Scene(passwordPane.getPasswordPane(firstTime, invalidPassword), PasswordPane.NEW_WIDTH, PasswordPane.NEW_HEIGHT);
		}else {
			scene = new Scene(passwordPane.getPasswordPane(firstTime, invalidPassword), PasswordPane.WIDTH, PasswordPane.HEIGHT);
		}
		scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.setTitle("Enter Password");
		stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
		stage.setOnHidden((WindowEvent event1) -> {
	        if (passwordPane.isCanceled()) {
	        	result.setCallBad("Cancelled opening data file.");
	        }else {
				result.setReturnedObject(passwordPane.getPassword());
	        }
	    });
		DisplayServiceImplJavaFX.center(stage, scene);
		stage.showAndWait();
		return result;
	}
	
	public CallResult loadData() {
		CallResult result = accountsTab.loadAccounts();
		if (result.isBad()) {
			setStatusBad("Could not load accounts from data file.");
			return result;
		}
		result = filtersTab.loadFilterSets();
		if (result.isBad()) {
			setStatusBad("Could not load filter sets from data file.");
			return result;
		}
		result = categoriesTab.loadCategories();
		if (result.isBad()) {
			setStatusBad("Could not load categories from data file.");
			return result;
		}
		dataLoaded = true;
		return result;
	}

	@Override
	public CallResult getInputFileName(String folder, String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		if (!StringUtils.isEmpty(folder)) {
			File fileFolder = new File(folder);
			if (fileFolder.exists()){
				fileChooser.setInitialDirectory(fileFolder);
			}
		}
		File fileSelected = fileChooser.showOpenDialog(stage);
		CallResult result = new CallResult();
		if (fileSelected != null) {
			result.setReturnedObject(fileSelected);
		}else {
			result.setCallBad("File Selection Failure", "No file selected.");
		}
		return result;
	}
	
	@Override
	public CallResult getOutputFileName(String folder, String fileName, String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		if (! StringUtils.isBlank(folder)) {
			File fileFolder = new File(folder);
			if (fileFolder.exists()) fileChooser.setInitialDirectory(fileFolder);
		}
		if (! StringUtils.isBlank(fileName)) {
			fileChooser.setInitialFileName(fileName);
		}
		File fileSelected = fileChooser.showSaveDialog(stage);
		CallResult result = new CallResult();
		if (fileSelected != null) {
			result.setReturnedObject(fileSelected);
		}else {
			result.setCallBad("File Selection Failure", "No file selected.");
		}
		return result;
	}
	
	private EventHandler<ActionEvent> getSettingsButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clearStatus();
				settingsPane = new SettingsPane();
				Scene scene = new Scene(settingsPane.getSettingsPane(), SettingsPane.WIDTH+20, SettingsPane.HEIGHTH+20);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("Settings");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
				// Set focus on base grid rather than any particular control
				Platform.runLater(()->settingsPane.getBasePane().requestFocus());
			}
		};
	}
	
	private EventHandler<ActionEvent> getHelpButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				displayHelp("Figures.html");
			}
		};
	}
	
	private EventHandler<ActionEvent> getAboutButtonHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Scene scene = new Scene(aboutPane.getAboutPane(), AboutPane.WIDTH+20, AboutPane.HEIGHTH+20);
				scene.getStylesheets().add(DisplayServiceImplJavaFX.getCss());
				Stage stage = new Stage();
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setScene(scene);
				stage.setTitle("About Figures");
				stage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
				DisplayServiceImplJavaFX.center(stage, scene);
				stage.show();
			}
		};
	}

	
	@Override
	public void displayHelp(String fileName) {
		File file = new File("/Figures/Help/" + fileName);
		if (!file.exists()) {
			setStatusBad("Help page not found: " + fileName);
			return;
		}
		Stage helpStage = new Stage();
		helpStage.initModality(Modality.NONE);
		helpStage.getIcons().add(DisplayServiceImplJavaFX.appIcon); 
		helpStage.setTitle("Figures Help");
		helpStage.setWidth(DisplayServiceImplJavaFX.getStage().getWidth()-40);
		helpStage.setHeight(DisplayServiceImplJavaFX.getStage().getHeight()-40);
		helpStage.setX(DisplayServiceImplJavaFX.getStage().getX()+20);
		helpStage.setY(DisplayServiceImplJavaFX.getStage().getY()+20);
		WebView webView = new WebView();
		Scene scene = new Scene(webView);
		helpStage.setScene(scene);
		try {
			URL url= file.toURI().toURL();
			webView.getEngine().load(url.toString());
			helpStage.show();
			Platform.runLater(()->helpStage.setAlwaysOnTop(true));
			Platform.runLater(()->helpStage.setAlwaysOnTop(false));
		} catch (MalformedURLException e) {
			Figures.logStackTrace(e);
			setStatusBad("Help system error: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void setStatusGood(String message) {
		statusMessage.setStyle("-fx-text-fill: #000000; -fx-border-color: lightgreen;");
		statusMessage.setText(message);
		statusMessage.requestFocus();
	}

	@Override
	public void setStatusHelp(String message) {
		statusMessage.setStyle("-fx-text-fill: #0000FF; -fx-border-color: lightgreen;");
		statusMessage.setText(message);
		statusMessage.requestFocus();
	}

	@Override
	public void setStatusBad(String message) {
		Toolkit.getDefaultToolkit().beep();
		statusMessage.setStyle("-fx-text-fill: #000000; -fx-faint-focus-color: red;");
		statusMessage.setText(message);
		statusMessage.requestFocus();
	}

	@Override
	public void setStatusBad(CallResult result) {
		Toolkit.getDefaultToolkit().beep();
		statusMessage.setStyle("-fx-text-fill: #000000; -fx-faint-focus-color: red;");
		statusMessage.setText(result.getMessageDecorator() + ": " + result.getErrorMessage());
		statusMessage.requestFocus();
	}
	
	public void clearStatus() {
		statusMessage.setText("");
		statusMessage.setStyle("-fx-text-fill: #000000; -fx-border-color: black;");
	}
	
	@Override
	public void setCursor(CursorType cursorType) {
		if (cursorType.equals(CursorType.WAIT)) {
			stage.getScene().setCursor(Cursor.WAIT);
		}else {
			stage.getScene().setCursor(Cursor.DEFAULT);
		}
	}

	public void handleException(String frameTitle, String messageTitle, Exception e, Window window) {
		if (e.getLocalizedMessage() == null) {
			Figures.logStackTrace(e);
			displayErrorMessage("Error loading Accounts", e.toString());
		}else {
			Figures.logStackTrace(e);
			displayErrorMessage("Error loading Accounts", e.getLocalizedMessage());
		}
	}
	
	@Override
	public void displayErrorMessage(String title, String messageText) {
		if (!stage.isShowing()) stage.show();
		Toolkit.getDefaultToolkit().beep();
		FxAlert alert = new FxAlert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(messageText);
		alert.showAndWait();
	}
	
	public void displayInformationMessage(String title, String messageText) {
		Toolkit.getDefaultToolkit().beep();
		FxAlert alert = new FxAlert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(messageText);
		alert.showAndWait();
	}
	
	public static void center(Stage stageToCenter, Scene scene) {
		stageToCenter.setX(stage.getX() + ((stage.getWidth() - scene.getWidth())/2));
		stageToCenter.setY(stage.getY() + ((stage.getHeight() - scene.getHeight())/2));
	}
		
	private void getWindowSettings() {
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double screenWidth = screenBounds.getWidth();
		double screenHeight = screenBounds.getHeight();
		String maximized = Figures.getProperty(MAXIMIZED);
		if (maximized != null) {
			stage.setMaximized(true);
			stage.setWidth(screenWidth);
			stage.setHeight(screenHeight);
			return;
		}
		String windowPositionX = Figures.getProperty(WINDOW_POSITION_X);
		String windowPositionY = Figures.getProperty(WINDOW_POSITION_Y);
		String widthProperty = Figures.getProperty(WINDOW_WIDTH);
		String heightProperty = Figures.getProperty(WINDOW_HEIGHT);
		if (windowPositionX == null || windowPositionY == null || widthProperty == null || heightProperty == null) {
			setStageToDefaults(screenWidth, screenHeight, true);
			return;
		}
		try {
			Double width = Double.valueOf(widthProperty);
			if (width > 0 && width <= screenWidth) {
				stage.setWidth(width);
			}else {
				setStageToDefaults(screenWidth, screenHeight, true);
				return;
			}
		} catch (NumberFormatException e1) {
			setStageToDefaults(screenWidth, screenHeight, true);
		}
		try {
			Double height = Double.valueOf(heightProperty);
			if (height > 0 && height <= screenHeight) {
				stage.setHeight(height);
			}else {
				setStageToDefaults(screenWidth, screenHeight, true);
			}
		} catch (NumberFormatException e1) {
			setStageToDefaults(screenWidth, screenHeight, true);
		}
		try {
			Double positionX = Double.valueOf(windowPositionX);
			if (positionX > 0 && (positionX + stage.getWidth()) < screenWidth) {
				stage.setX(positionX);
			}else {
				setStageToDefaults(screenWidth, screenHeight, true);
			}
		} catch (NumberFormatException e) {
			setStageToDefaults(screenWidth, screenHeight, true);
		}
		try {
			Double positionY = Double.valueOf(windowPositionY);
			if (positionY > 0 && (positionY + stage.getHeight()) < screenHeight) {
				stage.setY(positionY);
			}else {
				setStageToDefaults(screenWidth, screenHeight, true);
			}
		} catch (NumberFormatException e) {
			setStageToDefaults(screenWidth, screenHeight, true);
		}
	}
	
	private void stageMaximizeListener(boolean maximized) {
		if (! maximized) {
			Rectangle2D screenBounds = Screen.getPrimary().getBounds();
			setStageToDefaults(screenBounds.getWidth(), screenBounds.getHeight(), false);
			tabPane.requestLayout();
		}
	}
	
	private void setStageToDefaults(double screenWidth, double screenHeight, boolean allowMaximize) {
		if (screenHeight < PREFERRED_HEIGHT) {
			if (allowMaximize) stage.setMaximized(true);
			stage.setWidth(screenWidth * .98);
			stage.setHeight(screenHeight * .98);
			stage.setX(0);
			stage.setY(3);
			return;
		}
		if (screenWidth > PREFERRED_WIDTH) {
			stage.setX((screenWidth - PREFERRED_WIDTH) / 2);
			stage.setWidth(PREFERRED_WIDTH);
		}else {
			stage.setX(0);
			stage.setWidth(screenWidth);
		}
		stage.setY(3);
		if (screenHeight > PREFERRED_HEIGHT) {
			stage.setHeight(PREFERRED_HEIGHT); 
		}else {
			stage.setHeight(screenHeight);
		}
	}
	
	private void saveWindowSettings() {
		if (stage.isMaximized()) {
			Figures.setProperty(MAXIMIZED, "true");
			Figures.removeProperty(WINDOW_POSITION_X);
			Figures.removeProperty(WINDOW_POSITION_Y);
			Figures.removeProperty(WINDOW_WIDTH);
			Figures.removeProperty(WINDOW_HEIGHT);			
		}else {
			Figures.removeProperty(MAXIMIZED);
			Figures.setProperty(WINDOW_POSITION_X, Double.valueOf(stage.getX()).toString());
			Figures.setProperty(WINDOW_POSITION_Y, Double.valueOf(stage.getY()).toString());
			Figures.setProperty(WINDOW_WIDTH, Double.valueOf(stage.getWidth()).toString());
			Figures.setProperty(WINDOW_HEIGHT, Double.valueOf(stage.getHeight()).toString());			
		}
	}
	
	private CallResult checkForMultipleInstances() {
		CallResult result = Figures.checkForMultipleInstances();
		if (result.isBad()) {
			displayInformationMessage("Multiple Instances Running", "Another instance of Figures is already running.");
			System.exit(0);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void checkAppSettings() {
		CallResult result = Figures.validateProperties();
		if (result.isBad()) {
			List<String> properties = (List<String>)result.getReturnedObject();
			String propertyList = properties.get(0);
			for (int i = 1; i < properties.size(); i++) {
				propertyList = propertyList + ", " + properties.get(i); 
			}
			displayInformationMessage("Invalid Property Reset To Default", "The following properties were reset..." + propertyList);
		}
	}

}
