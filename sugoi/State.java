enum State {
    READY('0'),
    PLACED_THIS('1'),
    PLACED_OTHER('2'),
    OVERLAPPED('3'),;

	private char index;

	private State(char index) {
		this.index = index;
	}

	public char getIndex() {
		return index;
	}
}
