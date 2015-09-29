import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SubmitClient {
	public static void main(String[] args) {
		try {
			Socket sock = new Socket(args[1], 65432);

			BufferedReader in = new BufferedReader(
				new InputStreamReader(sock.getInputStream())
			);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			out.println("12 30 3\n123\n456\n789");
			String result = in.readLine();
			out.println("Reply from Server: " + result);

			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
