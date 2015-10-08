
class LPScriptBuilder {
	StringBuilder sb = new StringBuilder();
	int i = 0;

	public void setMinimize(String string) {
		sb.append("minimize\r\n");
		sb.append(string + "\r\n");
		sb.append("subject to\r\n");
	}

	public void addConstraint(String string) {
		sb.append(String.format("c%d: ", i));
		sb.append(string + "\r\n");
	}

	public void setBinary(String string) {
		sb.append("\r\n");
		sb.append("binary\r\n");
		sb.append(string);
	}

	public void setGeneral(String string) {
		sb.append("\r\n");
		sb.append("general\r\n");
		sb.append(string);
	}

	@Override
	public String toString() {
		sb.append("\r\n");
		sb.append("end");
		return sb.toString();
	}
}