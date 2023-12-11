package testing;

import connector.CollectionPage;
import connector.PocketBase;
import connector.PocketBaseException;
import connector.Record;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Hello {
	// adminroot@admin.com
	// password123

	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");
		Record r = pb.readOneRecord("posts", "a08eq0qsow53pvw");

		// Print the original HashMap
		System.out.println("Original HashMap: " + r.getValues());

		r.getValues().merge("content", "Whoaaaa this is a content body thingy", (oldValue, newValue) -> newValue);

		Record updated = pb.updateRecord("posts", r, "a08eq0qsow53pvw");
		System.out.println("Updated HashMap: " + updated.getValues());


	}
}