import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// 自力で解く
//		main_solve();
		// 読む
//		mail_lp_read();
		// ソルバに任せる
		main_lp();
	}

	private static File file = new File("C:/users/kazuaki/documents/projects/stonez/sugoi/quest17.txt");

	private static void mail_lp_read() throws FileNotFoundException {

		File file_result = new File("C:/users/kazuaki/desktop/procon.sol");
		Solver1 solver = new Solver1(new Scanner(file));
		solver.importLP(new Scanner(file_result));
	}

	private static void main_lp() throws IOException {
		File file_out = new File("C:/users/kazuaki/desktop/procon.lp");

		long t0 = System.currentTimeMillis();
		// --------------

		Solver1 solver = new Solver1(new Scanner(file));
		String script_lp = solver.exportLP();

		// --------------
		long t1 = System.currentTimeMillis();

		try (FileWriter out = new FileWriter(file_out)) {
			out.write(script_lp);
		}

		System.out.println("TIME : " + (t1 - t0));
	}

	private static void main_solve() throws FileNotFoundException {

		long t0 = System.currentTimeMillis();
		// --------------

		Solver1 solver = new Solver1(new Scanner(file));
		solver.solve();

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
