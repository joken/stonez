package com.procon.gui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Field {
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

		  private Color state;//JavaFXのColor

		  private ZukuState(int id){
		    setstate(id);
		  }

		  private void setstate(int id) {
			  switch(id){
		      case 0:
		        state = Color.rgb(255,255,255);//白色
		        break;
		      case 1:
		        state = Color.rgb(1,1,223);//青色
		        break;
		      case 2:
		    	state = Color.rgb(0,0,0);//黒色
		        break;
		      default:
		    	  state = Color.RED;
			    }
			}

		public Color getState(){
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
				for (char c : cl) {
					switch (c) {
					case '0':
						zstate[i][i] = ZukuState.NONE;
						break;
					case '1':
						zstate[i][i] = ZukuState.OBSTACLE;
						break;
					default:
						zstate[i][i] = ZukuState.OBSTACLE;
					}
				System.out.println(zstate[i][i].toString());
				}
			}
		in.readLine();
		suiren = in.readLine();//石の個数カウント
		int StoneCount = Integer.parseUnsignedInt
				(String.valueOf(suiren));
		for(int i = 0; i < StoneCount; i++){//石情報取得
			stones.add(new ZukuState[STONE_SIZE][STONE_SIZE]);
			suiren = in.readLine();
			for(int j = 0; j < STONE_SIZE; j++){
				System.out.println(suiren);
				char[] buri = suiren.toCharArray();
				for(int k = 0; k < STONE_SIZE; k++){
					ZukuState[][] z = stones.get(i);
					switch(buri[k]){
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
