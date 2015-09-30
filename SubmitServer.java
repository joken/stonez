import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

/**
 * 解答提出システムのサーバ
 *
 * $ java SubmitServer [-p [URL]]
 *
 * @author Kazuaki
 *
 */
public class SubmitServer {

	public static void main(String[] args) {
		// -p オプションで練習用URL文字列になる
		String spec;
		if (args.length >= 1 && args[0].equals("-p")) {
			spec = args.length == 2 ? args[1] : "http://testform26.procon-online.net/answer?token=0123456789abcdef";
		} else {
			spec = "http://172.16.1.2/answer?token=fe2e7a2191c446a6";
		}
		System.out.println("Server: " + spec);
		try {
			// 提出先のURLを生成
			URL url = new URL(spec);
			// 提出器を作る
			AnswerSubmitter submitter = new AnswerSubmitter(url);
			while (true) {
				// 解答を受け付け
				Answer answer = acceptAnswer();
				// とりあえず表示
				System.out.println(answer);
				// 提出
				answer.submit(submitter);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// 解答を受付
	private static Answer acceptAnswer() {
		int score, num_stones;
		String data = "";
		try (
			// いろいろ準備(1)
			ServerSocket servSock = new ServerSocket(65432);
			Socket sock = servSock.accept();
			Scanner sc = new Scanner(sock.getInputStream());
		) {
			// いろいろ準備(2)
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			// ヘッダを読む
			score = sc.nextInt();
			num_stones = sc.nextInt();
			int num_lines = sc.nextInt();
			// 改行を読み飛ばす
			sc.nextLine();
			// データを読む
			for (int i = 0; i < num_lines; i++) {
				data += sc.nextLine() + "\n";
			}
			// とりあえず表示
			System.out.printf("[Received from %s]\n", sock.getInetAddress());
			// リモートにお返事
			out.println("Data received.");

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		// 解答を返す
		return new Answer(score, num_stones, data);
	}
}
