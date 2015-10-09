import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("C:/users/kazuaki/documents/projects/stonez/sugoi/quest17.txt");

		long t0 = System.currentTimeMillis();
		// --------------

		Solver1 solver = new Solver1(new Scanner(file));

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
