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
	private static final int ZUKU_SIZE = 15;

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
								state.setText("done");
								SetField();
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
	}

	private void SetField(){
		ZukuState[][] F = FieldView.field.getField();
		int curX = 0;
		int curY = 0;
		for(int i = 0; i < Field.FIELD_SIZE; i++){
			for(int j = 0; j < Field.FIELD_SIZE; j++){
				Rectangle r = new Rectangle(ZUKU_SIZE,ZUKU_SIZE,
						F[i][j].getState());
				FieldPane.getChildren().add(r);
				r.relocate(curX, curY);
				curX += ZUKU_SIZE;
				curY += ZUKU_SIZE;
			}
		}
	}

}
