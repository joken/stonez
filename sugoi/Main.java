import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {

	// SubmitServer のアドレス
	static  String addr_submit = "192.168.2.4";
	// 開始石
	static int begin = 0;

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// 自力で解く
		main_solve();
		// ビームサーチ
//		main_solve_beam();
		// 読む
//		mail_lp_read();
		// ソルバに任せる
//		main_lp();
	}

	private static File file = new File("C:/users/joken/downloads/quest91.txt");

	private static void mail_lp_read() throws FileNotFoundException {
		File file_result = new File("C:/users/joken/desktop/procon.sol");
		Solver1 solver = new Solver1(new Scanner(file), addr_submit);
		solver.importLP(new Scanner(file_result));
	}

	private static void main_lp() throws IOException {
		File file_out = new File("C:/users/joken/desktop/procon.lp");

		long t0 = System.currentTimeMillis();
		// --------------

		Solver1 solver = new Solver1(new Scanner(file), addr_submit);
		try (FileWriter out = new FileWriter(file_out)) {
			solver.exportLP(out);
		}

		// --------------
		long t1 = System.currentTimeMillis();

		System.out.println("TIME : " + (t1 - t0));
	}

	private static void main_solve() throws FileNotFoundException {

		long t0 = System.currentTimeMillis();
		// --------------

		Solver1 solver = new Solver1(new Scanner(file), addr_submit);
		solver.solve(begin);

		// --------------
		long t1 = System.currentTimeMillis();

		Map<Integer, Map<Integer, String>> answers = solver.getAnswers();
		int score = answers.keySet().stream().min((a, b) -> a.compareTo(b)).get();
		int num_stones_placed = answers.get(score).keySet().stream().max((a, b) -> a.compareTo(b)).get();
		String answer_string = answers.get(score).get(num_stones_placed);
		System.out.println(solver.dumpField());
		System.out.println("SCORE: " + score);
		System.out.println("COUNT: " + num_stones_placed);
		System.out.print("ANSWER:\r\n" + answer_string);
//		System.out.println("SCORE: " + solver.getScore());
//		System.out.println("COUNT: " + solver.countPlacedStones());
		System.out.println("TIME : " + (t1 - t0));
	}

	private static void main_solve_beam() throws FileNotFoundException {

		long t0 = System.currentTimeMillis();
		// --------------

		Solver1 solver = new Solver1(new Scanner(file), addr_submit);
		solver.solveBeam(begin);

		// --------------
		long t1 = System.currentTimeMillis();

		Map<Integer, Map<Integer, String>> answers = solver.getAnswers();
		int score = answers.keySet().stream().min((a, b) -> a.compareTo(b)).get();
		int num_stones_placed = answers.get(score).keySet().stream().max((a, b) -> a.compareTo(b)).get();
		String answer_string = answers.get(score).get(num_stones_placed);
		System.out.println(solver.dumpField());
		System.out.println("SCORE: " + score);
		System.out.println("COUNT: " + num_stones_placed);
		System.out.print("ANSWER:\r\n" + answer_string);
//		System.out.println("SCORE: " + solver.getScore());
//		System.out.println("COUNT: " + solver.countPlacedStones());
		System.out.println("TIME : " + (t1 - t0));
	}
}
