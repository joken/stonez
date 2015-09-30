package com.procon.gui;

import com.procon.gui.Field.ZukuState;

import processing.core.PApplet;

public class FieldEdit extends PApplet {

	/**
	 * TODO 直列化ファイルで進行を保存?
	 * 直列化はFieldに実装すべき?
	 */
	private static final long serialVersionUID = 6769308402563936324L;

	private ZukuState[][] field;

	public void setup(){
		size(600,600);
		super.frame.setTitle("石畳職人ZZZ(˘ω˘)");
		setfield();
	}

	public void draw(){
		background(75,75,75);//グレー
		drawfield();
	}

	public void launch(){
		PApplet.main(this.getClass().getName());
	}

	private void setfield(){
		field = FieldView.field.getField();
		if(field == null){
			super.frame.setVisible(false);
			throw new IllegalStateException("Quwst File is not set");
		}
	}

	private void drawfield(){
		for(int i = 0; i < Field.FIELD_SIZE; i++){
			for(int j = 0; j < Field.FIELD_SIZE; j++){
				field[j][i].setXY(j * Field.ZUKU_SIZE + Field.FIELD_BASE,
						i * Field.ZUKU_SIZE + Field.FIELD_BASE);
				System.out.println(field[j][i].getX() + " , "
						+ field[j][i].getY());
				fill(field[j][i].getState());
				rect(field[j][i].getX(), field[j][i].getY(),
						Field.ZUKU_SIZE, Field.ZUKU_SIZE);
			}
		}
		noLoop();
	}

}
