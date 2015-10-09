class Stone {
	private static final int SIZE_FIELD = 32;
	private static final int SIZE_STONE = 8;
	/** 敷地の座標空間の補正値 */
	private static final int OFFSET_FIELD = SIZE_STONE - 1;

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
		return status.getState() == State.PLACED_THIS;
	}

	public boolean isReady() {
		checkStatusObject();
		return status.getState() == State.READY;
	}

	public void place(StoneBucket[] candidates_by_i, StoneBucket[][] candidates_by_position) {
		checkStatusObject();

		// おなじ石番号の候補
		for (Stone stone_other : candidates_by_i[i_stone].getStones()) {
			stone_other.status.set(State.PLACED_OTHER);
		}
		// かぶっている候補
		for (int j_stone = 0; j_stone < SIZE_STONE; j_stone++) {
			if (lines[j_stone] == 0) {
				continue;
			}
			int i = i_field + j_stone;
			if (i < 0) {
				continue;
			}
			if (i >= SIZE_FIELD) {
				break;
			}
			for (int value_stone = 0; value_stone < SIZE_STONE; value_stone++) {
				int v = value + value_stone;
				if (v < 0) {
					continue;
				}
				if (v >= SIZE_FIELD) {
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
		status.set(State.PLACED_THIS);
	}

	private void checkStatusObject() {
		if (status == null) {
			throw new IllegalStateException();
		}
	}

	public boolean isFollowedAfter(Stone stone) {
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
		return String.format("[Stone] %3d %d (%2d, %2d)", i_stone, op, value, i_field);
	}



}