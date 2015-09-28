import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SubmitServer {

    public static void main(String[] args){
        while (true) {
            try {
                ServerSocket servSock = new ServerSocket(65432);
                Socket sock = servSock.accept();

//                BufferedReader in = new BufferedReader(
//                    new InputStreamReader(sock.getInputStream())
//                );
                Scanner sc = new Scanner(sock.getInputStream());
                PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

                int score = sc.nextInt();
                int num_stones = sc.nextInt();
                int num_lines = sc.nextInt();

                String data = "";

                for (int i = 0; i < num_lines + 1; i++) {
                	data += sc.nextLine() + "\n";
				}

                System.out.printf(
                    "[Received from %s]\n",
                    sock.getInetAddress()
                );
                System.out.printf(
                    "Header: %d %d %d\n",
                    score,
                    num_stones,
                    num_lines
                );
                System.out.printf(
                    "Data: %s\n",
                    data
                );
                out.println("Data received.");

                sc.close();
                sock.close();
                servSock.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}

