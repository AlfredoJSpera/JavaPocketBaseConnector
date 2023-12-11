package connector;

public class ErrorInformationWrapper {
	private final String cause;
	private final String infoCode;
	private final String infoMessage;

	public ErrorInformationWrapper(String cause, String infoCode, String infoMessage) {
		this.cause = cause;
		this.infoCode = infoCode;
		this.infoMessage = infoMessage;
	}

	public String getCause() {
		return cause;
	}

	public String getInfoCode() {
		return infoCode;
	}

	public String getInfoMessage() {
		return infoMessage;
	}

	@Override
	public String toString() {
		return "\n\tCause='" + cause + '\''
				+ ", " + infoMessage.replace(".", "")
				+ " (" + infoCode + ")";
	}
}
