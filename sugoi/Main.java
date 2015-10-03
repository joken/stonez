import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("C:/hdd/Documents/Projects/stonez/sugoi/quest9.txt");

		long t0 = System.currentTimeMillis();
		// --------------

		Problem problem = new Problem(new Scanner(file));
		List<HashSet<Integer>> result = problem.getFieldMemo();

		// --------------
		long t1 = System.currentTimeMillis();

//		for (int i = 0; i < result.size(); i++) {
//			System.out.printf("%3d: ", i);
//			for (int value : result.get(i)) {
//				System.out.printf("%3d,", value);
//			}
//			System.out.println();
//		}

		System.out.println("TIME: " + (t1 - t0));
	}

}
