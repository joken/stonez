import java.io.PrintStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * 神クラス
 */
public class Solver {

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

	/** 石の候補 */
	private List<Stone> candidates_stone;
	/** 敷地の行 i の列 j に置くことができる石のリスト */
	private StoneBucket[][] stones_puttable;
	/** 石 i をおいたもの null許容 */
	private List<Stone> stones_put;
	/** 次に置くことができる石の候補 */
	private int[][][][] neighbors;

	private class Operation {
		static final int NORMAL = 0;
		static final int ROTATE90 = 1;
		static final int ROTATE180 = 2;
		static final int ROTATE270 = 3;
		static final int FLIP = 4;
		static final int COUNT = 8;
	}

	public Solver(Scanner in) {
		// 読み込み
		printWithTime("parsing...", System.out);
		parse(in);
		// 石の候補を探す
		printWithTime("seeking candidates...", System.out);
		seekCandidates();
		// 解く
		printWithTime("solving...", System.out);
		solve();

		for (int i_field = 0; i_field < SIZE_FIELD; i_field++) {
			for (int value = 0; value < SIZE_FIELD; value++) {
				System.out.print(String.format("%6d", stones_puttable[i_field][value].size()).replace("    0", "    -"));
			}
			System.out.println();
		}

		printWithTime("done.", System.out);
	}

