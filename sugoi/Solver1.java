import java.io.PrintStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

class Solver1 {
	/** あるスコアで停止 */
	private static final int STOP_AT = 0;

	private static final int SIZE_FIELD = 32;
	private static final int SIZE_STONE = 8;
	/** 敷地の座標空間の補正値 */
	private static final int OFFSET_FIELD = SIZE_STONE - 1;
	/** 石の周長の最大 */
	private static final int CIRCUMFERENCE_STONE = 34;

	private static final char CHAR_OBSTACLE = ' ';
	private static final char CHAR_FREE = '.';
	private static final char CHAR_STONE = '#';
	private static final char CHAR_STONE_MULTI = '2';
	private static final char CHAR_INVALID = 'X';
	private static final char CHAR_CANDIDATE = '+';

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
	private long[] lines_field = new long[SIZE_FIELD];
	/** 石 i に操作 j を行った行 k の構成 */
	private int[][][] lines_stone;

	/** 石の候補 */
	private StoneBucket candidates;
	/** 敷地の行 i の列 j に置くことができる石の候補 */
	private StoneBucket[][] candidates_by_position;
	/** 石 i である石の候補 */
	private StoneBucket[] candidates_by_i;
	/** 石の候補 key に隣接する石の候補 */
	private Map<Stone, StoneBucket> neighbors;
	/** 石の候補の状態 */
	private StatusCandidate status_candidate;

	/** 解答とスコアおよび石数 */
	private Map<Integer, Map<Integer, String>> answers;

	private static class Operation {
		static final int NORMAL = 0;
		static final int ROTATE90 = 1;
		static final int ROTATE180 = 2;
		static final int ROTATE270 = 3;
		static final int FLIP = 4;
		static final int COUNT = 8;

		static String export(int op) {
			String flip = ((op & 4) > 0) ? "T" : "H";
			int rotate = (((op & 1) > 0) ? 90 : 0) + (((op & 2) > 0) ? 180 : 0);
			return String.format("%s %d", flip, rotate);
		}
	}

