package connector;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * This class is used to store the possible values of a record.
 * The value can be a String or a List of Strings.
 * Only one of these values can be set at a time.
 */
public class PBValues {
	private String string;
	private List<String> stringList;

	// TODO: Add support for relations

	public PBValues() {
		string = null;
		stringList = null;
	}

	/**
	 * Set the value of this object to a String.
	 */
	public PBValues set(String string) {
		this.stringList = null;
		this.string = string;
		return this;
	}

	/**
	 * Set the value of this object to a List of Strings.
	 */
	public PBValues setList(List<String> stringList) {
		this.string = null;
		this.stringList = stringList;
		return this;
	}

	/**
	 * Get the value of this object as a String.
	 */
	public String get() {
		return string;
	}

	/**
	 * Get the value of this object as a List of Strings.
	 */
	public List<String> getList() {
		return stringList;
	}

	public static boolean isString(PBValues value) {
		return value.get() != null;
	}

	public static boolean isStringList(PBValues value) {
		return value.getList() != null;
	}

	@Override
	public String toString() {
		if (string != null) {
			return string;
		} else if (stringList != null) {
			return stringList.toString();
		} else {
			return "";
		}
	}

	/**
	 * This class is used to serialize and deserialize the PBValues class, so that it can be used directly with Gson.
	 */
	static class PBValuesTypeAdapter extends TypeAdapter<PBValues> {
		@Override
		public void write(JsonWriter out, PBValues value) throws IOException {
			if (value != null) {
				if (value.get() != null) {
					// STRING
					out.value(value.get());
				} else if (value.getList() != null) {
					// STRING LIST
					out.beginArray();
					for (String str : value.getList()) {
						out.value(str);
					}
					out.endArray();
				}
			} else {
				out.nullValue();
			}
		}

		@Override
		public PBValues read(JsonReader in) throws IOException {
			return null;
		}
	}
}
