package testing;

import connector.CollectionPage;
import connector.PocketBase;
import connector.PocketBaseException;

import java.io.IOException;

public class Hello {
	// adminroot@admin.com
	// password123
	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");
		System.out.println(pb.readOneRecord("posts", "sear"));



	}
}