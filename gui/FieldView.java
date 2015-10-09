package com.procon.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FieldView extends Application{
	public static Field field;

	public static void main(String[] args){
		FieldView.launch(args);
	}

	@Override
	public void init(){
		try {
			field = new Field();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage) throws Exception {

		FXMLLoader loader = new FXMLLoader
				(getClass().getResource("fieldview.fxml"));
        Parent root = (Parent)loader.load();
        //ControllerにStageを渡しておく(別窓作成時に必要)
        FieldViewctrl ctrl = (FieldViewctrl)loader.getController();
        ctrl.setStage(stage);
        ctrl.setArgs(this.getParameters().getRaw());
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("石畳職人Z");
		stage.getIcons().add(new Image(this.getClass()
				.getResourceAsStream("icon.png")));


		stage.show();

	}

}
