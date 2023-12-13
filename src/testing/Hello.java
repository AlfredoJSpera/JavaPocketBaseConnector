package testing;

import connector.PBRecord;
import connector.PBValues;
import connector.PBOperations;

import java.util.*;

public class Hello {
	private static final String ADMIN_EMAIL = "adminroot@admin.com";
	private static final String ADMIN_PASS = "password123";

	private static final String USER_EMAIL = "abc@kljwe.com";
	private static final String USER_PASS = "Password";

	private static final String COLLECTION = "posts";
	private static final String RECORD = "rvem2iu77xwe1ef";

	private static final String GIANTS = "C:/Users/Alfredo/Desktop/the-giants-causeway.png";
	private static final String WAVES = "C:/Users/Alfredo/Desktop/wave.png";
	private static final String SAVE_PATH = "C:/Users/Alfredo/Desktop/Hello.png";

	public static void main(String[] args) throws Exception {
		PBOperations pb = new PBOperations("http://127.0.0.1:8090");

		/*
		Map<String, PBValues> values = new HashMap<>();
		values.put("title", new PBValues().setString("Hello World!"));
		values.put("content", new PBValues().setString("content"));
		values.put("views", new PBValues().setString("145"));

		List<File> images = new ArrayList<>();
		images.add(new File(GIANTS));
		images.add(new File(WAVES));

		List<String> types = new ArrayList<>();
		types.add("other");
		types.add("panorama");

		values.put("image", new PBValues().setFileList(images));
		values.put("type", new PBValues().setStringList(types));

		PBRecord record = pb.createRecordWithFiles(COLLECTION, values);
		*/
		
		PBRecord record = pb.readOneRecord(COLLECTION, RECORD);
		System.out.println("record = " + record);
		Map<String, PBValues> files = new HashMap<>();

		files.put("image", new PBValues().setStringList(List.of(GIANTS)));
		record.setValues(files);

		// ADD A NEW FILE: filesStringList.add(path);
		// REMOVE A FILE WHEN THERE ARE MORE THAN ONE: filesStringList.remove(index);
		// REMOVE ALL FILES: filesStringList.clear(); filesStringList.add("");
		// UPDATE A FILE: filesStringList.remove(index); filesStringList.add(index, path);


		System.out.println("record = " + record);
		record = pb.updateRecordWithFiles(COLLECTION, record.getId(), record.getValues());
		System.out.println("record = " + record);

	}
}