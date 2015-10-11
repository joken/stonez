package com.net.url;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 提出器． 過去に提出された解答よりも良い解答が与えられれば提出する．
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
	private String data_latest;

	// 提出先のURL
	private URL url;
	// トークン
	private String token_str;

	// タイマ
	private Timer timer = new Timer();

	private class SubmitTask extends TimerTask {

		@Override
		public void run() {
			if (data_latest != null) {
				System.err.println(data_latest.replaceAll("[^\r\n]+", "")
						.replaceAll("\r", "r").replaceAll("\n", "n").length());
				System.err.println(data_latest);
				System.err.println("END");
				submitCLI(data_latest, token_str);
			}
			timer.schedule(new SubmitTask(), 1200);
		}

	};

	public AnswerSubmitter(URL url, String token_strs) {
		this.url = url;
		this.token_str = token_strs;
		timer.schedule(new SubmitTask(), 1200);
	}

	/**
	 * 過去に提出された解答よりも良い解答が与えられれば提出する．
	 *
	 * @param score
	 *            得点
	 * @param num_stones
	 *            石数
	 * @param data
	 *            解答
	 * @param token_str
	 *            TODO
	 * @return 結果の真偽
	 */
	public boolean submit(int score, int num_stones, String data) {
		if (score < score_min && num_stones > num_stones_max) {
			// 過去に提出された解答よりも良かった
			// 提出
			data_latest = data;
			score_min = score;
			num_stones_max = num_stones;
			return true;
		}
		return false;
	}

	// 提出
	private void submit(String data, String token_str) {
		// 改行文字の置換
		String data_crlf = data.replaceAll("\r\n", "\n").replaceAll("\n",
				"\r\n");
		// POSTのときにファイルで渡すための一時ファイル
		File tmpFile = null;
		try {
			// creates temporary file
			tmpFile = File.createTempFile("post", ".txt");

			// prints absolute path
			System.out.println("File path: " + tmpFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 一時ファイルに解答を書く
		try (BufferedWriter output = new BufferedWriter(new FileWriter(tmpFile))) {
			output.write(data_crlf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 送信とか
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httppost = new HttpPost(url.toURI());

			FileBody answer = new FileBody(tmpFile);
			StringBody token = new StringBody(token_str,
					ContentType.MULTIPART_FORM_DATA);
			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("answer", answer).addPart("token", token).build();
			httppost.setEntity(reqEntity);

			System.out
					.println("executing request " + httppost.getRequestLine());
			try (CloseableHttpResponse response = httpclient.execute(httppost)) {
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					System.out.println("Response content length: "
							+ resEntity.getContentLength());
					// UTF-8以外からは引数の文字列を変更(クソース)
					InputStreamReader isr = new InputStreamReader(
							resEntity.getContent(), Charset.forName("UTF-8"));
					BufferedReader in = new BufferedReader(isr);
					String line;
					while ((line = in.readLine()) != null) {
						System.out.println(line);
					}
					in.close();
				}
				EntityUtils.consume(resEntity);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			tmpFile.delete();
		}
	}

	private void submitCLI(String data, String token) {
		// 改行文字の置換
		String data_crlf = data.replaceAll("\r\n", "\n").replaceAll("\n",
				"\r\n");
		// POSTのときにファイルで渡すための一時ファイル
		File tmpFile = null;
		//POST結果のtext
		//File log = null;
		try {
			// creates temporary file
			tmpFile = File.createTempFile("post", ".txt");
			//log = new File("Serverlog.txt");

			// prints absolute path
			System.out.println("File path: " + tmpFile.getAbsolutePath());
		} catch (FileNotFoundException e){
			e.printStackTrace();
			/*try {
				log.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 一時ファイルに解答を書く
		try (BufferedWriter output = new BufferedWriter(new FileWriter(tmpFile))) {
			output.write(data_crlf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// cURLしゅるぅぅう
		String[] arg = new String[]{"cmd", "/c","curl",url.toString(),
				"--form-string","token="+token,
				"-F","answer=@"+tmpFile.getAbsolutePath()};
		ProcessBuilder pb = new ProcessBuilder(arg);
		pb.command().forEach(s -> System.out.println(s));
		//logに吐き出す
		//pb.redirectErrorStream(true);
		//pb.redirectOutput(log);
		//stdoutに吐き出す
		pb.inheritIO();
		try {
			pb.start();
		} catch (IOException /*| InterruptedException*/ e) {
			e.printStackTrace();
		}
		/*try(BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(log)))){
			System.err.println(in.readLine());
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}*/
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