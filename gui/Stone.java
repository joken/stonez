package com.procon.gui;

import com.procon.gui.Field.ZukuState;

/**
 * 回転、裏返しのプロパティをもつ
 * 座標設定も可能
 * */
public class Stone {
	private short rotated;//回転
	private short reversed;//裏返し

	public void rotate(){
		rotated++;
		if(rotated > 3){
			rotated = 0;
		}
	}

	public void reverse(){
		reversed++;
		if(reversed > 1){
			reversed = 0;
		}
	}

	/**
	 * 回転状態を返す
	 * @return 回転角度。度数法
	 * */
	public int getAngle(){
		return rotated * 90;
	}

	/**
	 * @return 裏返しかどうか 0は表 1は裏
	 * */
	public int getreverse(){
		return reversed;
	}

	public void setPosition(ZukuState[][] z,int x, int y){
		int curX = 0,curY = 0;
		for (int i = 0; i < Field.STONE_SIZE; i++) {
			for (int j = 0; j < Field.STONE_SIZE; j++) {
				curX = j * Field.ZUKU_SIZE;
				curY = i * Field.ZUKU_SIZE;
				z[j][i].setXY(x + curX, y + curY);
			}
		}
	}

}
