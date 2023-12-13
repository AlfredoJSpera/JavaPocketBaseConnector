package connector;

import java.io.File;
import java.util.List;

/**
 * This class is used to store the possible values of a record.
 * The value can be a String, a List of Strings or a List of Files.
 * Only one of these values can be set at a time.
 */
public class PBValues {
	private String stringValue;
	private List<String> stringListValue;

	public PBValues() {
		stringValue = null;
		stringListValue = null;
	}

	public PBValues setStringValue(String stringValue) {
		this.stringListValue = null;
		this.stringValue = stringValue;
		return this;
	}

	public PBValues setStringListValue(List<String> stringListValue) {
		this.stringValue = null;
		this.stringListValue = stringListValue;
		return this;
	}

	public String getStringValue() {
		return stringValue;
	}

	public List<String> getStringListValue() {
		return stringListValue;
	}

	@Override
	public String toString() {
		if (stringValue != null) {
			return stringValue;
		} else if (stringListValue != null) {
			return stringListValue.toString();
		} else {
			return "";
		}
	}
}
