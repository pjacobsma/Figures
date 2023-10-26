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

package org.bluewindows.figures.app;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.StringUtils;
import org.bluewindows.figures.domain.CallResult;
import org.bluewindows.figures.domain.Category;
import org.bluewindows.figures.service.ServiceFactory;
import org.bluewindows.figures.service.impl.ExportServiceImpl;
import org.bluewindows.figures.service.impl.FileServiceImpl;
import org.bluewindows.figures.service.impl.ImportServiceImpl;
import org.bluewindows.figures.service.impl.MaintenanceServiceImpl;
import org.bluewindows.figures.service.impl.PersistenceServiceImpl;
import org.bluewindows.figures.service.impl.jasper.ReportServiceImplJasper;
import org.bluewindows.figures.service.impl.javafx.DisplayServiceImplJavaFX;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

public class Figures {

	public static final String VERSION = "1.1";
	public static final Integer DATABASE_VERSION = Integer.valueOf(2);
	public static final String PROPERTY_FILE_NAME = "Figures.properties";
	public static final String PERSISTENCE_TYPE = "PersistenceType";
	public static final String PERSISTENCE_NAME = "PersistenceName";
	public static final String PERSISTENCE_ENCRYPTED = "PersistenceEncrypted";
	public static final String DISPLAY_TYPE = "DisplayType";
	public static final String DATE_FORMAT_NAME = "DateFormat";
	public static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
	public static final List<String> STANDARD_DATE_LIST = new ArrayList<String>();
	public static final String DEFAULT_PERSISTENCE_NAME = "\\Figures\\Figures.db";
	public static final String DEFAULT_DATA_FOLDER = "\\Figures";
	public static final String DEFAULT_HELP_FOLDER = "\\Figures\\Help";
	public static String HELP_FOLDER;
	public static final Map<String, DateTimeFormatter> DATE_FORMAT_MAP = new HashMap<String, DateTimeFormatter>();
	public static final List<Category> DEFAULT_CATEGORIES = new ArrayList<Category>();
	public static DateTimeFormatter dateFormat;
	public static NumberFormat currencyFormat;
	public static String currencySymbol;
	public static String fontName;
	public static boolean persistenceExpected = false;
	public static Properties properties;
	public static Logger logger;
	public static Timestamp accountTimestamp = new Timestamp(System.currentTimeMillis());
	public static Timestamp categoryTimestamp = new Timestamp(System.currentTimeMillis());
	public static Timestamp filterTimestamp = new Timestamp(System.currentTimeMillis());
	public static Timestamp filterSetTimestamp = new Timestamp(System.currentTimeMillis());
	public static Timestamp transactionTimestamp = new Timestamp(System.currentTimeMillis());
	public static Timestamp reportTimestamp = new Timestamp(System.currentTimeMillis());
	
