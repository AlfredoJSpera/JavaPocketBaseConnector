package testing;

import connector.PocketBase;
import connector.PocketBaseException;

import java.io.IOException;

public class Hello {
	// adminroot@admin.com
	// password123
	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		PocketBase operations = new PocketBase("http://localhost:8090");
		System.out.println(operations.adminAuthentication("adminroot@admin.com", "password123"));

	}
}
