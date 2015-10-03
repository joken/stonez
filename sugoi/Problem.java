import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * 神クラス
 */
public class Problem {

	private static final int SIZE_FIELD = 32;
	private static final int SIZE_STONE = 8;
	/** 敷地の座標空間の補正値  */
	private static final int OFFSET_FIELD = SIZE_STONE - 1;

	/** 全部の石の数 */
	private int num_stones;
	/** 石 i のずく数 */
	private int[] num_zk_stone;
	/** 石 i に操作 j を行った行 k のずく数 */
	private int[][][] num_zk_stone_line;
	/** 敷地のずく数 */
	private int num_zk_field;
	/** 敷地の行 i のずく数 */
	private int[] num_zk_field_line = new int[SIZE_FIELD];

	/** 敷地の行 i の構成 */
	private long[] field = new long[SIZE_FIELD];
	/** 石 i に操作 j を行った行 k の構成 */
	private int[][][] stones;

	/** 石 i に操作 j を行ったものを置くことができる敷地の行  k + OFFSET_FIELD の列の l 番目の位置 - OFFSET_FIELD */
	private int[][][][] candidates_stone;
	/** 石 i に操作 j を行ったものをおいた敷地の行  k + OFFSET_FIELD の列の(位置 - OFFSET_FIELD) null許容 */
	private Integer[][][] stones_put;

	class Operation {
		public static final int NORMAL = 0;
		public static final int ROTATE90 = 1;
		public static final int ROTATE180 = 2;
		public static final int ROTATE270 = 3;
		public static final int FLIP = 4;
		public static final int COUNT = 8;
	}

	public Problem(Scanner in) {
		parse(in);
		seekCandidates();
		solve(0);
	}

	private void parse(Scanner in) {
		// 敷地を読む
		num_zk_field = 0;
		for (int i = 0; i < SIZE_FIELD; i++) {
			// 読んで
			String raw_line = in.nextLine().replaceAll("^0{0,31}", "");
			// パース
			// 0: obstacle, 1: free 負論理注意
			long line_field = (~ Long.parseLong(raw_line, 2)) & 0xffffffffL;
			// ずく数を数える
			num_zk_field += num_zk_field_line[i] = Long.bitCount(line_field);
			// 格納
			field[i] = line_field;
		}
		// 石数を読む
		num_stones = in.nextInt();
		// 必要な配列の生成
		num_zk_stone = new int[num_stones];
		num_zk_stone_line = new int[num_stones][Operation.COUNT][SIZE_STONE];
		stones = new int[num_stones][Operation.COUNT][SIZE_STONE];
		candidates_stone = new int[num_stones][Operation.COUNT][SIZE_FIELD + OFFSET_FIELD][];
		stones_put = new Integer[num_stones][Operation.COUNT][SIZE_FIELD + OFFSET_FIELD];
		// 空行を読む
		in.nextLine();
		// 石を読む
		for (int i = 0; i < num_stones; i++) {
			for (int j = 0; j < SIZE_STONE; j++) {
				// 読んでパース
				String raw_line = in.nextLine().replaceAll("^0{0,7}", "");
				int stone_line = Integer.parseInt(raw_line, 2);
				// 縦向き
				// ずく数を数える
				int num_zk = Integer.bitCount(stone_line);
				num_zk_stone[i] += num_zk;
				num_zk_stone_line[i][Operation.NORMAL][j] = num_zk;
				num_zk_stone_line[i][Operation.ROTATE180][OFFSET_FIELD - j] =  num_zk;
				num_zk_stone_line[i][Operation.FLIP | Operation.NORMAL][j] = num_zk;
				num_zk_stone_line[i][Operation.FLIP | Operation.ROTATE180][OFFSET_FIELD - j] = num_zk;
				// 格納
				int line_reversed = Integer.reverse(stone_line) >>> 24;
				stones[i][Operation.NORMAL][j] = stone_line;
				stones[i][Operation.ROTATE180][OFFSET_FIELD - j] = line_reversed;
				stones[i][Operation.FLIP | Operation.NORMAL][j] = line_reversed;
				stones[i][Operation.FLIP | Operation.ROTATE180][OFFSET_FIELD - j] = stone_line;
				// 横向き
				for (int k = 0; k < SIZE_STONE; k++) {
					// ずく数を数える
					int zk = (stone_line >>> k) & 1;
					num_zk_stone_line[i][Operation.ROTATE90][OFFSET_FIELD - k] += zk;
					num_zk_stone_line[i][Operation.ROTATE270][k] += zk;
					num_zk_stone_line[i][Operation.FLIP | Operation.ROTATE90][k] += zk;
					num_zk_stone_line[i][Operation.FLIP | Operation.ROTATE270][OFFSET_FIELD - k] += zk;
					// 格納
					int bit = zk << j;
					int bit_reversed = zk << (OFFSET_FIELD - j);
					stones[i][Operation.ROTATE90][OFFSET_FIELD - k] += bit;
					stones[i][Operation.ROTATE270][k] += bit_reversed;
					stones[i][Operation.FLIP | Operation.ROTATE90][k] += bit;
					stones[i][Operation.FLIP | Operation.ROTATE270][OFFSET_FIELD - k] += bit_reversed;
				}
			}

//			for (int op = 0; op < Operation.COUNT; op++) {
//				for (int j = 0; j < SIZE_STONE; j++) {
//					System.out
//							.printf("STONE %d: OP %d: %8s %d\n", i, op,
//									Integer.toBinaryString(stones[i][op][j])
//											.replace("0", " "), num_zk_stone_line[i][op][j]);
//				}
//				System.out.println();
//			}

			// 空行を読む
			if (in.hasNext()) {
				in.nextLine();
			}
		}
	}