	private void printWithTime(String message, PrintStream out) {
		out.printf("%s: %s\r\n", new Time(System.currentTimeMillis()), message);
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
		candidates_stone = new ArrayList<Stone>(num_stones * Operation.COUNT * SIZE_FIELD + OFFSET_FIELD);
		stones_puttable = new StoneBucket[SIZE_FIELD][SIZE_FIELD];
		stones_put = new ArrayList<Stone>(num_stones);
		neighbors = new int[num_stones][Operation.COUNT][SIZE_FIELD + OFFSET_FIELD][];
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
//							.printf("STONE %d: OP %d: %8s %d\r\n", i, op,
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
		// 準備
		for (StoneBucket[] line : stones_puttable) {
			for (int i = 0; i < SIZE_FIELD; i++) {
				line[i] = new StoneBucket(SIZE_FIELD);
			}
		}
		// 探す
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
//			System.out.println("\r\n-----------\r\nSTONE " + i_stone);
			for (int op = 0; op < Operation.COUNT; op++) {
//				System.out.println("\r\nOP " + op);
				for (int i_field = - OFFSET_FIELD; i_field < SIZE_FIELD; i_field++) {
					// おける位置を探す
					List<Integer> indexes = null;
					for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
						if (stones[i_stone][op][j_stone] == 0) {
							continue;
						}
						if (indexes == null) {
							indexes = indexesPuttable(i_field + j_stone, i_stone, op, j_stone);
						} else {
							indexes.retainAll(indexesPuttable(i_field + j_stone, i_stone, op, j_stone));
						}
					}
					// 格納
					final int i_stone_final = i_stone;
					final int op_final = op;
					final int i_field_final = i_field;
					candidates_stone.addAll(
						indexes.stream()
							.map(value -> new Stone(i_stone_final, op_final, i_field_final, value))
							.collect(Collectors.toList())
					);

//					if (candidates_stone[i_stone][op][i_field + OFFSET_FIELD].length > 0) {
//						System.out.printf("\nSTONE %d, OP %d\r\n", i_stone, op);
//					}
//					for (int value : candidates_stone[i_stone][op][i_field + OFFSET_FIELD]) {
//						System.out.printf("(%2d, %2d)", value, i_field);
//					}
//					if (candidates_stone[i_stone][op][i_field + OFFSET_FIELD].length > 0) {
//						System.out.println();
//					}
				}
			}
		}
		// 位置をキーに石を探せるようにする
		for (Stone stone : candidates_stone) {
			int i_stone = stone.getIStone();
			int op = stone.getOp();
			int i_field = stone.getIField();
			int value = stone.getValue();
			// 石の構成をなぞる
			for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
				int ii = i_field + j_stone;
				int line = stones[i_stone][op][j_stone];
				if (line == 0) {
					continue;
				}
				for (int v = 0; v < SIZE_STONE; v++) {
					if (((line >> (OFFSET_FIELD - v)) & 1) == 1) {
						int iv = value + v;
						if (ii >= 0 && ii < SIZE_FIELD && iv >= 0 && iv < SIZE_FIELD) {
							stones_puttable[ii][iv].add(stone);
						}
					}
				}
			}
		}
		//		System.out.println(stones_puttable[6][6]);
		for (int i_field = 0; i_field < SIZE_FIELD; i_field++) {
			for (int value = 0; value < SIZE_FIELD; value++) {
				System.out.print(String.format("%6d", stones_puttable[i_field][value].size()).replace("    0", "    -"));
			}
			System.out.println();
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
//    		System.out.printf("%39s\r\n", Long.toBinaryString(field[i_field]).replace("0", " "));
//    		System.out.printf("%39s\r\n", Long.toBinaryString((long) stones[i_stone][op][j_stone] >>> i).replace("0", " "));
    		if (Long.bitCount(field[i_field] & ((long) stones[i_stone][op][j_stone] >>> i)) == num_zk_stone_line[i_stone][op][j_stone]) {
    			indexes.add(SIZE_FIELD - SIZE_STONE + i);
//    			System.out.println(SIZE_FIELD - SIZE_STONE + i);
    		}
    	}
    	for (int i = 0; i < SIZE_FIELD; i++) {
//    		System.out.printf("%39s\r\n", Long.toBinaryString(field[i_field]).replace("0", " "));
//    		System.out.printf("%39s\r\n", Long.toBinaryString((long) stones[i_stone][op][j_stone] << i).replace("0", " "));
    		if (Long.bitCount(field[i_field] & ((long) stones[i_stone][op][j_stone] << i)) == num_zk_stone_line[i_stone][op][j_stone]) {
    			indexes.add(SIZE_FIELD - SIZE_STONE - i);
//    			System.out.println(SIZE_FIELD - SIZE_STONE - i);
    		}
    	}
    	return indexes;
	}

    /** 解く */
	private void solve() {
		for (Stone stone : candidates_stone) {
			int i_stone = stone.getIStone();
			int op = stone.getOp();
			int i_field = stone.getIField();
			int value = stone.getValue();

			put(i_stone, op, i_field, value);

			addNeighbors(i_stone, op, i_field, value);

			System.out.println(dumpField());

			int count = 0;
			for (int i_stone_count = 0; i_stone_count < num_stones; i_stone_count++) {
				for (int op_count = 0; op_count < Operation.COUNT; op_count++) {
					for (int i_field_count = -OFFSET_FIELD; i_field_count < SIZE_FIELD; i_field_count++) {
						if (neighbors[i_stone_count][op_count][i_field_count + OFFSET_FIELD] == null) {
							continue;
						}
						count += neighbors[i_stone_count][op_count][i_field_count + OFFSET_FIELD].length;
					}
				}
			}
			System.out.println("NEIGHBORS: " + count);
		}
	}

	private void put(int i_stone_put, int op_put, int i_field_put, int value_put) {
		// 置く
		stones_put.add(new Stone(i_stone_put, op_put, i_field_put, value_put));
		candidates_stone.stream()
			.filter(stone -> stone.is(i_stone_put, op_put, i_field_put, value_put))
			.forEach(stone -> stone.place());
		// 置いた石を候補から消す
		for (int op = 0; op < Operation.COUNT; op++) {
			removeCandidate(i_stone_put, op);
		}
		// 置いた石にかぶっている石を石候補から消す
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			int line = stones[i_stone_put][op_put][j_stone];
			if (line == 0) {
				continue;
			}
			int index_i_field_put = i_field_put + j_stone;
			for (int value = 0; value < SIZE_STONE; value++) {
				if (((line >> (OFFSET_FIELD - value)) & 1) == 1) {
					int index_value_put = value_put + value;
					// 候補を削除
					for (Stone stone : stones_puttable[index_i_field_put][index_value_put].getStones()) {
						removeCandidate(stone.getIStone(), stone.getOp(), stone.getIField(), stone.getValue());
					}
				}
			}
		}

	}

	private void addNeighbors(int i_stone_me, int op_me, int i_field_me, int value_me) {
		// 膨張処理をして輪郭をとる
		int[] work = new int[SIZE_STONE + 2];
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			work[j_stone + 0] |= stones[i_stone_me][op_me][j_stone] << 1;
			work[j_stone + 1] |= stones[i_stone_me][op_me][j_stone] | stones[i_stone_me][op_me][j_stone] << 2;
			work[j_stone + 2] |= stones[i_stone_me][op_me][j_stone] << 1;
		}
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			work[j_stone + 1] &= ~ (stones[i_stone_me][op_me][j_stone] << 1);
		}
		// 石候補から輪郭にかぶるものを返す
		for (int j_work = 0; j_work < work.length; j_work++) {
			int line = work[j_work];
			if (line == 0) {
				continue;
			}
			int index_i_field_put = i_field_me + j_work + 1;
			if (index_i_field_put >= SIZE_FIELD) {
				continue;
			}
			for (int value_work = 0; value_work < work.length; value_work++) {
				if (((line >> (work.length - 1 - value_work)) & 1) == 1) {
					int index_value_put = value_me + value_work + 1;
					if (index_value_put >= SIZE_FIELD) {
						continue;
					}
					for (Stone stone : stones_puttable[index_i_field_put][index_value_put].getStones()) {
						if (!stone.canPlace()) {
							continue;
						}
						int i_stone = stone.getIStone();
						int op = stone.getOp();
						int index_i_field = stone.getIField() + OFFSET_FIELD;
						int[] values = neighbors[i_stone][op][index_i_field];

						if (values == null) {
							neighbors[i_stone][op][index_i_field] = new int[1];
							neighbors[i_stone][op][index_i_field][0] = stone.getValue();
							continue;
						}

//						for (int v : values) {
//							System.out.printf("(%2d, %2d) (%2d, %2d)\r\n", i_field_me, value_me, v, stone.getIField());
//						}

						List<Integer> list = Arrays.stream(values)
							.boxed()
							.collect(Collectors.toList());
						list.add(stone.getValue());
						neighbors[i_stone][op][index_i_field] = list.stream()
							.mapToInt(i -> i)
							.distinct()
							.toArray();
					}
				}
			}
		}
	}

	private void removeCandidate(int i_stone_rem, int op_rem) {
		candidates_stone.stream()
			.filter(stone -> stone.is(i_stone_rem, op_rem))
			.forEach(stone -> stone.delete());
//		removeCandidate(candidates_stone, i_stone_rem, op_rem);
		removeCandidate(neighbors, i_stone_rem, op_rem);
	}

	private void removeCandidate(int i_stone_rem, int op_rem, int i_field_rem, int value_rem) {
		candidates_stone.stream()
			.filter(stone -> stone.is(i_stone_rem, op_rem, i_field_rem, value_rem))
			.forEach(stone -> stone.delete());
		//		removeCandidates(candidates_stone, i_stone_rem, op_rem, i_field_rem, value_rem);
		removeCandidates(neighbors, i_stone_rem, op_rem, i_field_rem, value_rem);
	}

	private void removeCandidate(int[][][][] candidates, int i_stone_rem, int op_rem) {
		for (int index_i_field = 0; index_i_field < SIZE_FIELD + OFFSET_FIELD; index_i_field++) {
			candidates[i_stone_rem][op_rem][index_i_field] = new int[0];
		}
	}

	private void removeCandidates(int[][][][] candidates, int i_stone_rem, int op_rem, int i_field_rem, int value_rem) {
		int[] values = candidates[i_stone_rem][op_rem][i_field_rem + OFFSET_FIELD];
		if (values == null) {
			return;
		}
		candidates[i_stone_rem][op_rem][i_field_rem + OFFSET_FIELD] = Arrays.stream(values)
				.filter(value -> value != value_rem)
				.toArray();
	}

	public String dumpField() {
		char[][] chars = new char[SIZE_FIELD][];
		char char_obstacle = ' ';
		char char_free = '.';
		char char_stone = '#';
		char char_stone_multi = '2';
		char char_invalid = 'X';

		for (int i_field = 0; i_field < SIZE_FIELD; i_field++) {
			long line = field[i_field];
			line = (~ line) & 0xffffffffL;
			String content = String.format("%32s", Long.toBinaryString(line)).replace(' ', '0');
			content = content.replace('1', char_obstacle).replace('0', char_free);
			content += "\r\n";
			chars[i_field] = content.toCharArray();
		}
		for (Stone stone : candidates_stone) {
			if (!stone.isPlaced()) {
				continue;
			}
//			System.out.println(stone);
			int[] lines = stone.getStone(stones);
			for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
//				System.out.println("LINE: " + lines[j_stone]);
				if (lines[j_stone] == 0) {
					continue;
				}
				for (int value = 0; value < SIZE_STONE; value++) {
					if (((lines[j_stone] >> (OFFSET_FIELD - value)) & 1) == 1) {
						int i = stone.getIField() + j_stone;
						int v = stone.getValue() + value;
						if (chars[i][v] == char_stone) {
							chars[i][v] = char_stone_multi;
							continue;
						}
						chars[i][v] = (chars[i][v] == char_obstacle | chars[i][v] == char_invalid) ? char_invalid : char_stone;
					}
				}
			}
		}
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			for (int op = 0; op < Operation.COUNT; op++) {
				int[] lines = stones[i_stone][op];
				for (int i_field = - OFFSET_FIELD; i_field < SIZE_FIELD; i_field++) {
					if (neighbors[i_stone][op][i_field + OFFSET_FIELD] == null) {
						continue;
					}
					for (int value : neighbors[i_stone][op][i_field + OFFSET_FIELD]) {
						for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
							for (int val = 0; val < SIZE_STONE; val++) {
								if (((lines[j_stone] >> (OFFSET_FIELD - val)) & 1) == 1) {
									int i = i_field + j_stone;
									int v = value + val;
									if (i < 0 || i >= SIZE_FIELD || v < 0 || v >= chars[i].length) {
										continue;
									}
									chars[i][v] = '^';
								}
							}
						}
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("  0123456789ABCDEF0123456789ABCDEF\r\n");
		for (int i_field = 0; i_field < SIZE_FIELD; i_field++) {
			sb.append(String.format("%2X ", i_field).substring(1));
			sb.append(chars[i_field]);
		}
		return sb.toString();
	}

	public int getScore() {
		return Arrays.stream(dumpField().split("[^.]+")).mapToInt(str -> str.length()).sum();
	}

	public long countPlacedStones() {
		return candidates_stone.stream()
			.filter(stone -> stone.isPlaced())
			.count();
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