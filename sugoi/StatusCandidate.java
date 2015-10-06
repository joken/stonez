import java.util.Arrays;

class StatusCandidate {
	private State[] states;
	private int size;

	class Status {
		private int id;

		public Status(int id) {
			this.id = id;
		}

		public State getState() {
			return states[id];
		}

		public void set(State state_set) {
			states[id] = state_set;
		}
	}

	public StatusCandidate(StoneBucket candidates) {
		size = candidates.size();
		states = new State[size];
		int id = 0;
		for (Stone stone : candidates.getStones()) {
			stone.setStatusObject(new Status(id++));
		}
	}

	public State[] save() {
		return Arrays.copyOf(states, states.length);
	}

	public void load(State[] states) {
		this.states = Arrays.copyOf(states, states.length);
	}
}