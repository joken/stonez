package com.procon.gui;

import java.util.ArrayList;

import com.procon.gui.Field.ZukuState;

/**
 * 回転、裏返し,全石情報のプロパティをもつ
 * 座標設定も可能
 * */
public class Stone {
	private ArrayList<ZukuState[][]> stones;//問題から吸った全石情報
	private short rotated;//回転
	private short reversed;//裏返し

	Stone(ArrayList<ZukuState[][]> stone){
		stones = stone;
	}

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

	@Deprecated
	public void setPosition(ZukuState[][] z,int x, int y){
		int curX = 0,curY = 0;
		for (int i = 0; i < Field.STONE_SIZE; i++) {
			for (int j = 0; j < Field.STONE_SIZE; j++) {
				curX = j * Field.ZUKU_SIZE + x;
				curY = i * Field.ZUKU_SIZE + y;
				z[j][i].setXY(curX, curY);
			}
		}
	}

	/**
	 * 回転と裏返し情報をリセット
	 * */
	public void reset(){
		rotated = 0;
		reversed = 0;
	}

	/**
	 * 石情報を返す
	 * @param index 石番号
	 * @return 石情報 格納されてない石番号を指定した場合null
	 * */
	public ZukuState[][] getStone(int index) {
		if(index < stones.size() && index >= 0){
			return stones.get(index);
		}else{
			return null;
		}
	}

}