	public Solver1(Scanner in) {
		// 読み込み
		printWithTime("parsing...", System.out);
		parse(in);
		// 石の候補を探す
		printWithTime("seeking candidates...", System.out);
		findCandidates();
		// 解く
		printWithTime("solving...", System.out);
		solve();

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
			long line_field = (~Long.parseLong(raw_line, 2)) & 0xffffffffL;
			// ずく数を数える
			num_zk_field += num_zk_field_line[i] = Long.bitCount(line_field);
			// 格納
			lines_field[i] = line_field;
		}
		// 石数を読む
		num_stones = in.nextInt();
		// 必要な配列の生成
		num_zk_stone = new int[num_stones];
		num_zk_stone_line = new int[num_stones][Operation.COUNT][SIZE_STONE];
		lines_stone = new int[num_stones][Operation.COUNT][SIZE_STONE];
		candidates = new StoneBucket(num_stones * Operation.COUNT * (SIZE_FIELD + OFFSET_FIELD) * (SIZE_FIELD + OFFSET_FIELD));
		candidates_by_position = new StoneBucket[SIZE_FIELD][SIZE_FIELD];
		candidates_by_i = new StoneBucket[num_stones];
		neighbors = new HashMap<Stone, StoneBucket>(num_stones, 1.0f);
		answers = new HashMap<Integer, Map<Integer,String>>((SIZE_FIELD + OFFSET_FIELD) * (SIZE_FIELD + OFFSET_FIELD), 1.0f);
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
				num_zk_stone_line[i][Operation.ROTATE180][OFFSET_FIELD - j] = num_zk;
				num_zk_stone_line[i][Operation.FLIP | Operation.NORMAL][j] = num_zk;
				num_zk_stone_line[i][Operation.FLIP | Operation.ROTATE180][OFFSET_FIELD - j] = num_zk;
				// 格納
				int line_reversed = Integer.reverse(stone_line) >>> 24;
				lines_stone[i][Operation.NORMAL][j] = stone_line;
				lines_stone[i][Operation.ROTATE180][OFFSET_FIELD - j] = line_reversed;
				lines_stone[i][Operation.FLIP | Operation.NORMAL][j] = line_reversed;
				lines_stone[i][Operation.FLIP | Operation.ROTATE180][OFFSET_FIELD - j] = stone_line;
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
					lines_stone[i][Operation.ROTATE90][OFFSET_FIELD - k] += bit;
					lines_stone[i][Operation.ROTATE270][k] += bit_reversed;
					lines_stone[i][Operation.FLIP | Operation.ROTATE90][k] += bit;
					lines_stone[i][Operation.FLIP | Operation.ROTATE270][OFFSET_FIELD - k] += bit_reversed;
				}
			}
			// 空行を読む
			if (in.hasNext()) {
				in.nextLine();
			}
		}
	}

	private void findCandidates() {
		// 準備
		for (int i = 0; i < num_stones; i++) {
			candidates_by_i[i] = new StoneBucket(num_stones * Operation.COUNT);
		}
		for (StoneBucket[] line : candidates_by_position) {
			for (int i = 0; i < SIZE_FIELD; i++) {
				line[i] = new StoneBucket(SIZE_FIELD + OFFSET_FIELD);
			}
		}
		// 探す
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			for (int op = 0; op < Operation.COUNT; op++) {
				for (int i_field = - OFFSET_FIELD; i_field < SIZE_FIELD; i_field++) {
					// おける位置を探す
					List<Integer> values = null;
					for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
						if (lines_stone[i_stone][op][j_stone] == 0) {
							continue;
						}
						if (values == null) {
							values = findValuesPlaceable(i_field + j_stone, i_stone, op, j_stone);
						} else {
							values.retainAll(findValuesPlaceable(i_field + j_stone, i_stone, op, j_stone));
						}
					}
					// 格納
					for (int value : values) {
						Stone stone = new Stone(i_stone, op, i_field, value, lines_stone);
						candidates_by_i[i_stone].add(stone);
						candidates.add(stone);
					}
				}
			}
		}
		// 石の候補の状態を初期化
		status_candidate = new StatusCandidate(candidates);
		// 位置をキーに石を探せるようにする
		for (Stone stone : candidates.getStones()) {
			// 準備
			int i_stone = stone.getIStone();
			int op = stone.getOp();
			int i_field = stone.getIField();
			int value = stone.getValue();
			// 石の構成をなぞる
			for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
				int ii = i_field + j_stone;
				if (ii < 0) {
					continue;
				}
				if (ii >= SIZE_FIELD) {
					break;
				}
				int line = lines_stone[i_stone][op][j_stone];
				if (line == 0) {
					continue;
				}
				for (int v = 0; v < SIZE_STONE; v++) {
					int iv = value + v;
					if (iv < 0) {
						continue;
					}
					if (iv >= SIZE_FIELD) {
						break;
					}
					if (((line >> (OFFSET_FIELD - v)) & 1) == 1) {
						candidates_by_position[ii][iv].add(stone);
					}
				}
			}
		}
		// (結果を表示)
		for (int i_field = 0; i_field < SIZE_FIELD; i_field++) {
			for (int value = 0; value < SIZE_FIELD; value++) {
				System.out.print(String.format("%6d", candidates_by_position[i_field][value].size()).replace("    0", "    -"));
			}
			System.out.println();
		}
	}

	/** 敷地の i_field 行に対して石 i_stone に操作 op を行った行 j_stone が配置可能な石左上の列位置を返す */
	private List<Integer> findValuesPlaceable(int i_field, int i_stone, int op, int j_stone) {
		List<Integer> indexes = new ArrayList<Integer>(SIZE_FIELD + SIZE_STONE - 1);
		if (i_field < 0 || i_field >= SIZE_FIELD) {
			return indexes;
		}
		for (int i = 1; i < SIZE_STONE; i++) {
			if (Long.bitCount(lines_field[i_field]
					& ((long) lines_stone[i_stone][op][j_stone] >>> i)) == num_zk_stone_line[i_stone][op][j_stone]) {
				indexes.add(SIZE_FIELD - SIZE_STONE + i);
			}
		}
		for (int i = 0; i < SIZE_FIELD; i++) {
			if (Long.bitCount(lines_field[i_field]
					& ((long) lines_stone[i_stone][op][j_stone] << i)) == num_zk_stone_line[i_stone][op][j_stone]) {
				indexes.add(SIZE_FIELD - SIZE_STONE - i);
			}
		}
		return indexes;
	}

	private void solve() {
		// 一つ目を選んで置く
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			for (Stone stone : candidates_by_i[i_stone].getStones()) {
				// 配置をリセット
				candidates.reset();
				// 解く
				solve(stone);
			}
		}
	}

	private void solve(Stone stone_placed) {
		// 置く
		stone_placed.place(candidates_by_i, candidates_by_position);
		// 解答をまとめる
		int score = getScore();
		System.out.println(dumpField());
		System.out.println("SCORE: " + score);
		if (score <= STOP_AT) {
			long num_stones_placed = countPlacedStones();
			String answer_string = export();
			if (!answers.containsKey(score)) {
				answers.put(score, new HashMap<Integer, String>(num_stones, 1.0f));
			}
			answers.get(score).put((int) num_stones_placed, answer_string);
			if (score < 10) {
				System.out.println(dumpField());
			}
			System.out.printf("SCORE: %3d, STONES: %3d\r\n", score,
					num_stones_placed);
			return;
		}
		// 隣接する石の候補を計算
		Set<Stone> stones_neighbor;
		if (!neighbors.containsKey(stone_placed)) {
			stones_neighbor = findNeighbors(stone_placed);
			neighbors.put(stone_placed, new StoneBucket(stones_neighbor));
		} else {
			stones_neighbor = neighbors.get(stone_placed).getStones();
		}
		// 隣接する石を探索
		for (Stone stone : candidates.getStones()) {
			if (!stone.isPlaced()) {
				continue;
			}
			for (Stone stone_neighbor : neighbors.get(stone).getStones()) {
				if (stone_neighbor.isFollowedAfter(stone_placed) && stone_neighbor.isReady()) {
//					State[] tmp = status_candidate.save();
					solve(stone_neighbor);
//					status_candidate.load(tmp);
				}
			}
		}
	}

	private Set<Stone> findNeighbors(Stone stone_me) {
		// 準備
		int i_field_me = stone_me.getIField();
		int value_me = stone_me.getValue();
		int[] lines = stone_me.getStone();
		// 膨張処理をして輪郭をとる
		int[] work = new int[SIZE_STONE + 2];
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			work[j_stone + 0] |= lines[j_stone] << 1;
			work[j_stone + 1] |= lines[j_stone] | lines[j_stone] << 2;
			work[j_stone + 2] |= lines[j_stone] << 1;
		}
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			work[j_stone + 1] &= ~ (lines[j_stone] << 1);
		}
		// 石候補から輪郭にかぶるものを返す
		Set<Stone> stones = new HashSet<>(num_stones * Operation.COUNT * CIRCUMFERENCE_STONE);
		for (int j_work = 0; j_work < work.length; j_work++) {
			int line = work[j_work];
			if (line == 0) {
				continue;
			}
			int index_i_field_put = i_field_me + j_work - 1;
			if (index_i_field_put < 0) {
				continue;
			}
			if (index_i_field_put >= SIZE_FIELD) {
				break;
			}
			for (int value_work = 0; value_work < work.length; value_work++) {
				if (((line >> (work.length - 1 - value_work)) & 1) == 1) {
					int index_value_put = value_me + value_work - 1;
					if (index_value_put < 0) {
						continue;
					}
					if (index_value_put >= SIZE_FIELD) {
						break;
					}
					for (Stone stone : candidates_by_position[index_i_field_put][index_value_put].getStones()) {
						if (!stone.isReady()) {
							continue;
						}
						stones.add(stone);
					}
				}
			}
		}
		return stones;
	}

	public int getScore() {
		int score = num_zk_field;
		for (Stone stone : candidates.getStones()) {
			if (stone.isPlaced()) {
				score -= num_zk_stone[stone.getIStone()];
			}
		}
		return score;
	}

	public long countPlacedStones() {
		return candidates.getStones().stream()
			.filter(stone -> stone.isPlaced())
			.count();
	}

	public String dumpField() {
		char[][] chars = new char[SIZE_FIELD][];

		for (int index_i_field = 0; index_i_field < SIZE_FIELD; index_i_field++) {
			long line = lines_field[index_i_field];
			line = (~ line) & 0xffffffffL;
			String content = String.format("%32s", Long.toBinaryString(line)).replace(' ', '0');
			content = content.replace('1', CHAR_OBSTACLE).replace('0', CHAR_FREE);
			content += "\r\n";
			chars[index_i_field] = content.toCharArray();
		}
		for (Stone stone : candidates.getStones()) {
			if (!stone.isPlaced()) {
				continue;
			}
			int[] lines = stone.getStone();
			for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
				if (lines[j_stone] == 0) {
					continue;
				}
				for (int value = 0; value < SIZE_STONE; value++) {
					if (((lines[j_stone] >> (OFFSET_FIELD - value)) & 1) == 1) {
						int i = stone.getIField() + j_stone;
						int v = stone.getValue() + value;
						if (chars[i][v] == CHAR_STONE) {
							chars[i][v] = CHAR_STONE_MULTI;
							continue;
						}
						chars[i][v] = (chars[i][v] == CHAR_OBSTACLE | chars[i][v] == CHAR_INVALID) ? CHAR_INVALID : CHAR_STONE;
					}
				}
			}
		}
		for (StoneBucket stones : neighbors.values()) {
			for (Stone stone : stones.getStones()) {
				if (!stone.isReady()) {
					continue;
				}
				int[] lines = stone.getStone();
				for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
					if (lines[j_stone] == 0) {
						continue;
					}
					for (int value = 0; value < SIZE_STONE; value++) {
						if (((lines[j_stone] >> (OFFSET_FIELD - value)) & 1) == 1) {
							int i = stone.getIField() + j_stone;
							int v = stone.getValue() + value;
							if (chars[i][v] == CHAR_STONE) {
								chars[i][v] = CHAR_STONE_MULTI;
								continue;
							}
							chars[i][v] = CHAR_CANDIDATE;
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

	public String export() {
		StringBuilder sb = new StringBuilder();
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			Set<Stone> stones = candidates_by_i[i_stone].getStones();
			long count = stones.stream().filter(stone -> stone.isPlaced()).count();
			if (count > 1) {
				System.err.printf("ERROR:%d: too many placed stones (%d)\r\n", i_stone, count);
			}
			if (count == 1) {
				Stone stone_placed = stones.stream().filter(stone -> stone.isPlaced()).findAny().get();
				sb.append(
					String.format(
						"%d %d %s",
						stone_placed.getValue(),
						stone_placed.getIField(),
						Operation.export(stone_placed.getOp())
					)
				);
			}
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public Map<Integer, Map<Integer, String>> getAnswers() {
		return answers;
	}
}