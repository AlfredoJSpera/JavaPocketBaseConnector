package testing;

import connector.PocketBase;
import connector.PocketBaseException;

import java.io.IOException;

public class Hello {
	private static final String ADMIN_EMAIL = "adminroot@admin.com";
	private static final String ADMIN_PASS = "password123";

	private static final String USER_EMAIL = "abc@kljwe.com";
	private static final String USER_PASS = "Password";

	public static void main(String[] args) throws IOException, InterruptedException, PocketBaseException {
		PocketBase pb = new PocketBase("http://127.0.0.1:8090");
		System.out.println(pb.userAuthentication("users", USER_EMAIL, USER_PASS));
		System.out.println(pb.adminAuthentication(ADMIN_EMAIL, ADMIN_PASS));
	}
}