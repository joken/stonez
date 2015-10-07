import java.util.ArrayList;
import java.util.List;

class LPScriptBuilder {

	private String minimize;
	private List<String> constraints = new ArrayList<String>();
	private List<String> binaries = new ArrayList<String>();
	private List<String> generals = new ArrayList<String>();

	public void setMinimize(String string) {
		minimize = string;
	}

	public void addConstraint(String string) {
		constraints.add(string);
	}

	public void addBinary(String string) {
		binaries.add(string);
	}

	public void addGeneral(String string) {
		generals.add(string);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("minimize\r\n");
		sb.append(minimize + "\r\n");
		sb.append("subject to\r\n");
		for (int i = 0; i < constraints.size(); i++) {
			sb.append(String.format("c%d: ", i));
			sb.append(constraints.get(i) + "\r\n");
		}
		sb.append("general\r\n");
		for (int i = 0; i < generals.size(); i++) {
			sb.append(generals.get(i) + " ");
		}
		sb.append("\r\n");
		sb.append("binary\r\n");
		for (int i = 0; i < binaries.size(); i++) {
			sb.append(binaries.get(i) + " ");
		}
		sb.append("\r\n");
		sb.append("end");
		return sb.toString();
	}
}