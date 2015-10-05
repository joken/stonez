class Stone {
	private int i_stone;
	private int op;
	private int i_field;
	private int value;
	private boolean deleted = false;
	private boolean placed = false;

	public Stone(int i_stone, int op, int i_field, int value) {
		this.i_stone = i_stone;
		this.op = op;
		this.i_field = i_field;
		this.value = value;
	}

	public int[] getStone(int[][][] stones) {
		return stones[i_stone][op];
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

	public boolean is(int i_stone, int op) {
		return this.i_stone == i_stone && this.op == op;
	}

	public boolean is(int i_stone, int op, int i_field, int value) {
		return this.i_stone == i_stone && this.op == op && this.i_field == i_field && this.value == value;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isPlaced() {
		return placed;
	}

	public boolean canPlace() {
		return !(deleted || placed);
	}

	public void delete() {
		deleted = true;
	}

	public void place() {
		placed = true;
	}

	@Override
	public int hashCode() {
		return i_stone << (8 + 3 + 6) | op << (3 + 6) | (i_field + 7) << 6 | (value + 7);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Stone)) {
			return false;
		}
		Stone casted = (Stone) obj;
		return casted.i_stone == i_stone && casted.op == op && casted.i_field == i_field && casted.value == value;
	}

	@Override
	public String toString() {
		return String.format("[PlacedStone] %d %d (%2d, %2d)", i_stone, op, value, i_field);
	}

}