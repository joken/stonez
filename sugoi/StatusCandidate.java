import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class StatusCandidate {
	private State[] states;
	private Neighbors neighbors;
	private StoneBucket candidates;
	private Set<Stone> stones_placed;
	private Set<Stone> stones_ready;

	// 状態を管理するオブジェクト
	class Status {
		private int id;
		private Stone stone;

		public Status(int id, Stone stone) {
			this.id = id;
			this.stone = stone;
		}

		public void set(State state_set) {
			states[id] = state_set;
			// 置かれた石として追加
			if (state_set.isPlaced()) {
				stones_placed.add(stone);
			} else {
				stones_placed.remove(stone);
			}
			// 置かれた石を除去
			if (state_set != State.READY) {
				stones_ready.remove(stone);
			}
		}

		public boolean is(State state) {
			return states[id] == state;
		}

		@Override
		public String toString() {
			return states[id].name();
		}
	}

	public StatusCandidate(StoneBucket candidates, Neighbors neighbors, int num_stones) {
		// 準備
		this.candidates = candidates;
		states = new State[candidates.size()];
		Arrays.fill(states, State.READY);
		this.neighbors = neighbors;
		stones_placed = new HashSet<Stone>(num_stones);
		stones_ready = new HashSet<Stone>(candidates.getStones());
		// 全部の石の候補に状態オブジェクトを渡す
		int id = 0;
		for (Stone stone : candidates.getStones()) {
			stone.setStatusObject(new Status(id++, stone));
		}
	}

	public void reset() {
		// 状態をリセット
		Arrays.fill(states, State.READY);
		// 置かれた石もリセット
		stones_placed.clear();
		// 置ける石もリセット
		stones_ready = new HashSet<Stone>(candidates.getStones());
	}

	public State[] save() {
		return Arrays.copyOf(states, states.length);
	}

	public void load(State[] states) {
		this.states = Arrays.copyOf(states, states.length);
	}

	public Set<Stone> getStonesEdge() {
		Set<Stone> stones_edge = new HashSet<Stone>();
		for (Stone stone_placed : stones_placed) {
			if (stone_placed.isEdge(neighbors)) {
				stones_edge.add(stone_placed);
			}
		}
		return stones_edge;
	}

	public int countStonesReady() {
		return stones_ready.size();
	}
}