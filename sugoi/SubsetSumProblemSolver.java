import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 部分和問題のソルバ
 *
 * @author Kazuaki
 *
 */
public class SubsetSumProblemSolver {

	private static final int MAX_SIZE_FIELD = 1025;

	// 全体の集合
	private List<Integer> set;

	// 計算結果
	private List<HashSet<Integer>> memo = new ArrayList<HashSet<Integer>>(
			MAX_SIZE_FIELD);

	/**
	 * 全体の集合とフィールドの大きさを与えて初期化
	 */
	public SubsetSumProblemSolver(List<Integer> set, int size_field) {
		this.set = set;
		for (int i = 0; i < size_field + 1; i++) {
			memo.add(new HashSet<Integer>());
		}
		memo.get(0).add(0);
	}

	/**
	 * 解く
	 */
	public List<HashSet<Integer>> solve() {
		// DPっぽい
		for (int element : set) {
			int[] subSumToAppend = new int[memo.size()];
			// 部分和をつくっていく
			for (int subSum = 0; subSum < memo.size(); subSum++) {
				HashSet<Integer> hashSet = memo.get(subSum);
				if (hashSet.isEmpty()) {
					// この subSum の値はまだ不可能
					continue;
				}
				// 更新するために部分和を記録
				if (subSum + element < memo.size()) {
					subSumToAppend[subSum] = element;
				}
			}
			// 部分和を更新
			for (int subSum = 0; subSum < subSumToAppend.length; subSum++) {
				if (subSumToAppend[subSum] != 0) {
					memo.get(subSum + subSumToAppend[subSum]).add(
							subSumToAppend[subSum]);
				}
			}
		}
		return memo;
	}
}