package testing;

import connector.*;
import connector.PBRecord;

import java.awt.*;
import java.io.File;
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

		String token = pb.adminAuthentication(ADMIN_EMAIL, ADMIN_PASS).getToken();


		PBRecord record = pb.readOneRecord(COLLECTION, RECORD, token);
		Map<String, Object> values = record.getValues();
		String imageName = (String) values.get("image");

		System.out.println(values.get("title") + ", " + values.get("content") + ", " + values.get("views") + ", "
				+ record.getCreated() + ", " + imageName);

		File file = pb.downloadFile(COLLECTION, RECORD, imageName, SAVE_PATH, token);
		System.out.println("Downloaded file to: " + file.getAbsolutePath());
		openFileInDefaultViewer(file);

	}

	public static void openFileInDefaultViewer(File file) throws Exception {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		} else {
			System.out.println("Opening files is not supported on this platform.");
		}
	}
}