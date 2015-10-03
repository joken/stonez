import java.util.HashSet;

class StoneBucket {
	private HashSet<PlacedStone> stones;

	public StoneBucket(int initialCapacity) {
		stones = new HashSet<PlacedStone>(initialCapacity);
	}

	public void add(PlacedStone stone) {
		stones.add(stone);
	}

	public Iterable<PlacedStone> getStones() {
		return stones;
	}

	public int size() {
		return stones.size();
	}

	public void clear() {
		stones.clear();
	}

	public void remove(int i_stone, int op) {
		stones.removeIf(stone -> stone.is(i_stone, op));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (PlacedStone stone : stones) {
			sb.append(stone.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}
}