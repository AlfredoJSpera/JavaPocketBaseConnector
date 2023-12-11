package connector;

public class PocketBaseException extends Exception {

	// >= 400
	public PocketBaseException(int errorCode, String errorMessage) {
		super(errorMessage.replace(".", " ("+ errorCode + ")."));
	}

	public PocketBaseException(int errorCode, String errorMessage, String unknownEntry, String rawJson) {
		super(errorMessage.replace(".", ", ")
				+ "Unknown Error [" + unknownEntry + ", " + errorCode + "], json: \n"
				+ rawJson
		);
	}

	// 400
	public PocketBaseException(int errorCode, String errorMessage, String info, String infoCodeMeaning, String infoMessage) {
		super(errorMessage.replace(".", ", ")
				+ "[" + info + "] "
				+ infoMessage.toLowerCase().replace(".", " (" + errorCode + ", " + infoCodeMeaning + ").")
		);
	}
}