package testing;

import connector.PBValues;
import connector.PocketBase;

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
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");


		List<String> types = new ArrayList<>();
		types.add("other");
		types.add("panorama");
		/*
		List<String> images = new ArrayList<>();
		images.add(GIANTS);
		images.add(WAVES);
		*/
		Map<String, PBValues> values = new HashMap<>();
		values.put("title", new PBValues().set("Something"));
		values.put("content", new PBValues().set("other"));
		values.put("views", new PBValues().set("1"));
		//values.put("image", new PBValues().setList(images));
		values.put("type", new PBValues().setList(types));

		pb.createRecord(COLLECTION, values);




		/*
		PBRecord record = pb.readOneRecord(COLLECTION, RECORD);
		System.out.println("record = " + record);
		List<String> list = record.getValues().get("image").getStringList();
		System.out.println("list = " + list);
		//list.set(0, "");
		//list.add(GIANTS);
		list.clear();
		list.add("");
		System.out.println("list = " + list);
		pb.updateRecordWithFiles(COLLECTION, record.getId(), record.getValues());
		*/


		// ADD A NEW FILE: filesStringList.add(path);
		// REMOVE A FILE: filesStringList.set(index, "");
		// REMOVE ALL FILES: filesStringList.clear(); filesStringList.add("");
		// UPDATE A FILE: filesStringList.set(index, path);

	}
}