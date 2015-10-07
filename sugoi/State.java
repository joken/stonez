enum State {
	/** 配置可能 */
    READY,
    /** この石の候補が置かれている */
    PLACED_THIS,
    /** 同じ石番号の他の石の候補が置かれている */
    PLACED_OTHER,
    /** 既に置かれた石と重なっていて置けない */
    OVERLAPPED,
    /** 置かれていて配置可能な石の候補が1つ以上隣接している */
    EDGE,;

    public boolean isPlaced() {
		return this == PLACED_THIS || this == EDGE;
	}
}
