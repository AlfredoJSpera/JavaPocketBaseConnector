package connector;

public class PocketBaseException extends Exception {

	// >= 400
	public PocketBaseException(String errorCode, String errorMessage) {
		super(errorMessage.replace(".", " ("+ errorCode + ")."));
	}

	// 400
	public PocketBaseException(int errorCode, String errorMessage, String infoCodeMeaning, String infoMessage) {
		super(errorMessage.replace(".", ", ")
			+ infoMessage.toLowerCase().replace(".", " (" + errorCode + ", " + infoCodeMeaning + ").")
		);
	}
}