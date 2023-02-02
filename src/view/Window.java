package view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import resources.Images;
import util.Util;


public class Window extends Application {	
	
    
    private Stage stage;
    private Menu menu;
    private boolean manual_resize = false;
    private double prev_height = 0;
    
	public void start(Stage stage) {
		this.stage = stage;
		this.menu = new Menu(this);
		
		Scene scene = new Scene(menu, 900, 600);
		scene.getStylesheets().add("util/style.css");
		
		stage.setMaximized(false);
		stage.setScene(scene);
		stage.centerOnScreen();
		stage.setResizable(true);
		stage.getIcons().add(Images.icon);
		stage.show();
		
		addEvents();
		menu.show();
		stage.setHeight(600);
		stage.setMinHeight(300);
		stage.setMinWidth(400);
		stage.centerOnScreen();
	}	
	
	public void addEvents() {
		stage.setOnCloseRequest(e-> {
			Platform.exit();
		});
		stage.widthProperty().addListener((c, old, ne)->{
		//	Util.print("width changed from "+old+" to "+ne +" manual?:"+manual_resize);
			
			if (!manual_resize)
				resizeHeight(ne.doubleValue());
			//else
			//	Util.print("");
			
		});
		stage.heightProperty().addListener((c, old, ne)->{
		//	Util.print("height changed from "+old.doubleValue()+" to "+ne.doubleValue() +" manual?:"+manual_resize);
			if (!manual_resize) {
				if (prev_height != ne.doubleValue()) //snapped back to prev height before manual resize
					resizeWidth(ne.doubleValue());
			}
			else {
				prev_height = old.doubleValue();
				Util.print("");
			}
		});		
		stage.addEventFilter(KeyEvent.KEY_PRESSED, k->{
			if (k.getCode() == KeyCode.ESCAPE)
				stage.setFullScreen(false);			
		});
		
		stage.setOnCloseRequest(c->{   
			menu.save();
		});
		stage.getScene().focusOwnerProperty().addListener((c, o, n)->{
			//Util.print("focus owner change "+o+"->"+n);
		});
	}
	
	public void resizeHeight(double w) {
	//	Util.print(stage.getHeight()+" resizing height according to width"+ w);
		int h = (int)menu.getPrefHeight(w); 
		
		manual_resize = true;
		menu.my_resize(w, h);
		stage.setWidth(w);
		stage.setHeight(h);
		
		manual_resize = false;	
	}
	
	public void resizeWidth(double h) {
	//	Util.print(stage.getWidth()+" resizing width according to height"+ h);
		int w = (int)menu.getPrefWidth(h); 

		manual_resize = true;
		menu.my_resize(w, h);
		stage.setHeight(h);
		stage.setWidth(w);
	
		manual_resize = false;
	}
	
	public void setTitle(String s) {
		stage.setTitle(s);
	}
	
	public static void main(String[] args) {
		launch();
	}

}
