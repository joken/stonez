import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Neighbors {
	/** 石の周長の最大 */
	private static final int CIRCUMFERENCE_STONE = 34;

	/** 全部の石の数 */
	private int num_stones;
	/** 石の候補 key に隣接する石の候補 */
	Map<Stone, StoneBucket> neighbors;
	/** 敷地の行 i の列 j に置くことができる石の候補 */
	private StoneBucket[][] candidates_by_position;

	public Neighbors(int num_stones, StoneBucket[][] candidates_by_position) {
		this.num_stones = num_stones;
		this.candidates_by_position = candidates_by_position;
		neighbors = new HashMap<Stone, StoneBucket>(num_stones, 1.0f);
	}

	public Collection<StoneBucket> getNeighbors() {
		return neighbors.values();
	}

	public Set<Stone> getNeighbors(Stone stone_placed) {
		Set<Stone> stones_neighbor;
		if (!neighbors.containsKey(stone_placed)) {
			stones_neighbor = findNeighbors(stone_placed);
			neighbors.put(stone_placed, new StoneBucket(stones_neighbor));
		} else {
			stones_neighbor = neighbors.get(stone_placed).getStones();
		}
		return stones_neighbor;
	}

	public Set<Stone> getNeighbors(Stone stone_placed, boolean cached) {
		if (!cached) {
			return findNeighbors(stone_placed);
		}
		return getNeighbors(stone_placed);
	}

	private Set<Stone> findNeighbors(Stone stone_me) {
		// 準備
		int i_field_me = stone_me.getIField();
		int value_me = stone_me.getValue();
		int[] lines = stone_me.getStone();
		// 膨張処理をして輪郭をとる
		int[] work = new int[Stone.SIZE_STONE + 2];
		for (int j_stone = 0; j_stone < Stone.SIZE_STONE; j_stone++) {
			work[j_stone + 0] |= lines[j_stone] << 1;
			work[j_stone + 1] |= lines[j_stone] | lines[j_stone] << 2;
			work[j_stone + 2] |= lines[j_stone] << 1;
		}
		for (int j_stone = 0; j_stone < Stone.SIZE_STONE; j_stone++) {
			work[j_stone + 1] &= ~ (lines[j_stone] << 1);
		}
		// 石候補から輪郭にかぶるものを返す
		Set<Stone> stones = new HashSet<>(num_stones * StoneOperation.COUNT * CIRCUMFERENCE_STONE);
		for (int j_work = 0; j_work < work.length; j_work++) {
			int line = work[j_work];
			if (line == 0) {
				continue;
			}
			int index_i_field_put = i_field_me + j_work - 1;
			if (index_i_field_put < 0) {
				continue;
			}
			if (index_i_field_put >= Stone.SIZE_FIELD) {
				break;
			}
			for (int value_work = 0; value_work < work.length; value_work++) {
				if (((line >> (work.length - 1 - value_work)) & 1) == 1) {
					int index_value_put = value_me + value_work - 1;
					if (index_value_put < 0) {
						continue;
					}
					if (index_value_put >= Stone.SIZE_FIELD) {
						break;
					}
					for (Stone stone : candidates_by_position[index_i_field_put][index_value_put].getStones()) {
						if (!stone.isReady()) {
							continue;
						}
						stones.add(stone);
					}
				}
			}
		}
		return stones;
	}
}