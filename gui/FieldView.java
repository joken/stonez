package com.procon.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FieldView extends Application{
	public static Field field;
	public static String TEAM_TOKEN = "fe2e7a2191c446a6";
	public static String QUEST_NUM = "1";

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
        //ctrl.setArgs(this.getParameters().getRaw());
        ctrl.setArgs("http://172.16.1.2/quest"
        +QUEST_NUM+".txt?token=" + TEAM_TOKEN);
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("石畳職人Z");
		stage.getIcons().add(new Image(this.getClass()
				.getResourceAsStream("icon.png")));


		stage.show();

	}

}
