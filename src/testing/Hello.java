package testing;

import connector.PBRecord;
import connector.PBValue;
import connector.PocketBase;

import java.util.List;
import java.util.Map;

public class Hello {
	private static final String ADMIN_EMAIL = "adminroot@admin.com";
	private static final String ADMIN_PASS = "password123";

	private static final String USER_EMAIL = "abc@kljwe.com";
	private static final String USER_PASS = "Password";

	private static final String COLLECTION = "posts";
	private static final String RECORD = "95jh01oozx0qwg8";

	private static final String GIANTS = "C:/Users/Alfredo/Desktop/the-giants-causeway.png";
	private static final String WAVES = "C:/Users/Alfredo/Desktop/wave.png";
	private static final String SAVE_PATH = "C:/Users/Alfredo/Desktop/Hello.png";

	public static void main(String[] args) throws Exception {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");

		PBRecord record = pb.readOneRecord(COLLECTION, RECORD);
		Map<String, PBValue> values = record.getValues();
		List<String> types = values.get("type").getList();
		types.remove(1);
		types.add("panorama");

		pb.updateRecord(COLLECTION, RECORD, values);

		System.out.println("types = " + types);









	}
}