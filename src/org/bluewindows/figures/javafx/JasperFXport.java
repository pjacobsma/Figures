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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import org.bluewindows.figures.app.Figures;
import org.bluewindows.figures.service.ServiceFactory;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JROrigin;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JROriginExporterFilter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.type.BandTypeEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

public class JasperFXport {

	private JasperPrint jasperPrint;
	private Stage stage;
	private Scene scene;
	private Pane displayPane;
	private Float zoom;
	private String css;
	private Slider pageSlider;
	private WebView webView;
	private double displayWidth;
	private double displayHeight;
	private Double previousValue = (double) 0;
	private Double scrollDeltaY = (double) 0;
	private Double keyDeltaY = (double) 0;

	/**
	* Provides a native JavaFX print/export UI for a Jasper Report.
	*
	* @param  jasperPrint The Jasper print object you want to print/export.
	* @param  stage The stage to display the print/export UI.
	* @param  zoom The zoom value to apply to size the Jasper page images.
	* @param  css Optional css string to apply to the UI.
	*/
	public JasperFXport(JasperPrint jasperPrint, Stage stage, Float zoom, String css) {
		if (jasperPrint == null) {
			throw new IllegalArgumentException("JasperPrint cannot be null");
		}
		if (stage == null) {
			throw new IllegalArgumentException("Stage cannot be null");
		}
		if (zoom == null) {
			throw new IllegalArgumentException("Scale cannot be null");
		}
		this.jasperPrint = jasperPrint;
		this.stage = stage;
		this.zoom = zoom;
		this.css = css;
		displayWidth = stage.getWidth();
		displayHeight = stage.getHeight();
	}

	/**
	* Provides a native JavaFX print/export UI for a Jasper Report.
	*
	* @param  jasperPrint The Jasper print object you want to print/export.
	* @param  displayPane The ScrollPane to display the print/export UI.
	* @param  zoom The zoom value to apply to size the Jasper page images.
	* @param  css Optional css string to apply to the UI.
	* @param  width The desired width of the report display.
	* @param  height The desired height of the report display.
	*/
	public JasperFXport(JasperPrint jasperPrint, Pane displayPane, Float zoom, String css, double width, double height) {
		if (jasperPrint == null) {
			throw new IllegalArgumentException("JasperPrint cannot be null");
		}
		if (displayPane == null) {
			throw new IllegalArgumentException("Pane cannot be null");
		}
		if (zoom == null) {
			throw new IllegalArgumentException("Scale cannot be null");
		}
		this.jasperPrint = jasperPrint;
		this.displayPane = displayPane;
		this.zoom = zoom;
		this.css = css;
		displayWidth = width;
		displayHeight = height;
	}
	
	/**
	* Build and show the print/export UI.
	 * @throws IOException 
	*/
	public void show() throws IOException {
		webView = new WebView();
		generatePageImage(0);
		createPreview();
	}

	private void generatePageImage(int pageNumber) {
		webView.getEngine().loadContent(generateHtml(Integer.valueOf(pageNumber)));
		Platform.runLater(()->webView.requestFocus());

	}
	
	private String generateHtml(Integer pageNumber) {
		HtmlExporter exporter = new HtmlExporter();
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(outputStream, "UTF-8");
		exporter.setExporterOutput(output);
		SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
		if (pageNumber != null) reportConfig.setPageIndex(pageNumber.intValue());
		reportConfig.setZoomRatio(zoom);
		reportConfig.setEmbeddedSvgUseFonts(true);
		reportConfig.setAccessibleHtml(true);
		exporter.setConfiguration(reportConfig);
		try {
			exporter.exportReport();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return outputStream.toString();
	}
		
	private void createPreview() throws IOException {
		if (stage != null) {
			displayPane = new Pane();
			scene = new Scene(displayPane);
			if (css != null) scene.getStylesheets().add(css);
			stage.setScene(scene);
		}
		displayPane.setOnKeyPressed((KeyEvent event) -> {
			if (scene == null) {
				event.consume();
			}else {
				if (scene.focusOwnerProperty().get() instanceof Slider) {
					event.consume();
		            if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.RIGHT) {
		                pageSlider.setValue(pageSlider.getValue()+1);
		            }else if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.LEFT) {
		                pageSlider.setValue(pageSlider.getValue()-1);
		            }
				}
			}
		});
		
