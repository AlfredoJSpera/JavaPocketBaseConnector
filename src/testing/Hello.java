package testing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import connector.PocketBase;
import connector.PocketBaseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Hello {
	// adminroot@admin.com
	// password123
	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");
		Map<String, Object> values = new HashMap<>();
		values.put("title", "mytest");
		values.put("content", "mycontent");
		values.put("views", 2);

		System.out.println(pb.createRecord("posts", values));

	}
}