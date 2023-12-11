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
		System.out.println(pb.deleteRecord("posts", "rsk2io9cxo5yo61"));


	}
}