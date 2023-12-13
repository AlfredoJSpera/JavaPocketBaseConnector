package connector;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
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
	private List<File> fileList;

	// TODO: Add support for relations

	public PBValues() {
		string = null;
		stringList = null;
	}

	/**
	 * Set the value of this object to a String.
	 */
	public PBValues setString(String string) {
		this.stringList = null;
		this.string = string;
		return this;
	}

	/**
	 * Set the value of this object to a List of Strings.
	 */
	public PBValues setStringList(List<String> stringList) {
		this.string = null;
		this.stringList = stringList;
		return this;
	}

	/**
	 * Set the value of this object to a List of Files.
	 */
	public PBValues setFileList(List<File> fileList) {
		this.string = null;
		this.stringList = null;
		this.fileList = fileList;
		return this;
	}

	/**
	 * Get the value of this object as a String.
	 */
	public String getString() {
		return string;
	}

	/**
	 * Get the value of this object as a List of Strings.
	 */
	public List<String> getStringList() {
		return stringList;
	}

	/**
	 * Get the value of this object as a List of Files.
	 */
	public List<File> getFileList() {
		return fileList;
	}

	public static boolean isString(PBValues value) {
		return value.getString() != null;
	}

	public static boolean isStringList(PBValues value) {
		return value.getStringList() != null;
	}

	public static boolean isFileList(PBValues value) {
		return value.getFileList() != null;
	}

	@Override
	public String toString() {
		if (string != null) {
			return string;
		} else if (stringList != null) {
			return stringList.toString();
		} else if (fileList != null) {
			return fileList.toString();
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
				if (value.getString() != null) {
					// STRING
					out.value(value.getString());
				} else if (value.getStringList() != null) {
					// STRING LIST
					out.beginArray();
					for (String str : value.getStringList()) {
						out.value(str);
					}
					out.endArray();
				} else if (value.getFileList() != null) {
					// FILE LIST
					out.beginArray();
					for (File file : value.getFileList()) {
						out.value(file.getPath());
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