		displayPane.getChildren().clear();
		VBox baseBox = new VBox();
		baseBox.setAlignment(Pos.TOP_LEFT);
		displayPane.getChildren().add(baseBox);
		HBox buttonBar = getButtonBar();
		buttonBar.setAlignment(Pos.TOP_LEFT);
		buttonBar.setSpacing(5);
		buttonBar.setPadding(new Insets(3));
		buttonBar.setMinWidth(displayWidth-10);
		buttonBar.setMaxWidth(displayWidth-10);
		baseBox.getChildren().add(buttonBar);
		
		baseBox.getChildren().add(webView);
		baseBox.setMinWidth(displayWidth-20);
		baseBox.setMaxWidth(displayWidth-20);
		baseBox.setMinHeight(displayHeight-60);
		baseBox.setMaxHeight(displayHeight-60);

		if (stage != null) {
			webView.prefWidthProperty().bind(stage.widthProperty().subtract(20));
			webView.maxWidthProperty().bind(stage.widthProperty().subtract(20));
			webView.prefHeightProperty().bind(stage.heightProperty().subtract(20));
		}else {
			webView.prefWidthProperty().bind(displayPane.widthProperty());
			webView.maxWidthProperty().bind(displayPane.widthProperty());
			webView.prefHeightProperty().bind(displayPane.heightProperty());
			webView.maxHeightProperty().bind(displayPane.heightProperty());
		}
		
