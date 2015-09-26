package com.procon.gui;

import java.io.File;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import com.procon.gui.Field.ZukuState;

public class FieldViewctrl {

	private Stage FieldViewStage;
	@FXML private Label state;
	@FXML private Pane FieldPane;

	public void setStage(Stage s){
		FieldViewStage = s;
	}

	@FXML
	private void onExitMenu(){
		Platform.exit();
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
								FieldView.field.setFile
								(f.getAbsolutePath());
								SetField();
								state.setText("done");
							} catch (Exception e2) {
								// TODO: handle exception
								e2.printStackTrace();
								state.setText("Error : "
								+ e2.toString());
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

	private void SetField(){
		Rectangle[][] r = new Rectangle[Field.FIELD_SIZE][Field.FIELD_SIZE];
		ZukuState[][] F = FieldView.field.getField();
		for(int i = 0; i < Field.FIELD_SIZE; i++){
			for(int j = 0; j < Field.FIELD_SIZE; j++){
				r[i][j] = F[i][j].getState();
				r[i][j].setX(j * Field.ZUKU_SIZE);
				r[i][j].setY(i * Field.ZUKU_SIZE);
				System.out.println(r[i][j].toString());
				FieldPane.getChildren().add(r[i][j]);
			}
		}
		//型があわない
		//FieldPane.getChildren().addAll(r);
	}

}
