package testing;

import connector.PocketBase;
import connector.PocketBaseException;

import java.io.IOException;

public class Hello {
	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		PocketBase pb = new PocketBase("http://localhost:8090");

		String record = "{ \"views\": 123, \"post\": \"test\" }";

		pb.createRecord("posts", record);

	}
}
