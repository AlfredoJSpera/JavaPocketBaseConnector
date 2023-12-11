package connector;

import java.util.List;

public class PocketBaseException extends Exception {

	public PocketBaseException(int errorCode, String errorMessage) {
		super(errorMessage.replace(".", " ("+ errorCode + ").\n"));
	}

	public PocketBaseException(int errorCode, String errorMessage, List<ErrorInformationWrapper> errors) {
		super(errorMessage.replace(".", " ("+ errorCode + ").")
				+ " Errors: " + errors.toString().replace("[", "").replace("]", "") + "\n"
		);
	}
}