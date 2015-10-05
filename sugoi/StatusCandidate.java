class StatusCandidate {
	private char[] state;
	private int size;

	class Status {
		private int id;

		public Status(int id) {
			this.id = id;
		}

		public State getState() {
			return State.values()[state[id] - '0'];
		}

		public void set(State state_set) {
			state[id] = state_set.getIndex();
		}
	}

	public StatusCandidate(StoneBucket candidates) {
		size = candidates.size();
		state = new char[size];
		int id = 0;
		for (Stone stone : candidates.getStones()) {
			stone.setStatusObject(new Status(id++));
		}
	}

	public String save() {
		return new String(state);
	}

	public void load(String state) {
		this.state = state.toCharArray();
	}
}