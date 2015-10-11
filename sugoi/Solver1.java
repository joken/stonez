import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
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

	/** ビーム幅 */
	private static final int WIDTH_BEAM = 3;

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

	/** 2つの石候補のずく数の比較器 */
	private Comparator<? super Stone> comparator = (a, b) -> Integer.compare(num_zk_stone[a.getIStone()], num_zk_stone[b.getIStone()]);

	/** 回答先アドレス */
	private String addr_submit;

	public Solver1(Scanner in, String addr_submit) {
		this.addr_submit = addr_submit;
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
		map_candidate = new HashMap<Stone, Integer>(candidates.size());
		list_candidate = new ArrayList<Stone>(candidates.getStones());
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

	public void solveBeam(int begin) {
		// 解く
		printWithTime("solving...", System.out);
		// 一つ目を選んで置く
		for (int i_stone = begin; i_stone < num_stones; i_stone++) {
			for (Stone stone_first : candidates_by_i[i_stone].getStones()) {
				// 配置をリセット (全部の石の候補の状態を裏からリセット)
				status_candidate.reset();
//				neighbors.clear();
				// えんきゅー
				Deque<Stone> queue = new ArrayDeque<Stone>();
				queue.offer(stone_first);
				while (!queue.isEmpty()) {
					Stone stone = queue.poll();
					// おけなくなっていた
					if (!stone.isReady()) {
						continue;
					}
					// おく
					stone.place(candidates_by_i, candidates_by_position, neighbors);
					// dump
					System.out.println(dumpField());
					System.out.println("SCORE: " + getScore());
					// えんきゅー
					Set<Stone> stones_neighbor = neighbors.getNeighborsFollowing(stone, false);
					for (int i = 0; i < WIDTH_BEAM && stones_neighbor.size() > 0; i++) {
						Stone stone_sugoi = stones_neighbor.stream().max(comparator).get();
						stones_neighbor.remove(stone_sugoi);
						queue.offer(stone_sugoi);
					}
				}
				// 提出
				submit();
			}
		}
		printWithTime("solved.", System.out);
	}

	public void solve(int begin) {
		// 解く
		printWithTime("solving...", System.out);
		// 一つ目を選んで置く
		for (int i_stone = begin; i_stone < num_stones; i_stone++) {
			for (Stone stone : candidates_by_i[i_stone].getStones()) {
				// 配置をリセット (全部の石の候補の状態を裏からリセット)
				status_candidate.reset();
//				neighbors.clear();
				// 解く
				solve(stone);
				// dump
//				System.out.println(dumpField());
				System.out.println("SCORE: " + getScore());
				// 提出
				submit();
//				System.out.println(export());
//				new Scanner(System.in).nextLine();
			}
		}
		printWithTime("solved.", System.out);
	}

	private void solve(Stone stone_placed) {
		// 置く
		stone_placed.place(candidates_by_i, candidates_by_position, neighbors);
		// 解答をまとめる
//		int score = getScore();
		// 隣接する石を探索
		for (Stone stone : status_candidate.getStonesEdge()) {
			for (Stone stone_neighbor : neighbors.getNeighborsFollowing(stone, true)) {
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
						chars[i][v] = (char) (stone.getIStone() % 10 + '0');
//						chars[i][v] = (chars[i][v] == CHAR_OBSTACLE | chars[i][v] == CHAR_INVALID) ? CHAR_INVALID : CHAR_STONE;
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

	public void exportLP(FileWriter out) throws IOException {
		String[] y_str = new String[num_stones];
		String[] x_str = new String[candidates.size()];
		int i = 0;
		StringBuilder sb = new StringBuilder(1024 * 1024 * 1024);
		printWithTime("exporting as LP...", System.out);
		printWithTime("minimize", System.out);
		sb.append("minimize\r\n");
		sb.append("z + s\r\n");
		printWithTime("subject to", System.out);
		sb.append("subject to\r\n");
		// 目的関数の制約
		// スコアを小さく
		printWithTime("subject to: score", System.out);
		sb.append("c");
		sb.append(i++);
		sb.append(": ");
		for (int i_candidate = 0; i_candidate < candidates.size(); i_candidate++) {
			map_candidate.put(list_candidate.get(i_candidate), i_candidate);
			x_str[i_candidate] = new StringBuilder("x").append(Integer.toString(i_candidate, 36)).toString();
			sb.append(" - ");
			sb.append(num_zk_stone[list_candidate.get(i_candidate).getIStone()]);
			sb.append(" ");
			sb.append(x_str[i_candidate]);
			sb.append(" ");
		}
		sb.append("- z <= - ");
		sb.append(num_zk_field);
		sb.append("\r\n");
		// 使用石数を小さく
		printWithTime("subject to: number of stones", System.out);
		sb.append("c");
		sb.append(i++);
		sb.append(":");
		for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
			sb.append(" + ");
			sb.append(x_str[i_candidate]);
		}
		sb.append(" - s <= 0\r\n");
		// out.flush();
		// ある石番号の候補は1つしか置けない
		printWithTime("subject to: i_stone", System.out);
		for (StoneBucket stones : candidates_by_i) {
			if (stones.size() == 0) {
				continue;
			}
			sb.append("c");
			sb.append(i++);
			sb.append(":");
			for (Stone stone : stones.getStones()) {
				sb.append(" + ");
				sb.append(x_str[map_candidate.get(stone)]);
			}
			sb.append(" <= 1\r\n");
		}
		// out.flush();
		// ある位置に候補は1つしか置けない *
		printWithTime("subject to: position", System.out);
		for (int y = 0; y < SIZE_FIELD; y++) {
			for (int x = 0; x < SIZE_FIELD; x++) {
				if (candidates_by_position[y][x].size() == 0) {
					continue;
				}
				sb.append("c");
				sb.append(i++);
				sb.append(":");
				for (Stone stone : candidates_by_position[y][x].getStones()) {
					sb.append(" + ");
					sb.append(x_str[map_candidate.get(stone)]);
				}
				sb.append(" <= 1\r\n");
			}
		}
		// out.flush();
		// 1番目においた石が1つある
		printWithTime("subject to: first stone", System.out);
		sb.append("c");
		sb.append(i++);
		sb.append(":");
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			y_str[i_stone] = new StringBuilder("y").append(Integer.toString(i_stone, 36)).toString();
			sb.append(" + ");
			sb.append(y_str[i_stone]);
		}
		sb.append(" = 1\r\n");
		// out.flush();
		// 1番目に置いた石の番号より小さい番号の石は置かれていない  30 sec
		printWithTime("subject to: fiest stone confirm 1", System.out);
		StringBuilder substring = new StringBuilder();
		int i_stone_current = 0;
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			sb.append("c");
			sb.append(i++);
			sb.append(": + ");
			sb.append(y_str[i_stone]);
			for (int is = i_stone_current; is < i_stone; is++) {
				for (Stone stone : candidates_by_i[is].getStones()) {
					substring.append(" + ");
					substring.append(x_str[map_candidate.get(stone)]);
				}
			}
			i_stone_current = i_stone;
			sb.append(" ");
			sb.append(substring);
			sb.append(" <= 1\r\n");
			// out.flush();
			if (i_stone % 32 == 0) {
				printWithTime(String.format("subject to: fiest stone confirm 1: %d %%", i_stone * 100 / num_stones), System.out);
				String out_str = sb.toString();
				sb = null;
//				Runtime.getRuntime().gc();
				out.write(out_str);
				out.flush();
				out_str = null;
				Runtime.getRuntime().gc();
				sb = new StringBuilder();
			}
		}
		// 1番目に置いた石がちゃんと置かれている
		printWithTime("subject to: first stone confirm 2", System.out);
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			sb.append("c");
			sb.append(i++);
			sb.append(": ");
			sb.append("+ " + y_str[i_stone]);
			for (Stone stone : candidates_by_i[i_stone].getStones()) {
				sb.append(" - " + x_str[map_candidate.get(stone)]);
			}
			sb.append(" <= 0\r\n");
		}
		// out.flush();
		// 順序と隣接
		printWithTime("subject to: order and joint", System.out);
		int size = list_candidate.size();
		for (int i_candidate = 0; i_candidate < list_candidate.size(); i_candidate++) {
			if (i_candidate % 1024 == 0) {
				printWithTime(String.format("subject to: order and joint: %d %%", i_candidate * 100 / size), System.out);
				String out_str1 = sb.toString();
				sb = null;
//				Runtime.getRuntime().gc();
				out.write(out_str1);
				out.flush();
				out_str1 = null;
				Runtime.getRuntime().gc();
				sb = new StringBuilder();
				// out.flush();
			}
			Stone stone = list_candidate.get(i_candidate);
			Set<Stone> stone_neighbors = neighbors.getNeighborsFollowing(stone, false);
			if (stone_neighbors.size() == 0) {
				stone_neighbors = neighbors.getNeighbors(stone, false);
				if (stone_neighbors.size() == 0) {
					continue;
				}
				sb.append("c");
				sb.append(i++);
				sb.append(": ");
				sb.append(y_str[stone.getIStone()]);
				sb.append(" - ");
				sb.append(x_str[i_candidate]);
				sb.append(" <= 0\r\n");
				continue;
			}
			sb.append("c");
			sb.append(i++);
			sb.append(": + ");
			sb.append(x_str[i_candidate]);
			sb.append(" - ");
			sb.append(y_str[stone.getIStone()]);
			for (Stone neighbor : stone_neighbors) {
				sb.append("- 2 ");
				sb.append(x_str[map_candidate.get(neighbor)]);
			}
			sb.append(" <= -1\r\n");
		}
		System.out.println();
		// 変数
		printWithTime("general", System.out);
		sb.append("general\r\n");
		sb.append("z s\r\n");
		// out.flush();
		printWithTime("binary", System.out);
		sb.append("binary\r\n");
		for (int j = 0; j < candidates.size(); j++) {
			sb.append(x_str[j] + " ");
		}
		for (int i_stone = 0; i_stone < num_stones; i_stone++) {
			sb.append(y_str[i_stone] + " ");
		}
		sb.append("\r\n");
		// out.flush();
		printWithTime("exported as LP.", System.out);
		out.write(sb.toString());
		out.flush();
	}

	private void appendln(String out, Object text, Object... args) throws IOException {
		if (args.length > 0) {
			text = String.format(text.toString(), args);
		}
		out += (text.toString());
		out += ("\r\n");
	}


	private void appendsp(String out, Object text, Object... args) throws IOException {
		if (args.length > 0) {
			text = String.format(text.toString(), args);
		}
		out += (text.toString());
		out += (" ");
	}

	public Map<Integer, Map<Integer, String>> getAnswers() {
		return answers;
	}

	public void importLP(Scanner lp) {
		status_candidate.reset();
		Pattern p = Pattern.compile("x([0-9a-z]+)");
		while (lp.hasNextLine()) {
			String line = lp.nextLine();
			Matcher m = p.matcher(line);
			if (m.find()) {
				list_candidate.get(Integer.parseInt(m.group(1), 36)).place(candidates_by_i, candidates_by_position, neighbors);
			}
		}
		// dump
		System.out.println(dumpField());
		System.out.println("SCORE: " + getScore());
		// 提出
		submit();
	}

	private void submit() {
		String answer = export();
		int cout_lines = answer.split("\r\n").length;
		answer = String.format("%d %d %d\r\n", getScore(), countPlacedStones(), cout_lines) + answer;

		System.out.println("Connecting to " + addr_submit + "...");
		try (
			// 標準入力を準備
			Scanner stdIn = new Scanner(answer);
			// リモートに接続
			Socket sock = new Socket(addr_submit, 65432);
		) {
			System.out.println("Connected.");

			// リモートからの入力を準備
			BufferedReader in = new BufferedReader(
				new InputStreamReader(sock.getInputStream())
			);
			// リモートへの出力を準備
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			// 標準入力から解答を読む
			// <想定フォーマット> := <スコア> <石数> <解答の行数>
			int score = stdIn.nextInt();
			int num_stones = stdIn.nextInt();
			int num_lines = stdIn.nextInt();
			// 改行を読み飛ばす
			stdIn.nextLine();
			// 解答を読み込む
			String data = "";
			for (int i = 0; i < num_lines; i++) {
				data += stdIn.nextLine() + "\n";
			}

			// リモートに送信
			out.printf("%d %d %d\n", score, num_stones, num_lines);
			out.println(data);

			// リモートからの応答を受信
			String result = in.readLine();
			System.out.printf("Reply from %s: %s\n", addr_submit, result);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
}