package com.net.url;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

import com.procon.gui.FieldView;

public class RequestSubmit {

	public void interactive(){
		String query = null
				,command = null;
		BufferedReader bf = new BufferedReader(
				new InputStreamReader(System.in));
		System.out.println("URLを入力(0で終了)");
		try {
			query = bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("input error");
			System.exit(0);
		}

		if(query.equals("0")){
			try{
				bf.close();
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				System.exit(0);
			}
		}

		System.out.println("操作を選択(1 = GET,2 = POST,0 = 終了)");
		try{
			command = bf.readLine();
		}catch(IOException e){
			e.printStackTrace();
			System.err.println("input error");
			try {
				bf.close();
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			this.interactive();
		}

		if(command.equals("0")){
			try{
				bf.close();
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				System.exit(0);
			}
		}else if(command.equals("1") || command.equals("2")){
			switch(Integer.parseInt(command)){
			case 1:
				this.download(query);
				break;
			case 2:
				System.out.println("送信ファイルパスを入力");
				String path = null;
				try {
					path = bf.readLine();
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				this.post(query, path);
				break;
			}
		}else{
			System.out.println("無効な入力,再度入力");
			this.interactive();
		}
	}

	public void download(String url){
		try {
			HttpURLConnection con =
					(HttpURLConnection) new URL(url).openConnection();

			//設定
			//対話形式の無効化
			con.setAllowUserInteraction(false);
			//リダイレクトには従う
			con.setInstanceFollowRedirects(true);
			//GETメソッドを使用
			con.setRequestMethod("GET");
			//キャッシュを使用しない
			con.setUseCaches(false);

			//接続
			con.connect();
			//接続の確認
			if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
				//データ取得 -> File出力
				BufferedReader im =
						new BufferedReader(new InputStreamReader
								(con.getInputStream()));
				BufferedWriter out = new BufferedWriter(
						new FileWriter("quest"+FieldView.QUEST_NUM+".txt"));
				//1回改行?
				//out.newLine();
				String line;
				while((line = im.readLine()) != null){
					out.write(line);
					out.newLine();
				}
				out.flush();
				im.close();
				out.close();
			}else{//エラー
				System.err.println("Error " + con.getResponseCode());
				//さいしょにもどる
				//this.interactive();
				return;
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
			System.err.println("無効なURL");
			return;
			//this.interactive();
		}catch (ProtocolException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void post(String url, String filename){
		try {
			HttpURLConnection con =
					(HttpURLConnection) new URL(url).openConnection();

			//設定
			//対話形式の無効化
			con.setAllowUserInteraction(false);
			//リダイレクトには従う
			con.setInstanceFollowRedirects(true);
			//POSTメソッドを使用
			con.setRequestMethod("POST");
			//キャッシュを使用しない
			con.setUseCaches(false);
			//Output有効化
			con.setDoOutput(true);
			//ContentType
			con.setRequestProperty("Content-Type", "text/plain");

			//接続
			//con.connect();

			try{
				//txtRead -> UPload
				//filename = nullでstdinから吸う("end"で終了)
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(con.getOutputStream()));
				if (filename != null) {
					String line;
					BufferedReader im = new BufferedReader(
							new FileReader(filename));
					while((line = im.readLine()) != null){
						out.write(line);
					}
					im.close();
				}else{
					Scanner scan = new Scanner(System.in);
					while(!scan.hasNext("end")){
						out.write(scan.nextLine());
					}
					scan.close();
				}

				out.flush();

				out.close();
			}catch(IOException e){
				e.printStackTrace();
				return;
			}

			//レスポンスの確認
			if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
				//UTF-8以外からは引数の文字列を変更(クソース)
				InputStreamReader isr = new InputStreamReader(
						con.getInputStream(),
                        Charset.forName("UTF-8"));
				BufferedReader reader = new BufferedReader(isr);
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					}
				reader.close();
			}else{//エラー
				System.out.println("Error " + con.getResponseCode());
				//さいしょにもどる
				this.interactive();
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("無効なURL");
			this.interactive();
		}catch (ProtocolException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

}

class Main{
	/**
	 * URLとコマンドが引数にないと対話モード
	 * @param args[0] URL
	 * @param args[1] コマンド
	 * @param args[2] POST用ファイル名(nullでstdin使用)
	 * */
	public static void main(String args[]){
		RequestSubmit rm = new RequestSubmit();
		if (args.length < 2) {
			rm.interactive();
		}else{
			if(args[1].equals("1")){
				rm.download(args[0]);
			}else if(args[1].equals("2") && args.length == 3){
				rm.post(args[0], args[2]);
			}else if(args[1].equals("2") && args.length == 2){
				rm.post(args[0], null);
			}else{
				System.err.println("Illegal arguments, exit");
				System.exit(0);
			}
		}
	}
}
