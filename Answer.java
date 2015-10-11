
public class Answer {
	private int score;
	private int num_stones;
	private String data;

	public Answer(int score, int num_stones, String data) {
		this.score = score;
		this.num_stones = num_stones;
		this.data = data;
	}

	public boolean submit(AnswerSubmitter submitter) {
		return submitter.submit(score, num_stones, data);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Header: %d %d %d\n", score, num_stones, data.split("\n").length - 1));
		sb.append(String.format("Data: %s\n", data));
		return sb.toString();
	}
}