		// Allow user to scroll pages using arrow keys when page slider is focused
		webView.setOnKeyPressed((KeyEvent event) -> {
			if (scene != null && scene.focusOwnerProperty().get() instanceof Slider) {
	            if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.RIGHT) {
	                pageSlider.setValue(pageSlider.getValue()+1);
	            }else if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.LEFT) {
	                pageSlider.setValue(pageSlider.getValue()-1);
	            }
	            event.consume();
			}else {
				handleWebviewKeyEvent(event);
			}
		});
		// Allow the use of the mouse wheel to scroll the pages when when page slider is focused
		webView.setOnScroll((ScrollEvent event) -> {
			if (scene != null && scene.focusOwnerProperty().get() instanceof Slider) {
				if (event.getDeltaY() < 0) {
					if (pageSlider.getValue() < jasperPrint.getPages().size()) pageSlider.setValue(pageSlider.getValue() + 1);
				} else {
					if (pageSlider.getValue() > 0) pageSlider.setValue(pageSlider.getValue() - 1);
				}
			}else {
				handleWebviewScrollEvent(event);
			}
		});
		Platform.runLater(()->displayPane.requestLayout());
		if (pageSlider != null) {
			Platform.runLater(()->pageSlider.requestFocus());
		}
		if (stage != null) stage.show();
	}
	
	private ScrollBar getWebviewVerticalScrollBar() {
		ScrollBar verticalBar = null;
		Set<Node> scrollBars = webView.lookupAll(".scroll-bar");
        for (Node node : scrollBars) {
           	if (((ScrollBar)node).getOrientation() == Orientation.VERTICAL && ((ScrollBar)node).getVisibleAmount() > 0) {
            	verticalBar = (ScrollBar) node;
                break;
            }
        }
        return verticalBar;
	}
	
	private void handleWebviewScrollEvent(ScrollEvent event) {
		ScrollBar verticalBar = getWebviewVerticalScrollBar();
		if (verticalBar != null) {
			if (scrollDeltaY == 0) scrollDeltaY = Double.valueOf(java.lang.Math.abs(verticalBar.getValue() - previousValue));
			double checkValue;
			if (event.getDeltaY() < 0) {
				checkValue = Double.valueOf(previousValue + scrollDeltaY);
			}else {
				checkValue = Double.valueOf(previousValue - scrollDeltaY);
			}
			previousValue = checkValue;
            if (checkValue >= verticalBar.getMax()) {
            	if (pageSlider != null && pageSlider.getValue() < jasperPrint.getPages().size()) {
            		pageSlider.setValue(pageSlider.getValue() + 1);
            		previousValue = Double.valueOf(0);
            	}else {
            		previousValue = Double.valueOf(verticalBar.getMax());
            	}
            }else if (checkValue <= verticalBar.getMin()) {
            	if (pageSlider != null && pageSlider != null && pageSlider.getValue() > 0) {
            		pageSlider.setValue(pageSlider.getValue() - 1);
            	}
        		previousValue = Double.valueOf(0);
            }
		}
	}
	
	private void handleWebviewKeyEvent(KeyEvent event) {
		ScrollBar verticalBar = getWebviewVerticalScrollBar();
		if (verticalBar != null) {
			if (keyDeltaY == 0) {
				keyDeltaY = Double.valueOf(java.lang.Math.abs(verticalBar.getValue() - previousValue));
				event.consume();
				return;
			}
			double checkValue;
			if (event.getCode() == KeyCode.DOWN) {
				checkValue = Double.valueOf(previousValue + keyDeltaY);
			}else if (event.getCode() == KeyCode.UP) {
				checkValue = Double.valueOf(previousValue - keyDeltaY);
			}else {
				return;
			}
			previousValue = checkValue;
			if (keyDeltaY == 0) return;
            if (checkValue >= verticalBar.getMax()) {
            	event.consume();
            	if (pageSlider.getValue() < jasperPrint.getPages().size()) {
            		previousValue = Double.valueOf(0);
            		pageSlider.setValue(pageSlider.getValue() + 1);
            	}else {
            		previousValue = Double.valueOf(verticalBar.getMax());
            	}
             }else if (checkValue <= 0) {
            	event.consume();
           		previousValue = Double.valueOf(0);
           	   	if (pageSlider.getValue() > 0) {
            		pageSlider.setValue(pageSlider.getValue() - 1);
            	}
            }
		}
	}
	
	private HBox getButtonBar() {
		HBox buttonBar = new HBox();
		Button printButton = new Button("Print");
		printButton.setOnAction((ActionEvent e) -> {
			printReport();
		});
		buttonBar.getChildren().add(printButton);
		Button pdfButton = new Button("PDF");
		pdfButton.setOnAction((ActionEvent e) -> {
			exportReportToPDF();
		});
		buttonBar.getChildren().add(pdfButton);
		Button excelButton = new Button("Excel");
		excelButton.setOnAction((ActionEvent e) -> {
			exportReportToExcel();
		});
		buttonBar.getChildren().add(excelButton);
		Button htmlButton = new Button("HTML");
		htmlButton.setOnAction((ActionEvent e) -> {
			exportReportToHTML();
		});
		buttonBar.getChildren().add(htmlButton);
		Region spacer = new Region();
		spacer.setMinWidth(20);
		buttonBar.getChildren().add(spacer);
		Label pageSliderLabel = new Label("Page:");
		buttonBar.getChildren().add(pageSliderLabel);
		Label pageNumber = new Label("0001");
		buttonBar.getChildren().add(pageNumber);
		Label ofLabel = new Label("of");
		buttonBar.getChildren().add(ofLabel);
		Label maxPageNumber = new Label(String.format("%04d", jasperPrint.getPages().size()));
		buttonBar.getChildren().add(maxPageNumber);
		if (jasperPrint.getPages().size() > 1) {
			pageSlider = new Slider();
			buttonBar.getChildren().add(pageSlider);
			pageSlider.setMinWidth(108);
			pageSlider.setMin(1);
			pageSlider.setMax(jasperPrint.getPages().size());
			pageSlider.setValue(1);
			pageSlider.setMajorTickUnit(1);
			pageSlider.setMinorTickCount(1);
			pageSlider.setBlockIncrement(1);
			pageSlider.setSnapToTicks(true);
			pageSlider.valueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
					int newPageNumber = (int) pageSlider.getValue();
					pageNumber.setText(String.format("%04d", newPageNumber));
					generatePageImage(newPageNumber-1);
				}
			});
			// Allow user to scroll pages with up and down keys
			pageSlider.setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode() == KeyCode.DOWN) {
                    pageSlider.setValue(pageSlider.getValue()+1);
                }else if (event.getCode() == KeyCode.UP) {
                    pageSlider.setValue(pageSlider.getValue()-1);
                }
                event.consume();
			});
			// Allow the use of the mouse wheel to scroll the pages when the mouse is hovering over the slider.
			pageSlider.setOnScroll((ScrollEvent event) -> {
				int currentPage = (int) pageSlider.getValue();
				if (event.getDeltaY() < 0) {
					if (currentPage < jasperPrint.getPages().size()) pageSlider.setValue(currentPage + 1);
				} else {
					if (currentPage > 0) pageSlider.setValue(currentPage - 1);
				}
				event.consume();
			});
		}
		return buttonBar;
	}
	
	private void exportToPDF() {
		
	}

	private File getOutputFile(String title, ExtensionFilter extension) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().add(extension);
		return fileChooser.showSaveDialog(displayPane.getScene().getWindow());
	}

	private void printReport() {
		try {
			JasperPrintManager.printReport(jasperPrint, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	private void exportReportToPDF() {
		ExtensionFilter extension = new ExtensionFilter("PDF Files", "*.pdf");
		File file = getOutputFile("Save as PDF", extension);
		if (file != null) {
			displayPane.getScene().setCursor(Cursor.WAIT);
			Service<Boolean> exportTask = new Service<Boolean>() {
				@Override
				protected Task<Boolean> createTask() {
					return new Task<Boolean>() {
						@Override
						protected Boolean call() throws Exception {
							try {
								JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsoluteFile().getAbsolutePath());
							} catch (Exception e) {
								Figures.logStackTrace(e);
								return false;
							}
							return true;
						}
					};
				}
			};
			exportTask.setOnSucceeded(s -> {
				displayPane.getScene().setCursor(Cursor.DEFAULT);
				if (exportTask.getValue().equals(Boolean.FALSE)){
					ServiceFactory.getInstance().getDisplaySvc().displayErrorMessage("Export failed", "See Figures log for details");
				}
				return;
			});
			exportTask.start();
		}
	}

	private void exportReportToExcel() {
		ExtensionFilter extension = new ExtensionFilter("Excel Files", "*.xlsx");
		File file = getOutputFile("Save as Spreadsheet", extension);
		if (file != null) {
			displayPane.getScene().setCursor(Cursor.WAIT);
			Service<Object> exportTask = new Service<Object>() {
				@Override
				protected Task<Object> createTask() {
					return new Task<Object>() {
						@Override
						protected Object call() throws Exception {
							try {
								JRXlsxExporter exporter = new JRXlsxExporter();
								exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
								exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
								SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
								configuration.setOnePagePerSheet(false);
								configuration.setDetectCellType(true);
								configuration.setIgnoreGraphics(false);
								configuration.setIgnoreTextFormatting(false);
								configuration.setWhitePageBackground(false);
								configuration.setRemoveEmptySpaceBetweenColumns(true);
								configuration.setRemoveEmptySpaceBetweenRows(true);
								configuration.setIgnoreCellBorder(true);
								configuration.setIgnoreCellBackground(true);
								configuration.setCollapseRowSpan(true);
								configuration.setIgnoreAnchors(true);
								configuration.setWrapText(false);
								configuration.setIgnorePageMargins(true);
								configuration.setForcePageBreaks(false);
								configuration.setFreezeRow(Integer.valueOf(2));
								JROriginExporterFilter filter = new JROriginExporterFilter();
								JROrigin pageHeaderOrigin = new JROrigin(BandTypeEnum.PAGE_HEADER);
								filter.addOrigin(pageHeaderOrigin);
								JROrigin columnHeaderOrigin = new JROrigin(BandTypeEnum.COLUMN_HEADER);
								filter.addOrigin(columnHeaderOrigin, true);
								configuration.setExporterFilter(filter);
								exporter.setConfiguration(configuration);
								exporter.exportReport();
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e.getLocalizedMessage());
							}
							return null;
						}
					};
				}
			};
			exportTask.start();
			exportTask.setOnSucceeded(s -> {
				displayPane.getScene().setCursor(Cursor.DEFAULT);
				return;
			});
		}
	}

	private void exportReportToHTML() {
		ExtensionFilter extension = new ExtensionFilter("HTML Files", "*.html");
		File file = getOutputFile("Save as HTML", extension);
		if (file != null) {
			displayPane.getScene().setCursor(Cursor.WAIT);
			Service<Object> exportTask = new Service<Object>() {
				@Override
				protected Task<Object> createTask() {
					return new Task<Object>() {
						@Override
						protected Object call() throws Exception {
							String html = generateHtml(null);
							Files.writeString(file.toPath(), html);
							return null;
						}
					};
				}
			};
			exportTask.start();
			exportTask.setOnSucceeded(s -> {
				displayPane.getScene().setCursor(Cursor.DEFAULT);
				return;
			});
		}
	}
}
