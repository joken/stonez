import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

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
	private String data_latest = "";

	// 提出先のURL
	private URL url;

	public AnswerSubmitter(URL url) {
		this.url = url;
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
		String data_crlf = data.replaceAll("\n", "\r\n");
		File tmpFile = null;
		try {
			// creates temporary file
			tmpFile = File.createTempFile("tmp", ".txt");
			tmpFile.deleteOnExit();

			// prints absolute path
			System.out.println("File path: " + tmpFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedWriter output = new BufferedWriter(new FileWriter(tmpFile))) {
            output.write(data_crlf);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httppost = new HttpPost(url.toURI());

			FileBody answer = new FileBody(tmpFile);
			StringBody token = new StringBody("0123456789abcdef",
					ContentType.MULTIPART_FORM_DATA);
			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("answer", answer).addPart("token", token).build();

			httppost.setEntity(reqEntity);

			System.out.println("executing request " + httppost.getRequestLine());
			try (CloseableHttpResponse response = httpclient.execute(httppost)) {
				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					System.out.println("Response content length: " + resEntity.getContentLength());
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