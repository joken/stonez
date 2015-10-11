
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 解答提出システムのクライアント．
 *
 * $ java SubmitClient host
 *
 * @author Kazuaki
 *
 */
public class SubmitClient {

	public static void main(String[] args) {
		// 引数を確認
		if (args.length < 1) {
			System.err.println("bad argment #1 to 'SubmitClient' (hostname or address expected)");
			System.exit(1);
		}
		String hostaddr = args[0];

		System.out.println("Connecting to " + hostaddr + "...");
		try (
			// 標準入力を準備
			Scanner stdIn = new Scanner(System.in);
			// リモートに接続
			Socket sock = new Socket(hostaddr, 65432);
		) {
			System.out.println("Connected.");

			// リモートからの入力を準備
			BufferedReader in = new BufferedReader(
				new InputStreamReader(sock.getInputStream())
			);
			// リモートへの出力を準備
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			// 標準入力から解答を読む
			// <想定フォーマット> := <スコア> <石数> <解答の行数>
			int score = stdIn.nextInt();
			int num_stones = stdIn.nextInt();
			int num_lines = stdIn.nextInt();
			// 改行を読み飛ばす
			stdIn.nextLine();
			// 解答を読み込む
			String data = "";
			for (int i = 0; i < num_lines; i++) {
				data += stdIn.nextLine() + "\r\n";
			}

			// リモートに送信
			out.printf("%d %d %d\n", score, num_stones, num_lines);
			out.println(data);

			// リモートからの応答を受信
			String result = in.readLine();
			System.out.printf("Reply from %s: %s\n", hostaddr, result);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

}
