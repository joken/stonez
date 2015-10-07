class Stone {
	public static final int SIZE_FIELD = 32;
	public static final int SIZE_STONE = 8;
	/** 敷地の座標空間の補正値 */
	public static final int OFFSET_FIELD = SIZE_STONE - 1;

	private int i_stone;
	private int op;
	private int i_field;
	private int value;
	private int[] lines;
	private StatusCandidate.Status status;

	public Stone(int i_stone, int op, int i_field, int value, int[][][] lines_stone) {
		this.i_stone = i_stone;
		this.op = op;
		this.i_field = i_field;
		this.value = value;
		this.lines = lines_stone[i_stone][op];
	}

	public void setStatusObject(StatusCandidate.Status status) {
		this.status = status;
	}

	public int[] getStone() {
		return lines;
	}

	public int getIStone() {
		return i_stone;
	}

	public int getIField() {
		return i_field;
	}

	public int getOp() {
		return op;
	}

	public int getValue() {
		return value;
	}

	public void reset() {
		checkStatusObject();
		status.set(State.READY);
	}

	public boolean isPlaced() {
		checkStatusObject();
		return status.is(State.PLACED_THIS) || status.is(State.EDGE);
	}

	public boolean isEdge(Neighbors neighbors) {
		checkStatusObject();
		// 更新
		updateStatus(neighbors);
		return status.is(State.EDGE);
	}

	public boolean isReady() {
		checkStatusObject();
		return status.is(State.READY);
	}

	public void place(StoneBucket[] candidates_by_i, StoneBucket[][] candidates_by_position, Neighbors neighbors) {
		checkStatusObject();
		if (!status.is(State.READY)) {
			throw new IllegalStateException();
		}
		// おなじ石番号の候補
		for (Stone stone_other : candidates_by_i[i_stone].getStones()) {
			stone_other.status.set(State.PLACED_OTHER);
		}
		// かぶっている候補
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			if (lines[j_stone] == 0) {
				continue;
			}
			int i = i_field + j_stone + OFFSET_FIELD;
			if (i < 0) {
				continue;
			}
			if (i >= SIZE_FIELD + OFFSET_FIELD) {
				break;
			}
			for (int value_stone = 0; value_stone < SIZE_STONE; value_stone++) {
				int v = value + value_stone + OFFSET_FIELD;
				if (v < 0) {
					continue;
				}
				if (v >= SIZE_FIELD + OFFSET_FIELD) {
					break;
				}
				if (((lines[j_stone] >> (OFFSET_FIELD - value_stone)) & 1) == 1) {
					for (Stone stone_overlapped : candidates_by_position[i][v].getStones()) {
						stone_overlapped.status.set(State.OVERLAPPED);
					}
				}
			}
		}
		// この候補
		if (canBeEdge(neighbors)) {
			// エッジ
			status.set(State.EDGE);
		} else {
			// エッジではない
			status.set(State.PLACED_THIS);
		}
	}

	// エッジかどうかを判定して更新
	private void updateStatus(Neighbors neighbors) {
		// もとからエッジじゃなかった
		if (!status.is(State.EDGE)) {
			return;
		}
		if (canBeEdge(neighbors)) {
			// まだエッジ
			status.set(State.EDGE);
			return;
		}
		// もはやエッジではない
		status.set(State.PLACED_THIS);
	}

	private boolean canBeEdge(Neighbors neighbors) {
		// 隣接する石の候補を調べる
		for (Stone neighbor : neighbors.getNeighbors(this)) {
			if (neighbor.isReady()) {
				// エッジっぽい
				return true;
			}
		}
		// エッジっぽくない
		return false;
	}

	private void checkStatusObject() {
		if (status == null) {
			throw new IllegalStateException();
		}
	}

	public boolean isFollowingAfter(Stone stone) {
		return stone.i_stone < i_stone;
	}

	public String dump() {
		char[][] chars = new char[SIZE_STONE][];
		char char_free = '.';
		char char_stone = '#';

		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			int line = lines[j_stone];
			line = (~ line) & 0xff;
			String content = String.format("%8s", Integer.toBinaryString(line)).replace(' ', '0');
			content = content.replace('1', char_free).replace('0', char_stone);
			content += "\r\n";
			chars[j_stone] = content.toCharArray();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("  01234567\r\n");
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			sb.append(String.format("%2X ", j_stone).substring(1));
			sb.append(chars[j_stone]);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return String.format("[Stone] %3d %d (%2d, %2d) %s", i_stone, op, value, i_field, status);
	}
}