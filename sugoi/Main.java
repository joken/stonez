import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("C:/users/kazuaki/documents/projects/stonez/sugoi/quest1.txt");

		long t0 = System.currentTimeMillis();
		// --------------

		Solver solver = new Solver(new Scanner(file));


		// --------------
		long t1 = System.currentTimeMillis();

//		for (int i = 0; i < result.size(); i++) {
//			System.out.printf("%3d: ", i);
//			for (int value : result.get(i)) {
//				System.out.printf("%3d,", value);
//			}
//			System.out.println();
//		}
		System.out.println(solver.dumpField());
		System.out.println("SCORE: " + solver.getScore());
		System.out.println("COUNT: " + solver.countPlacedStones());
		System.out.println("TIME : " + (t1 - t0));
	}

}
