import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 提出器．
 * 過去に提出された解答よりも良い解答が与えられれば提出する．
 *
 *
 * @author Kazuaki
 *
 */
public class AnswerSubmitter {

	// 一番良かった解答の得点
	private int score_min = Integer.MAX_VALUE;
	// 一番良かった解答の石数
	private int num_stones_max = 0;
	// 一番良かった解答(toString 用)
	private String data_latest = "";

	// 提出先のURL
	private URL url;

	public AnswerSubmitter(URL url) {
		this.url = url;
	}

	/**
	 * 過去に提出された解答よりも良い解答が与えられれば提出する．
	 * @param score 得点
	 * @param num_stones 石数
	 * @param data 解答
	 * @return 結果の真偽
	 */
	public boolean submit(int score, int num_stones, String data) {
		if (score < score_min && num_stones > num_stones_max) {
			// 過去に提出された解答よりも良かった
			// 提出
			submit(data);
			data_latest = data;
			return true;
		}
		return false;
	}

	// 提出
	private void submit(String data) {
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// 設定
			// 対話形式の無効化
			con.setAllowUserInteraction(false);
			// リダイレクトには従う
			con.setInstanceFollowRedirects(true);
			// POSTメソッドを使用
			con.setRequestMethod("POST");
			// キャッシュを使用しない
			con.setUseCaches(false);
			// Output有効化
			con.setDoOutput(true);
			// ContentType
			con.setRequestProperty("Content-Type", "text/plain");

			con.connect();

			BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(con.getOutputStream())
			);

			// 送信?
			out.write(data);
			out.flush();

			//レスポンスの確認
			if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
				//UTF-8以外からは引数の文字列を変更(クソース)
				InputStreamReader isr = new InputStreamReader(
						con.getInputStream(),
                        Charset.forName("UTF-8"));
				BufferedReader in = new BufferedReader(isr);
				String line;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
					}
				in.close();
			}else{
				//エラー
				System.out.println("Error " + con.getResponseCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Latest Submitted Answer]");
		sb.append(String.format("score: %d\nstones: %d\ndata:\n%s\n",
				score_min, num_stones_max, data_latest));
		return sb.toString();
	}
}