package testing;

import com.google.gson.JsonNull;
import connector.*;
import connector.PBRecord;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Hello {
	private static final String ADMIN_EMAIL = "adminroot@admin.com";
	private static final String ADMIN_PASS = "password123";

	private static final String USER_EMAIL = "abc@kljwe.com";
	private static final String USER_PASS = "Password";

	private static final String COLLECTION = "posts";
	private static final String RECORD = "a08eq0qsow53pvw";

	private static final String SAVE_PATH = "C:/Users/Alfredo/Desktop/test.png";

	public static void main(String[] args) throws Exception {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");

		//String token = pb.adminAuthentication(ADMIN_EMAIL, ADMIN_PASS).getToken();

		PBRecord r = pb.readOneRecord(COLLECTION, RECORD);
		r.getValues().put("image", null);
		r = pb.updateRecord(COLLECTION, r);

		/*
		Map<String, Object> values = new HashMap<>();
		values.put("title", "Waters");
		values.put("content", "Photos of water.");
		values.put("views", 62);
		Map<String, File> files = new HashMap<>();
		values.put("image", new File("C:/Users/Alfredo/Desktop/the-giants-causeway.png"));

		PBRecord r = pb.createRecordWithFiles(COLLECTION, values, files);
		System.out.println("r = " + r);
		*/
	}
}