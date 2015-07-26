
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

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
			//はじめにもどる
			e.printStackTrace();
			System.err.println("input error");
			this.interactive();
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
				this.post(query);
				break;
			}
		}else{
			System.out.println("無効な入力,再度入力");
			this.interactive();
		}
	}

	public void download(String url){
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

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
				//データ取得 -> stdout
				BufferedReader im =
						new BufferedReader(new InputStreamReader
								(con.getInputStream()));
				while(im.readLine() != null){
					System.out.println(im.readLine());
				}
				im.close();
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

	public void post(String url){}

}

class Main{
	public static void main(String args[]){
		RequestSubmit rm = new RequestSubmit();
		rm.interactive();
	}
}