	static {
		STANDARD_DATE_LIST.add("MM/dd/yyyy");
		STANDARD_DATE_LIST.add("MM-dd-yyyy");
		STANDARD_DATE_LIST.add("yyyy/MM/dd");
		STANDARD_DATE_LIST.add("yyyy-MM-dd");
		STANDARD_DATE_LIST.add("yyyy.MM.dd");
		STANDARD_DATE_LIST.add("dd/MM/yyyy");
		STANDARD_DATE_LIST.add("dd-MM-yyyy");
		STANDARD_DATE_LIST.add("dd.MM.yyyy");
		for (String dateFormat : STANDARD_DATE_LIST) {
			DATE_FORMAT_MAP.put(dateFormat, DateTimeFormatter.ofPattern(dateFormat));
		}
		
		DEFAULT_CATEGORIES.add(Category.NONE);
		DEFAULT_CATEGORIES.add(new Category(1, "Appliances"));
		DEFAULT_CATEGORIES.add(new Category(2, "Car Payment"));
		DEFAULT_CATEGORIES.add(new Category(3, "Car Insurance"));
		DEFAULT_CATEGORIES.add(new Category(4, "Car Maintenance"));
		DEFAULT_CATEGORIES.add(new Category(5, "Charity"));
		DEFAULT_CATEGORIES.add(new Category(6, "Clothing"));
		DEFAULT_CATEGORIES.add(new Category(7, "Dining Out"));
		DEFAULT_CATEGORIES.add(new Category(8, "Furniture"));
		DEFAULT_CATEGORIES.add(new Category(9, "Gas"));
		DEFAULT_CATEGORIES.add(new Category(10, "Gifts"));
		DEFAULT_CATEGORIES.add(new Category(11, "Groceries"));
		DEFAULT_CATEGORIES.add(new Category(12, "Healthcare"));
		DEFAULT_CATEGORIES.add(new Category(13, "Home Insurance"));
		DEFAULT_CATEGORIES.add(new Category(14, "Mortgage"));
		DEFAULT_CATEGORIES.add(new Category(15, "Phones"));
		DEFAULT_CATEGORIES.add(new Category(16, "Software"));
		DEFAULT_CATEGORIES.add(new Category(17, "TV/Internet"));
		DEFAULT_CATEGORIES.add(new Category(18, "Utilities"));
		DEFAULT_CATEGORIES.add(new Category(19, "Vacation/Travel"));
	}
	
	public static void main(String[] args) {
		try {
			checkDefaultDataFolder();
			checkDefaultHelpFolder();
			configureLogging();
			setReportFont();
		} catch (Exception e) {
			logStackTrace(e);
			System.exit(1);
		}
//		Locale.setDefault(new Locale("en", "IE")); // Euros
//		Locale.setDefault(new Locale("en", "GB")); // Pounds
		logger.info("Starting Figures...");
		currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
		currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
		ServiceFactory.getInstance().setFileSvc(new FileServiceImpl());
		CallResult result = ServiceFactory.getInstance().getFileSvc().loadProperties(PROPERTY_FILE_NAME);
		if (result.isGood()){
			properties = (Properties) result.getReturnedObject();
			if (properties.entrySet() != null && properties.entrySet().size() > 0) {
				logger.info("Loaded properties...");
				dateFormat = DATE_FORMAT_MAP.get(properties.get(DATE_FORMAT_NAME));
			}else {
				logger.info("Initialized properties...");
				initializeProperties();
			}
		}else{
			logger.info("Initialized properties...");
			initializeProperties();
		}
		if (!StringUtils.isEmpty(getProperty(PERSISTENCE_NAME))) {
			persistenceExpected = true;
		}
		initializeTheServices();
	}
	
	private static void checkDefaultDataFolder() {
		Path defaultDataPath = Paths.get(DEFAULT_DATA_FOLDER);
		if (!Files.exists(defaultDataPath)) {
			try {
				Files.createDirectory(defaultDataPath);
			} catch (IOException e) {
				logStackTrace(e);
				System.exit(1);
			}
		}
	}
	
	private static void checkDefaultHelpFolder() {
		Path defaultHelpPath = Paths.get(DEFAULT_HELP_FOLDER);
		if (!Files.exists(defaultHelpPath)) { // Running in Eclipse without installed app
			Path currentRelativePath = Paths.get("");
			String currentPath = currentRelativePath.toAbsolutePath().toString();
			HELP_FOLDER = currentPath + "\\HelpNDoc\\Output\\Build html documentation";
		}else {
			HELP_FOLDER = DEFAULT_HELP_FOLDER;
		}
	}
	
