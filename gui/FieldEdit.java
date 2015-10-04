package com.procon.gui;

import java.util.ArrayList;

import com.procon.gui.Field.ZukuState;

import processing.core.PApplet;
import processing.core.PConstants;

public class FieldEdit extends PApplet {

	/**
	 * TODO 直列化ファイルで進行を保存?
	 * 直列化はFieldに実装すべき?
	 */
	private static final long serialVersionUID = 6769308402563936324L;

	private ZukuState[][] field;
	private Stone stones;
	private int CurrentStoneIndex;
	private String status;

	public void setup(){
		size(600,600);
		super.frame.setTitle("石畳職人ZZZ(˘ω˘)");
		setfield();
		textSize(36);
		status = "ok";
	}

	public void draw(){
		background(75,75,75);//グレー
		fill(255,255,255);
		text(status,0,height-12);
		drawfield();
		drawCurrentStone();
	}

	public void launch(){
		PApplet.main(this.getClass().getName());
	}

	private void setfield(){
		field = FieldView.field.getField();
		if(field[0][0] == null){
			super.frame.setVisible(false);
			throw new IllegalStateException("Quwst File is not set");
		}
		stones = FieldView.field.getStones();
	}

	public void mouseReleased(){
		switch(mouseButton){
		case PConstants.LEFT:
			stones.rotate();
			break;
		case PConstants.RIGHT:
			stones.reverse();
			break;
		}
	}

	public void keyPressed(){
		switch(keyCode){
		case PConstants.ENTER:
			this.stoneput();
			break;
		}
	}

	private void drawfield(){
		for(int i = 0; i < Field.FIELD_SIZE; i++){
			for(int j = 0; j < Field.FIELD_SIZE; j++){
				field[j][i].setXY(j * Field.ZUKU_SIZE + Field.FIELD_BASE,
						i * Field.ZUKU_SIZE + Field.FIELD_BASE);
				//System.out.println(field[j][i].getX() + " , "
						//+ field[j][i].getY());デバッグ
				fill(field[j][i].getState().getRed(),
						field[j][i].getState().getGreen(),
						field[j][i].getState().getBlue(),
						field[j][i].getState().getAlpha());
				rect(field[j][i].getX(), field[j][i].getY(),
						Field.ZUKU_SIZE, Field.ZUKU_SIZE);
			}
		}
		noLoop();
	}

	private void drawCurrentStone(){
		loop();
		translate(mouseX,mouseY);
		this.rotate();
		this.reverse();
		for(int i = 0; i < Field.STONE_SIZE; i++){
			for(int j = 0; j < Field.STONE_SIZE; j++){
				ZukuState z = stones.getStone(CurrentStoneIndex)[j][i];
				z.setXY(j * Field.ZUKU_SIZE,
						i * Field.ZUKU_SIZE);
				//System.out.println(z.getX() + " " + z.getY());
				fill(z.getState().getRed(),
						z.getState().getGreen(),
						z.getState().getBlue(),z.getState().getAlpha());
				rect(z.getX(), z.getY(),
						Field.ZUKU_SIZE, Field.ZUKU_SIZE);
			}
		}
	}

	private void stoneput(){
		//各石座標取得 -> それがNONEに入ってるか探索(クソース)
		ZukuState[][] z = stones.getStone(CurrentStoneIndex);
		int sx = 0,sy = 0;
		ArrayList<ZukuState> suiren = new ArrayList<ZukuState>();
		for(int i = 0; i < Field.STONE_SIZE; i++){
			for(int j = 0; j < Field.STONE_SIZE; j++){
				if (z[j][i].equals(ZukuState.STONE)) {
					sx = z[j][i].getX();
					sy = z[j][i].getY();
				}else{
					continue;
				}
				for(int k = 0; k < Field.FIELD_SIZE; k++){
					for(int l = 0; l < Field.FIELD_SIZE; l++){
						int fx = field[l][k].getX(),
							fy = field[l][k].getY();
						System.out.println(sx +","+ sy +","+ fx +","+ fy);
						if(sx >= fx && sx < fx + Field.ZUKU_SIZE
								&& sy >= fy && sy < fy + Field.ZUKU_SIZE){
							if(field[l][k].equals(ZukuState.OBSTACLE)
								|| field[l][k].equals(ZukuState.STONE)){
								status = "Overriding";
								field[l][k].setStateToRed();
								return;
							}else{
								System.out.println("found");
								suiren.add(field[l][k]);
							}
						}else{
							System.out.println("not found");
							continue;
						}
					}
				}
			}
		}
		if(suiren.isEmpty()){
			status = "out of field";
			return;
		}
		for(int e = 0; e < suiren.size(); e++){
			suiren.get(e).setStateToStonePut();
		}
	}

	private void rotate(){
		if(stones.getAngle() != 0){
			rotate(PApplet.radians(stones.getAngle()));
		}
	}

	private void reverse(){
		if(stones.getreverse() != 0){
			//図形を(図形width) * (X方向-100%)に拡大することで
			//線対称に見せる。 図形拡大メソッドの応用
			scale(-1,1);
		}
	}

}
