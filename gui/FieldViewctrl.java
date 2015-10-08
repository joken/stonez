package com.procon.gui;

import java.io.File;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FieldViewctrl {

	private Stage FieldViewStage;
	private boolean FormatSwitch;
	@FXML private Label state,questfield,refText;
	@FXML private Pane FieldPane;
	@FXML private Button FormatSwitcher,AnswerTypeBtn;
	@FXML private TextArea AnswerArea;
	@FXML private MenuItem FieldEditLauncher;

	public void setStage(Stage s){
		FieldViewStage = s;
	}

	public void setArgs(List<String> args){
		args.forEach(s -> AnswerArea.appendText(s + "\n"));
	}

	@FXML
	private void onExitMenu(){
		Platform.exit();
	}

	@FXML
	private void onAbout(){
		Stage stage = new Stage();
		// モーダルウインドウに設定
		stage.initModality(Modality.APPLICATION_MODAL);
		// オーナーを設定
		stage.initOwner(FieldViewStage);
		//タイトル
		stage.setTitle("About");

		//GUI部品
		VBox hb = new VBox();
		hb.setPrefSize(250, 50);
		hb.setAlignment(Pos.TOP_CENTER);
		Label about = new Label("石畳職人Z2015");
		Button bt = new Button("Close");
		bt.setOnAction(e -> stage.close());
		hb.getChildren().add(about);
		hb.getChildren().add(bt);

		//DONE
		stage.setScene(new Scene(hb));
		stage.show();
	}

	@FXML
	private void onSwitchFormat(){
		FormatSwitch = !FormatSwitch;
		AnswerTypeBtn.setVisible(FormatSwitch);
		AnswerArea.setVisible(FormatSwitch);
		questfield.setVisible(!FormatSwitch);
		if(FormatSwitch){
			refText.setText("Answer Data(type and push Done");
			FormatSwitcher.setText("switch to 問題フォーマット");
		}else{
			refText.setText("Quest Data(D&D to view)");
			FormatSwitcher.setText("switch to 回答フォーマット");
		}
	}

	@FXML
	private void onAnswerTyped(){
		if(!AnswerArea.getText().isEmpty()){
			FieldView.field.setAns(String.valueOf(AnswerArea.getText()));
		}else{
			state.setText("Type answer");
		}
	}

	@FXML
	private void PaneonDragOver(DragEvent e){
		Dragboard b = e.getDragboard();
		if(b.hasFiles()){
			e.acceptTransferModes(TransferMode.COPY);
			e.consume();
		}
	}

	@FXML
	private void PaneonDragDropped(DragEvent e){
		Dragboard b = e.getDragboard();
		if(b.hasFiles()){
			b.getFiles().stream().findFirst().ifPresent((File f)
					-> {
						if(f.getAbsolutePath().endsWith(".txt")){
							state.setText("loading...");
							try {
								questfield.setText(FieldView.field.setFile
								(f.getAbsolutePath()));
								state.setText("done");
								this.FieldEditLauncher.setDisable(false);
								this.AnswerTypeBtn.setDisable(false);
							} catch (Exception e2) {
								// TODO: handle exception
								e2.printStackTrace();
								state.setText("Error : "
								+ e2.toString());
								e.consume();
							}
						}else{
							state.setText("load faild : "
									+ "this is not txt file.");
							e.setDropCompleted(false);
						}
					});
		}else{
			state.setText("load faild : "
					+ "this is not file.");
			e.setDropCompleted(false);
		}
		e.setDropCompleted(true);
	}

	@FXML
	private void onEditLaunch(){
		FieldEdit e = new FieldEdit();
		e.launch();
	}

}