	public static void configureLogging() throws SecurityException, IOException {
		logger = Logger.getLogger(Figures.class.getName());
		logger.setLevel(Level.ALL);
		Handler[] handlers = logger.getHandlers();
		if (handlers.length == 0) {
			Path defaultDataPath = Paths.get(DEFAULT_DATA_FOLDER);
			FileHandler fileLogger = null;
			if (Files.exists(defaultDataPath)) {
				fileLogger = new FileHandler(DEFAULT_DATA_FOLDER + "\\Figures.log", true);
			}else { // Create dummy file logger for unit tests
				fileLogger = new FileHandler(File.createTempFile("Figures", "log").getAbsolutePath());
			}
			fileLogger.setFormatter(new SimpleFormatter());
			fileLogger.setFormatter(new SimpleFormatter() {
		          private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
		          @Override
		          public synchronized String format(LogRecord lr) {
		              return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage()
		              );
		          }
		      });
			logger.addHandler(fileLogger); 
		}
	}

	private static void setReportFont() {
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	    for (int i = 0; i < fonts.length; i++) {
	    	if (fonts[i].trim().equals("Arial")) {
	    		fontName = "Arial";
	    		return;
	    	}
	    }
	    for (int i = 0; i < fonts.length; i++) {
	    	if (fonts[i].trim().equals("Helvetica")) {
	    		fontName = "Helvetica";
	    		return;
	    	}
		}
	    for (int i = 0; i < fonts.length; i++) {
	    	if (fonts[i].trim().equals("Tahoma")) {
	    		fontName = "Tahoma";
	    		return;
	    	}
		}
	    for (int i = 0; i < fonts.length; i++) {
	    	if (fonts[i].trim().equals("Calibri")) {
	    		fontName = "Calibri";
	    		return;
	    	}
		}
	    logger.severe("Cannot find a usable system font.  Need Arial, Helvetica, or Tahoma.");
	    System.exit(1);
	}
	
	public static void logStackTrace(Exception ex) {
		Writer buffer = new StringWriter();
		PrintWriter pw = new PrintWriter(buffer);
		ex.printStackTrace(pw);
		logger.severe(buffer.toString());
	}
	
	private static void initializeProperties() {
		properties = new Properties();
		CallResult result = ServiceFactory.getInstance().getFileSvc().getFileOutputStream(PROPERTY_FILE_NAME, true);
		if (result.isBad()) {
			logger.info("Cannot create property file: " + PROPERTY_FILE_NAME);
			exit();
		}
		result = setProperty(PERSISTENCE_TYPE, PersistenceType.SQLITE.name());
		if (result.isBad()) {
			logger.info("Cannot write to property file: " + PROPERTY_FILE_NAME);
			exit();
		}
		result = setProperty(DISPLAY_TYPE, DisplayType.JAVAFX.getName());
		if (result.isBad()) {
			logger.info("Cannot write to property file: " + PROPERTY_FILE_NAME);
			exit();
		}
		String localPattern = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())).toLocalizedPattern();
		localPattern = StringUtils.replace(localPattern, "Y", "y");
		localPattern = StringUtils.replace(localPattern, "D", "d");
		dateFormat = DATE_FORMAT_MAP.get(localPattern);
		if (dateFormat == null) {
			result = setProperty(DATE_FORMAT_NAME, DEFAULT_DATE_FORMAT);
			dateFormat = DATE_FORMAT_MAP.get(DEFAULT_DATE_FORMAT);
		}else {
			result = setProperty(DATE_FORMAT_NAME, localPattern);
		}
		if (result.isBad()) {
			logger.info("Cannot write to property file: " + PROPERTY_FILE_NAME);
			exit();
		}
	}
	
	public static CallResult checkForMultipleInstances()  {
		CallResult result = new CallResult();
		try {
			JUnique.acquireLock(Figures.class.getName(), null);
		} catch (AlreadyLockedException e) {
			result.setCallBad();
		}
		return result;
	}
	
	public static CallResult validateProperties() {
		CallResult result = new CallResult();
		List<String> propertyList = new ArrayList<String>();
		result.setReturnedObject(propertyList);
		String property = getProperty(PERSISTENCE_TYPE);
		if (property == null || (!property.equals(PersistenceType.SQLITE.name()))){
			setProperty(PERSISTENCE_TYPE, PersistenceType.SQLITE.name());
			propertyList.add(PERSISTENCE_TYPE);
			result.setCallBad();
		}
		property = getProperty(DISPLAY_TYPE);
		if (property == null || (!property.equals(DisplayType.JAVAFX.getName()))) {
			setProperty(DISPLAY_TYPE, DisplayType.JAVAFX.getName());
			propertyList.add(DISPLAY_TYPE);
			result.setCallBad();
		}
		property = getProperty(DATE_FORMAT_NAME);
		if (DATE_FORMAT_MAP.get(property) == null) {
			setProperty(DATE_FORMAT_NAME, DEFAULT_DATE_FORMAT);
			propertyList.add(DATE_FORMAT_NAME);
			result.setCallBad();
		}
		return result;
	}

	public static void initializeTheServices() {
		logger.info("Initializing services...");
		String persistenceTypeName = getProperty(PERSISTENCE_TYPE);
		if (StringUtils.isEmpty(persistenceTypeName)) {
			logger.info("Persistence type not found in properties file.");
			exit();
		}
		if (persistenceTypeName.equals(PersistenceType.SQLITE.name())) {
			ServiceFactory.getInstance().setPersistenceSvc(PersistenceServiceImpl.getInstance());
			ServiceFactory.getInstance().setUpPersistenceSvcSqlite();
		}else {
			logger.info("Illegal persistence type: " +  persistenceTypeName + " found in properties file.");
			exit();
		}
		String displayTypeName = properties.getProperty(DISPLAY_TYPE);
		if (StringUtils.isEmpty(displayTypeName)){
			logger.info("Display type not found in properties file.");
			exit();
		}
		if (displayTypeName.equals(DisplayType.JAVAFX.getName())) {
			ServiceFactory.getInstance().setDisplaySvc(DisplayServiceImplJavaFX.getInstance());
		}else {
			logger.info("Illegal display type: " +  displayTypeName + " found in properties file.");
			exit();
		}
		ServiceFactory.getInstance().setReportSvc(ReportServiceImplJasper.getInstance());
		ServiceFactory.getInstance().setMaintenanceSvc(MaintenanceServiceImpl.getInstance());
		ServiceFactory.getInstance().setImportSvc(ImportServiceImpl.getInstance());
		ServiceFactory.getInstance().setExportSvc(ExportServiceImpl.getInstance());
		CallResult result = ServiceFactory.getInstance().getDisplaySvc().initialize();
		if (result.isBad()) {
			logger.info("Could not initialize the display service.");
			exit();
		}
	}
	
	public static void resetForNewFile() {
		accountTimestamp = new Timestamp(System.currentTimeMillis());
		categoryTimestamp = new Timestamp(System.currentTimeMillis());
		filterTimestamp = new Timestamp(System.currentTimeMillis());
		filterSetTimestamp = new Timestamp(System.currentTimeMillis());
		transactionTimestamp = new Timestamp(System.currentTimeMillis());
		reportTimestamp = new Timestamp(System.currentTimeMillis());
	}
	
	public static void setProperties(Properties props) {
		properties = props;
	}
		
	public static String getProperty(String key){
		return properties.getProperty(key);
	}

	public static CallResult setProperty(String key, String value){
		properties.setProperty(key, value);
		return ServiceFactory.getInstance().getFileSvc().storeProperties(PROPERTY_FILE_NAME, properties);
	}

	public static CallResult removeProperty(String key){
		properties.remove(key);
		return ServiceFactory.getInstance().getFileSvc().storeProperties(PROPERTY_FILE_NAME, properties);
	}

	public static void exit(){
		if (ServiceFactory.getInstance().getPersistenceSvc() != null) ServiceFactory.getInstance().getPersistenceSvc().close();
		logger.getHandlers()[0].close();
		System.exit(0);
	}
}
