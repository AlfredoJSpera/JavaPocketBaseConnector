package testing;

import connector.PBRecord;
import connector.PocketBase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Hello {
	private static final String ADMIN_EMAIL = "adminroot@admin.com";
	private static final String ADMIN_PASS = "password123";

	private static final String USER_EMAIL = "abc@kljwe.com";
	private static final String USER_PASS = "Password";

	private static final String COLLECTION = "posts";
	private static final String RECORD = "nntjxpwbrstk9qo";

	private static final String INPUT_PATH = "C:/Users/Alfredo/Desktop/the-giants-causeway.png";
	private static final String SAVE_PATH = "C:/Users/Alfredo/Desktop/Hello.png";

	public static void main(String[] args) throws Exception {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");

		PBRecord record = pb.readOneRecord(COLLECTION, RECORD);
		System.out.println("record = " + record);
		pb.downloadFile(COLLECTION, RECORD, record.getValues().get("image").getStringListValue().get(0), SAVE_PATH);

	}
}