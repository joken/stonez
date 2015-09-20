package com.procon.gui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Field {
	public static final int ZUKU_SIZE = 15;
	public static final int FIELD_SIZE = 32;
	public static final int STONE_SIZE = 8;
	private ZukuState[][] zstate;
	private ArrayList<ZukuState[][]> stones;

	Field(){
		zstate = new ZukuState[FIELD_SIZE][FIELD_SIZE];
		stones = new ArrayList<ZukuState[][]>();
	}

	//石の状態
	public enum ZukuState{
		  NONE(0),//からっぽ
		  STONE(1),//石を置いてる
		  OBSTACLE(2);//最初からある障害物

		  private Rectangle state;//JavaFXのColor

		  private ZukuState(int id){
		    setstate(id);
		  }

		  private void setstate(int id) {
			  switch(id){
		      case 0:
		        state = new Rectangle(ZUKU_SIZE,ZUKU_SIZE,Color.WHITE);//白色
		        break;
		      case 1:
		        state = new Rectangle(ZUKU_SIZE,ZUKU_SIZE,Color.LIGHTBLUE);//青色
		        break;
		      case 2:
		    	state = new Rectangle(ZUKU_SIZE,ZUKU_SIZE,Color.BLACK);//黒色
		        break;
		      default:
		    	  state = new Rectangle(ZUKU_SIZE,ZUKU_SIZE,Color.VIOLET);
			    }
			  state.setStrokeWidth(1.0);
			}

		public Rectangle getState(){
			return state;
		}
	}

	public ZukuState[][] getField(){return zstate;}

	public ArrayList<ZukuState[][]> getStones(){return stones;}

	//TODO GUIからもブチ込めるようにする
	public void setFile(String name){
		try(BufferedReader in = new BufferedReader(
				new InputStreamReader(
				new FileInputStream(name)))){
			parse(in);
			}catch (FileNotFoundException e) {
				System.err.println(e.toString());
			}catch(IOException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	/**
	 * Procon純正フォーマットのParse
	 * @param in 読み込み先を確保したStream(BufferedReaderで)
	 * */
	private void parse(BufferedReader in) throws IOException{
		String suiren;//一時
		for(int i = 0; i < FIELD_SIZE; i++){//Field情報取得
				String a = in.readLine();
				System.out.println(a);
				char[] cl = a.toCharArray();
				for (int j = 0; j < FIELD_SIZE; j++) {
					switch (cl[j]) {
					case '0':
						zstate[i][j] = ZukuState.NONE;
						break;
					case '1':
						zstate[i][j] = ZukuState.OBSTACLE;
						break;
					default:
						zstate[i][j] = ZukuState.OBSTACLE;
					}
				System.out.println(zstate[i][j].toString());
				}
			}
		in.readLine();
		suiren = in.readLine();//石の個数カウント
		int StoneCount = Integer.parseUnsignedInt
				(String.valueOf(suiren));
		for(int i = 0; i < StoneCount; i++){//石情報取得
			stones.add(new ZukuState[STONE_SIZE][STONE_SIZE]);
			for(int j = 0; j < STONE_SIZE; j++){
				do {
					suiren = in.readLine();
				} while (!suiren.isEmpty());
				System.out.println(suiren);
				for(int k = 0; k < STONE_SIZE; k++){
					ZukuState[][] z = stones.get(i);
					switch(suiren.charAt(k)){
					case '1':
						z[j][k] = ZukuState.STONE;
						break;
					default:
						z[j][k] = ZukuState.NONE;
					}
				}
			}
		}
	}
}
