import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

class StoneBucket {
	private HashSet<Stone> stones;

	public StoneBucket(int initialCapacity) {
		stones = new HashSet<Stone>(initialCapacity);
	}

	public void add(Stone stone) {
		stones.add(stone);
	}

	public List<Stone> getStones() {
		return stones.stream()
			.filter(stone -> !stone.isDeleted())
			.collect(Collectors.toList());
	}

	public int size() {
		return getStones().size();
	}

	public void clear() {
		stones.clear();
	}

	public void remove(int i_stone, int op) {
		stones.stream()
			.filter(stone -> stone.is(i_stone, op))
			.forEach(stone -> stone.delete());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Stone stone : stones) {
			sb.append(stone.toString());
			sb.append("\r\n");
		}
		return sb.toString();
	}
}