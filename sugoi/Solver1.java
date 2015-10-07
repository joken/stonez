import java.io.PrintStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Solver1 {
	/** あるスコアで停止 */
	private static final int STOP_AT = 0;

	private static final int SIZE_FIELD = 32;
	private static final int SIZE_STONE = 8;
	/** 敷地の座標空間の補正値 */
	private static final int OFFSET_FIELD = SIZE_STONE - 1;

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
	private Neighbors neighbors;
	/** 石の候補の状態 */
	private StatusCandidate status_candidate;

	private Map<Stone, Integer> map_candidate;
	private List<Stone> list_candidate;

	/** 解答とスコアおよび石数 */
	private Map<Integer, Map<Integer, String>> answers;

	public Solver1(Scanner in) {
		// 読み込み
		printWithTime("parsing...", System.out);
		parse(in);
		// 石の候補を探す
		printWithTime("finding candidates...", System.out);
		findCandidates();
		printWithTime("ready.", System.out);
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
		num_zk_stone_line = new int[num_stones][StoneOperation.COUNT][SIZE_STONE];
		lines_stone = new int[num_stones][StoneOperation.COUNT][SIZE_STONE];
		candidates = new StoneBucket(num_stones * StoneOperation.COUNT * (SIZE_FIELD + OFFSET_FIELD) * (SIZE_FIELD + OFFSET_FIELD));
		candidates_by_position = new StoneBucket[SIZE_FIELD + OFFSET_FIELD][SIZE_FIELD + OFFSET_FIELD];
		candidates_by_i = new StoneBucket[num_stones];
		neighbors = new Neighbors(num_stones, candidates_by_position);
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
				num_zk_stone_line[i][StoneOperation.NORMAL][j] = num_zk;
				num_zk_stone_line[i][StoneOperation.ROTATE180][OFFSET_FIELD - j] = num_zk;
				num_zk_stone_line[i][StoneOperation.FLIP | StoneOperation.NORMAL][j] = num_zk;
				num_zk_stone_line[i][StoneOperation.FLIP | StoneOperation.ROTATE180][OFFSET_FIELD - j] = num_zk;
				// 格納
				int line_reversed = Integer.reverse(stone_line) >>> 24;
				lines_stone[i][StoneOperation.NORMAL][j] = stone_line;
				lines_stone[i][StoneOperation.ROTATE180][OFFSET_FIELD - j] = line_reversed;
				lines_stone[i][StoneOperation.FLIP | StoneOperation.NORMAL][j] = line_reversed;
				lines_stone[i][StoneOperation.FLIP | StoneOperation.ROTATE180][OFFSET_FIELD - j] = stone_line;
				// 横向き
				for (int k = 0; k < SIZE_STONE; k++) {
					// ずく数を数える
					int zk = (stone_line >>> k) & 1;
					num_zk_stone_line[i][StoneOperation.ROTATE90][OFFSET_FIELD - k] += zk;
					num_zk_stone_line[i][StoneOperation.ROTATE270][k] += zk;
					num_zk_stone_line[i][StoneOperation.FLIP | StoneOperation.ROTATE90][k] += zk;
					num_zk_stone_line[i][StoneOperation.FLIP | StoneOperation.ROTATE270][OFFSET_FIELD - k] += zk;
					// 格納
					int bit = zk << j;
					int bit_reversed = zk << (OFFSET_FIELD - j);
					lines_stone[i][StoneOperation.ROTATE90][OFFSET_FIELD - k] += bit;
					lines_stone[i][StoneOperation.ROTATE270][k] += bit_reversed;
					lines_stone[i][StoneOperation.FLIP | StoneOperation.ROTATE90][k] += bit;
					lines_stone[i][StoneOperation.FLIP | StoneOperation.ROTATE270][OFFSET_FIELD - k] += bit_reversed;
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
			candidates_by_i[i] = new StoneBucket(num_stones * StoneOperation.COUNT);
		}
		for (StoneBucket[] line : candidates_by_position) {
			for (int i = 0; i < SIZE_FIELD + OFFSET_FIELD; i++) {
				line[i] = new StoneBucket(SIZE_FIELD + OFFSET_FIELD);
			}
		}
		// 探す
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			for (int op = 0; op < StoneOperation.COUNT; op++) {
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
		status_candidate = new StatusCandidate(candidates, neighbors, num_stones);
		// 位置をキーに石を探せるようにする
		for (Stone stone : candidates.getStones()) {
			// 準備
			int i_stone = stone.getIStone();
			int op = stone.getOp();
			int i_field = stone.getIField();
			int value = stone.getValue();
			// 石の構成をなぞる
			for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
				int ii = i_field + j_stone + OFFSET_FIELD;
				if (ii < 0) {
					continue;
				}
				if (ii >= SIZE_FIELD + OFFSET_FIELD) {
					break;
				}
				int line = lines_stone[i_stone][op][j_stone];
				if (line == 0) {
					continue;
				}
				for (int v = 0; v < SIZE_STONE; v++) {
					int iv = value + v + OFFSET_FIELD;
					if (iv < 0) {
						continue;
					}
					if (iv >= SIZE_FIELD + OFFSET_FIELD) {
						break;
					}
					if (((line >> (OFFSET_FIELD - v)) & 1) == 1) {
						candidates_by_position[ii][iv].add(stone);
					}
				}
			}
		}
		map_candidate = new HashMap<Stone, Integer>(candidates.size());
		list_candidate = new ArrayList<Stone>(candidates.getStones());
		// (結果を表示)
//		for (int i_field = 0; i_field < SIZE_FIELD; i_field++) {
//			for (int value = 0; value < SIZE_FIELD; value++) {
//				System.out.print(String.format("%6d", candidates_by_position[i_field][value].size()).replace("    0", "    -"));
//			}
//			System.out.println();
//		}
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

	public void solve() {
		// 解く
		printWithTime("solving...", System.out);
		// 一つ目を選んで置く
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			for (Stone stone : candidates_by_i[i_stone].getStones()) {
				// 配置をリセット (全部の石の候補の状態を裏からリセット)
				status_candidate.reset();
				// 解く
				solve(stone);
			}
		}
		printWithTime("solved.", System.out);
	}

	private void solve(Stone stone_placed) {
		// 置く
		stone_placed.place(candidates_by_i, candidates_by_position, neighbors);
		// 解答をまとめる
		int score = getScore();
		System.out.print(dumpField());
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
			System.out.printf("SCORE: %3d, STONES: %3d\r\n", score, num_stones_placed);
			return;
		}

		// 隣接する石を探索
		for (Stone stone : status_candidate.getStonesEdge()) {
			for (Stone stone_neighbor : neighbors.getNeighbors(stone)) {
				// 石の順序と配置状態を確認して置く
				if (stone_neighbor.isFollowingAfter(stone_placed) && stone_neighbor.isReady()) {
//					State[] tmp = status_candidate.save();
					solve(stone_neighbor);
//					status_candidate.load(tmp);
				}
			}
		}
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
		for (StoneBucket stones : neighbors.getNeighbors()) {
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
						StoneOperation.export(stone_placed.getOp())
					)
				);
			}
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public String exportLP() {
		printWithTime("exporting as LP...", System.out);
		LPScriptBuilder builder = new LPScriptBuilder();
		StringBuilder sb;
		printWithTime("minimize", System.out);
		builder.setMinimize("z + s");
		printWithTime("subject to", System.out);
		// 目的関数の制約
		// スコアを小さく
		printWithTime("subject to: score", System.out);
		sb = new StringBuilder();
		for (int i_candidate = 0; i_candidate < candidates.size(); i_candidate++) {
			map_candidate.put(list_candidate.get(i_candidate), i_candidate);
			appendsp(sb, "- %d x(%d)", num_zk_stone[list_candidate.get(i_candidate).getIStone()], i_candidate);
		}
		appendsp(sb, "- z");
		appendsp(sb, "<=");
		appendsp(sb, "- " + num_zk_field);
		builder.addConstraint(sb.toString());
		// 使用石数を小さく
		printWithTime("subject to: number of stones", System.out);
		sb = new StringBuilder();
		for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
			appendsp(sb, "+ x(%d)", i_candidate);
		}
		appendsp(sb, "- s");
		appendsp(sb, "<=");
		appendsp(sb, "0");
		builder.addConstraint(sb.toString());
		// ある石番号の候補は1つしか置けない
		printWithTime("subject to: i_stone", System.out);
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			Set<Stone> stones = candidates_by_i[i_stone].getStones();
			if (stones.size() == 0) {
				continue;
			}
			sb = new StringBuilder();
			for (Stone stone : stones) {
				appendsp(sb, "+ x(%d)", map_candidate.get(stone));
			}
			appendsp(sb, "<=");
			appendsp(sb, "1");
			builder.addConstraint(sb.toString());
		}
		// ある位置に候補は1つしか置けない
		printWithTime("subject to: position", System.out);
		for (int y = - OFFSET_FIELD; y < SIZE_FIELD; y++) {
			for (int x = - OFFSET_FIELD; x < SIZE_FIELD; x++) {
				sb = new StringBuilder();
				if (candidates_by_position[y + OFFSET_FIELD][x + OFFSET_FIELD].size() == 0) {
					continue;
				}
				for (Stone stone : candidates_by_position[y + OFFSET_FIELD][x + OFFSET_FIELD].getStones()) {
					appendsp(sb, "+ x(%d)", map_candidate.get(stone));
				}
//				for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
//					if (candidates_by_position[y + OFFSET_FIELD][x + OFFSET_FIELD].getStones().contains(list_candidate.get(i_candidate))) {
//
//					}
//				}
				appendsp(sb, "<=");
				appendsp(sb, "1");
				builder.addConstraint(sb.toString());
			}
		}
		// 1番目においた石が1つある
		printWithTime("subject to: first stone", System.out);
		sb = new StringBuilder();
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			appendsp(sb, "+ y(%d)", i_stone);
		}
		appendsp(sb, "=");
		appendsp(sb, "1");
		builder.addConstraint(sb.toString());
		// 1番目に置いた石の番号より小さい番号の石は置かれていない
		printWithTime("subject to: fiest stone confirm 1", System.out);
		for (int i_stone = 1; i_stone < num_stones; i_stone++) {
			sb = new StringBuilder();
			appendsp(sb, "+ y(%d)", i_stone);
			for (int is = 0; is < i_stone; is++) {
				for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
					if (list_candidate.get(i_candidate).getIStone() == is) {
						appendsp(sb, "+ x(%d)", i_candidate);
					}
				}
			}
			appendsp(sb, "<=");
			appendsp(sb, "1");
			builder.addConstraint(sb.toString());
		}
		// 1番目に置いた石がちゃんと置かれている
		printWithTime("subject to: first stone confirm 2", System.out);
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			sb = new StringBuilder();
			appendsp(sb, "+ y(%d)", i_stone);
			for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
				if (list_candidate.get(i_candidate).getIStone() == i_stone) {
					appendsp(sb, "- x(%d)", i_candidate);
				}
			}
			appendsp(sb, "<=");
			appendsp(sb, "0");
			builder.addConstraint(sb.toString());
		}
		// 順序と隣接
		printWithTime("subject to: order and joint", System.out);
		for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
			Set<Stone> stone_neighbors = neighbors.getNeighbors(list_candidate.get(i_candidate));
			if (stone_neighbors.size() == 0) {
				continue;
			}
			sb = new StringBuilder();
			for (int i_stone = 0; i_stone < num_stones; i_stone++) {
				if (list_candidate.get(i_candidate).getIStone() == i_stone) {
					appendsp(sb, "+ x(%d)", i_candidate);
					appendsp(sb, "- y(%d)", i_stone);
				}
			}
			int count = 0;
			for (Stone neighbor : stone_neighbors) {
				if (list_candidate.get(i_candidate).isFollowingAfter(neighbor)) {
					count++;
					appendsp(sb, "- 2 x(%d)", map_candidate.get(neighbor));
				}
			}
			if (count == 0) {
				continue;
			}
			appendsp(sb, "<=");
			// 計算しなくてもよかった
			appendsp(sb, -1);
			builder.addConstraint(sb.toString());
		}
		// 変数
		printWithTime("general", System.out);
		builder.addGeneral("z");
		printWithTime("binary", System.out);
		for (int i = 0; i < candidates.size(); i++) {
			builder.addBinary(String.format("x(%d)", i));
		}
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			builder.addBinary(String.format("y(%d)", i_stone));
		}
		printWithTime("exported as LP.", System.out);
		return builder.toString();
	}

	private void appendsp(StringBuilder sb, Object text, Object... args) {
		if (args.length > 0) {
			text = String.format(text.toString(), args);
		}
		sb.append(text);
		sb.append(" ");
	}

	public Map<Integer, Map<Integer, String>> getAnswers() {
		return answers;
	}

	public void importLP(Scanner lp) {
		Pattern p = Pattern.compile("x\\((d+)\\).*\r\n");
		while (lp.hasNextLine()) {
			String line = lp.nextLine();
			Matcher m = p.matcher(line);
			if (m != null) {
				int i = Integer.parseInt(m.toMatchResult().group(0));
				list_candidate.get(i).place(candidates_by_i, candidates_by_position, neighbors);
				System.out.println(line);
				System.out.println(i);
			}
		}
		System.out.println(dumpField());
	}
}