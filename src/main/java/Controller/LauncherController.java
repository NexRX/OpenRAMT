/*
 * Copyright (c) 2011, 2014 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package Controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

/**
 * Sample custom control hosting a text field and a button.
 */
public class LauncherController extends AnchorPane {
	@FXML private TextField textField;
	@FXML private Button btnClickMe;
	@FXML private ButtonBar topBar;
	private Stage stage;
	
    private double xOffset = 0;
    private double yOffset = 0;
    
    private Boolean isMaximised = false;
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    private double prevWidth;
    private double prevHeight;
    private double prevX;
    private double prevY;

	public LauncherController(Stage stage) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Launcher.fxml"));
		this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();            
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		this.stage = stage;
		
		applyEventHandlers();
	}

	private void applyEventHandlers() {
		topBar.setOnMousePressed(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	            xOffset = event.getSceneX();
	            yOffset = event.getSceneY();
	        }
	    });
	    topBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	            stage.setX(event.getScreenX() - xOffset);
	            stage.setY(event.getScreenY() - yOffset);
	            isMaximised = false;
	        }
	    });
	    
	    topBar.getButtons().get(2).setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	        	Platform.exit();
	            System.exit(0);
	        }
	    });
	    
	    topBar.getButtons().get(1).setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	        	ObservableList<Screen> screens = Screen.getScreensForRectangle(new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
	        	
	        	
	        	if (!isMaximised) {
		        	Rectangle2D bounds = screens.get(0).getVisualBounds();
		        	prevX = stage.getX();
		        	prevY = stage.getY();
		        	
		        	stage.setX(bounds.getMinX());
		        	stage.setY(bounds.getMinY());
		        	
		        	prevWidth = stage.getWidth();
		        	prevHeight = stage.getHeight();
		        	
		        	stage.setWidth(bounds.getWidth());
		        	stage.setHeight(bounds.getHeight());
	    			
	    			isMaximised = true;
	    		} else {
	    			stage.setWidth(prevWidth);
	    			stage.setHeight(prevHeight);
	    			
	    			stage.setX(prevX);
	    			stage.setY(prevY);
	    			
	    			isMaximised = false;
	    		}
	        }
	    });
	    
	    topBar.getButtons().get(0).setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	        		stage.setIconified(true);
	        }
	    });
	    
	    // none top bar
	    UndecoratedResizable.addResizeListener(stage, this);
	}
	
	
	public String getText() {
		return textProperty().get();
	}

	public void setText(String value) {
		textProperty().set(value);
	}

	public StringProperty textProperty() {
		return textField.textProperty();                
	}

	@FXML
	protected void doSomething() {
		System.out.println("The button was clicked!");
	}

}
