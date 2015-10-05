import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class StoneBucket {
	private Set<Stone> stones;

	public StoneBucket(int initialCapacity) {
		stones = new HashSet<Stone>(initialCapacity);
	}

	public StoneBucket(Collection<? extends Stone> c) {
		stones = new HashSet<Stone>(c);
	}

	public void add(Stone stone) {
		stones.add(stone);
	}

	public void addAll(Collection<? extends Stone> c) {
		stones.addAll(c);
	}

	public Set<Stone> getStones() {
		return stones;
	}

	public int size() {
		return getStones().size();
	}

	public void clear() {
		stones.clear();
	}

	public void reset() {
		stones.forEach(stone -> stone.reset());
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