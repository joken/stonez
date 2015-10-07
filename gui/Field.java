package com.procon.gui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Field {
	public static final int ZUKU_SIZE = 15;
	public static final int FIELD_SIZE = 32;
	public static final int STONE_SIZE = 8;
	public static final int FIELD_BASE = 150;

	private ZukuState[][] zstate;
	private Stone stones;

	Field(){
		zstate = new ZukuState[FIELD_SIZE][FIELD_SIZE];
	}

	//石の状態
	public enum ZukuState{
		  NONE(0),//からっぽ
		  STONE(1),//石を置いてる
		  OBSTACLE(2),//最初からある障害物
		  TRANSPARENCY(3);//からっぽでみえない(石情報の石ではない部分)

		  private Color state;//色情報
		  private int X,Y;//座標値
		  private boolean putable;//置けるかどうか

		  private ZukuState(int id){
			putable = false;
		    setstate(id);
		  }

		  private void setstate(int id) {
			  switch(id){
		      case 0:
		        state = new Color(0xFFFFFF);//白色
		        putable = true;
		        break;
		      case 1:
		        state = new Color(0x005AFF);//青色
		        break;
		      case 2:
		    	state = new Color(0x000000);//黒色
		        break;
		      case 3:
		    	  state = new Color(0xFFFFFFF, true);//白色(透明)
		    	  break;
		      default:
		    	  state = new Color(0x9E0099);//むらさき
			    }
			}

		public void setStateToRed(){
			state = new Color(0xFF0000);
		}

		public void setStateToStonePut(){
			state = new Color(0xB2CCFE);//薄い青
			putable = false;
		}

		public Color getState(){
			return state;
		}

		public int getX() {
			return X;
		}

		public void setX(int x) {
			synchronized(this){X = x;}
		}

		public int getY() {
			return Y;
		}

		public void setY(int y) {
			synchronized(this){Y = y;}
		}

		public void setXY(int x, int y){
			setX(x);setY(y);
		}

		public boolean isPutable(){
			return putable;
		}

	}

	public ZukuState[][] getField(){return zstate;}

	public Stone getStones(){return stones;}

	public void setField(ZukuState[][] z){
		synchronized(this){
			zstate = z;
		}
	}

	public String setFile(String name){
		try(BufferedReader in = new BufferedReader(
				new InputStreamReader(
				new FileInputStream(name)))){
			return parse(in);
			}catch (FileNotFoundException e) {
				System.err.println(e.toString());
			}catch(IOException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		return null;
		}

	/**
	 * Procon問題フォーマットのParse
	 * @param in 読み込み先を確保したStream(BufferedReaderで)
	 * @return 問題データ(空白行カット)
	 * */
	private String parse(BufferedReader in) throws IOException{
		String suiren;//一時
		StringBuffer data = new StringBuffer();//@return
		synchronized (this) {
			for (int i = 0; i < FIELD_SIZE; i++) {//Field情報取得
				String a = in.readLine();
				data.append(a + "\n");
				char[] cl = a.toCharArray();
				for (int j = 0; j < FIELD_SIZE; j++) {
					switch (cl[j]) {
					case '0':
						zstate[j][i] = ZukuState.NONE;
						break;
					case '1':
						zstate[j][i] = ZukuState.OBSTACLE;
						break;
					default:
						zstate[j][i] = ZukuState.OBSTACLE;
					}
				}
			}
		}
		in.readLine();
		suiren = in.readLine();//石の個数カウント
		int StoneCount = Integer.parseUnsignedInt
				(String.valueOf(suiren));
		suiren = null;
		ArrayList<ZukuState[][]> stone = new ArrayList<ZukuState[][]>();
		synchronized (this) {
			for (int i = 0; i < StoneCount; i++) {//石情報取得
				stone.add(new ZukuState[STONE_SIZE][STONE_SIZE]);
				ZukuState[][] z = stone.get(i);
				for (int j = 0; j < STONE_SIZE; j++) {
					do {
						suiren = in.readLine();
					} while (suiren == null || suiren.isEmpty());
					data.append(suiren + "\n");
					for (int k = 0; k < STONE_SIZE; k++) {
						switch (suiren.charAt(k)) {
						case '1':
							z[k][j] = ZukuState.STONE;
							break;
						default:
							z[k][j] = ZukuState.TRANSPARENCY;
						}
					}
				}
			}
			stones = new Stone(stone);
		}
		return data.toString();
	}

	/**
	 * 複数行の回答フォーマットをParseする
	 * @param paragraph 回答フォーマット(複数行)
	 * */
	public void setAns(String paragraph){
		String[] lines = paragraph.split("\n");
		for(int i = 0; i < lines.length; i++){
			parse(lines[i]);
		}
	}

	//TODO パーサ実装
	/**
	 * 単行の回答フォーマットをParseする
	 * @param line 回答フォーマット(単行)
	 * */
	private void parse(String line){}
}