	private void seekCandidates() {
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
//			System.out.println("\n-----------\nSTONE " + i_stone);
			for (int op = 0; op < Operation.COUNT; op++) {
//				System.out.println("\nOP " + op);
				for (int i_field = - OFFSET_FIELD; i_field < SIZE_FIELD; i_field++) {
					List<Integer> indexes = indexesPuttable(i_field, i_stone, op, 0);
					for (int j_stone = 1; j_stone < SIZE_STONE; j_stone++) {
						indexes.retainAll(indexesPuttable(i_field + j_stone, i_stone, op, j_stone));
					}
					candidates_stone[i_stone][op][i_field + OFFSET_FIELD] = indexes.stream().mapToInt(i -> i).toArray();
//					for (int value : candidates_stone[i_stone][op][i_field + OFFSET_FIELD]) {
//						System.out.printf("(%2d, %2d)", value, i_field);
//					}
//					if (candidates_stone[i_stone][op][i_field + OFFSET_FIELD].length > 0) {
//						System.out.println();
//					}
				}
			}
		}
	}

	/** 敷地の i_field 行に対して石 i_stone に操作 op を行った行 j_stone が配置可能な石左上の列位置を返す*/
    private List<Integer> indexesPuttable(int i_field, int i_stone, int op, int j_stone) {
    	// -7     0                              31     38
    	//        ++++++++++++++++++++++++++++++++ ← 敷地
    	// ^^^^^^^^                              ^^^^^^^^ ← 石
    	List<Integer> indexes = new ArrayList<Integer>(SIZE_FIELD + SIZE_STONE - 1);
    	if (i_field < 0 || i_field >= SIZE_FIELD) {
    		return indexes;
    	}
    	for (int i = 1; i < SIZE_STONE; i++) {
//    		System.out.printf("%39s\n", Long.toBinaryString(field[i_field]).replace("0", " "));
//    		System.out.printf("%39s\n", Long.toBinaryString((long) stones[i_stone][op][j_stone] >>> i).replace("0", " "));
    		if (Long.bitCount(field[i_field] & ((long) stones[i_stone][op][j_stone] >>> i)) == num_zk_stone_line[i_stone][op][j_stone]) {
    			indexes.add(SIZE_FIELD - SIZE_STONE + i);
//    			System.out.println(SIZE_FIELD - SIZE_STONE + i);
    		}
    	}
    	for (int i = 0; i < SIZE_FIELD; i++) {
//    		System.out.printf("%39s\n", Long.toBinaryString(field[i_field]).replace("0", " "));
//    		System.out.printf("%39s\n", Long.toBinaryString((long) stones[i_stone][op][j_stone] << i).replace("0", " "));
    		if (Long.bitCount(field[i_field] & ((long) stones[i_stone][op][j_stone] << i)) == num_zk_stone_line[i_stone][op][j_stone]) {
    			indexes.add(SIZE_FIELD - SIZE_STONE - i);
//    			System.out.println(SIZE_FIELD - SIZE_STONE - i);
    		}
    	}
    	return indexes;
	}

    /** stone_begin 個目よりあとの石を最初において解く */
	private void solve(int stone_begin) {
		for (int i_stone = stone_begin; i_stone < num_stones; i_stone++) {
			for (int op = 0; op < Operation.COUNT; op++) {
				for (int i_field = - OFFSET_FIELD; i_field < SIZE_FIELD; i_field++) {
					int[] values = candidates_stone[i_stone][op][i_field + OFFSET_FIELD];
					for (int i = 0; i < values.length; i++) {
						put(i_stone, op, i_field, values[i]);
					}
				}
			}
		}
	}

	private void put(int i_stone_put, int op_put, int i_field_put, int value_put) {
		// 置く
		stones_put[i_stone_put][op_put][i_field_put] = value_put;
		// 置いた石にかぶっている石を石候補から消す
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			for (int op = 0; op < Operation.COUNT; op++) {
				for (int i = i_field_put - OFFSET_FIELD; i < i_field_put + OFFSET_FIELD; i++) {
					for (int j = value_put - OFFSET_FIELD; j < value_put + OFFSET_FIELD; j++) {
						if (isCollide(i_stone_put, op_put, i_field_put, value_put, i_stone, op, i, j)) {
							removeCandidate(i_stone, op, i, j);
						}
					}
				}
			}
		}
		// 置いた石に隣接している石を石配置候補に加える
	}

	private void removeCandidate(int i_stone_rem, int op_rem, int i_rem, int j_rem) {


	}

	private boolean isCollide(int i_stone_a, int op_a, int i_field_a,
			int value_a, int i_stone_b, int op_b, int i_field_b, int value_b) {

		return false;
	}

	public List<HashSet<Integer>> getFieldMemo() {
		SubsetSumProblemSolver solver = new SubsetSumProblemSolver(
				Arrays.stream(num_zk_stone)
				.boxed()
				.collect(Collectors.toList()),
			num_zk_field
		);
		return solver.solve();
	}
}