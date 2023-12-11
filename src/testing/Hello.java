package testing;

import connector.PBQuery;
import connector.PocketBase;
import connector.PocketBaseException;
import connector.Record;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Hello {
	private static final String ADMIN_EMAIL = "adminroot@admin.com";
	private static final String ADMIN_PASS = "password123";

	private static final String USER_EMAIL = "abc@kljwe.com";
	private static final String USER_PASS = "Password";

	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		// Creates a PocketBase instance
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");

		List<Record> list = pb.readAllRecords("posts",
				new PBQuery(
					"-views",
					"views > 60 && title = \"Hello World\"",
					null
				)
		).getItems();

		printAll("Filtered list:", list);


		list = pb.readAllRecords("posts").getItems();

		printAll("Normal list:", list);

		list = pb.readAllRecords("posts",
				new PBQuery(
					1,
					2
				)
		).getItems();

		printAll("Paginated list (part 1):", list);

		list = pb.readAllRecords("posts",
				new PBQuery(
						2,
						2
				)
		).getItems();

		printAll("Paginated list (part 2):", list);

		list = pb.readAllRecords("posts",
				new PBQuery(
						"views",
						null,
						null
				)
		).getItems();

		printAll("Sorted list:", list);
	}

	public static void printAll(String title, List<Record> list) {
		System.out.println(title);
		for (Record record : list) {
			Map<String, Object> values = record.getValues();
			System.out.println(values.get("title") + ", " + values.get("content") + ", " + values.get("views") + ", " + record.getCreated());
		}
		System.out.println("--------------------");
	}
}