class StoneOperation {
	static final int NORMAL = 0;
	static final int ROTATE90 = 1;
	static final int ROTATE180 = 2;
	static final int ROTATE270 = 3;
	static final int FLIP = 4;
	static final int COUNT = 8;

	static String export(int op) {
		String flip = ((op & 4) > 0) ? "T" : "H";
		int rotate = (((op & 1) > 0) ? 90 : 0) + (((op & 2) > 0) ? 180 : 0);
		return String.format("%s %d", flip, rotate);
	}
}