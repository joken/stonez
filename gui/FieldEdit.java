package com.procon.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
	private LinkedHashMap<Integer,ArrayList<Object>> fieldpos;
	private LinkedHashMap<Integer,ArrayList<Integer>> stonepos;

	public void setup(){
		size(700,700);
		super.frame.setTitle("石畳職人ZZZ( ˘ω˘)");
		setfield();
		textSize(36);
		status = "ok";
		stonepos = new LinkedHashMap<>();
		fieldpos = new LinkedHashMap<>();
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
		int cur = 0;
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

				ArrayList<Object> a =new ArrayList<Object>(3);
				a.add(field[j][i].getX());
				a.add(field[j][i].getY());
				a.add(field[j][i]);
				fieldpos.put(cur, a);
				cur++;

				rect(field[j][i].getX(), field[j][i].getY(),
						Field.ZUKU_SIZE, Field.ZUKU_SIZE);
			}
		}
		noLoop();
	}

	private void drawCurrentStone(){
		loop();
		int cur = 0;
		translate(mouseX,mouseY);
		this.rotate();
		this.reverse();
		for(int i = 0; i < Field.STONE_SIZE; i++){
			for(int j = 0; j < Field.STONE_SIZE; j++){
				if(this.CurrentStoneIndex > stones.length){
					this.CurrentStoneIndex = 0;
					this.status = "reuturned first stone";
				}
				ZukuState z = stones.getStone(CurrentStoneIndex)[j][i];
				z.setXY(j * Field.ZUKU_SIZE,
						i * Field.ZUKU_SIZE);
				if (z.equals(ZukuState.STONE)) {
					ArrayList<Integer> a = new ArrayList<Integer>(2);
					a.add(z.getX()+mouseX);
					a.add(z.getY()+mouseY);
					stonepos.put(cur, a);
					cur++;
				}
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
		ArrayList<ZukuState> suiren = new ArrayList<ZukuState>();
		stonepos.forEach((index,List) ->{
		int sx = List.get(0), sy = List.get(1);
			fieldpos.forEach((index2,List2) -> {
				int fx = (int)List2.get(0),fy = (int)List2.get(1);
				ZukuState z = (ZukuState)List2.get(2);
				System.out.println(sx +","+ sy +","+ fx +","+ fy);
				if(sx >= fx && sx < fx + Field.ZUKU_SIZE
						&& sy >= fy && sy < fy + Field.ZUKU_SIZE
						&& z.isPutable()){
					System.out.println("found");
					suiren.add((z));
				}
			});
		});
		if(suiren.isEmpty()){
			status = "out of field or Overriding";
			return;
		}
		for(int e = 0; e < suiren.size(); e++){
			suiren.get(e).setStateToStonePut();
		}
		this.CurrentStoneIndex++;
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
