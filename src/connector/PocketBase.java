package connector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * An abstraction of <a href="https://pocketbase.io/">PocketBase</a>, with all the methods to operate on its records.
 */
public class PocketBase {
	private final String address;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Instantiates a new PocketBase connection.
	 *
	 * @param address the string address of the database
	 */
	public PocketBase(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	// ==================== COMMON METHODS ====================

	/**
	 * Common method that handles and returns the response of the HTTP request.
	 */
	private String handleResponse(HttpRequest.Builder requestBuilder) throws IOException, InterruptedException, PocketBaseException {
		// Build the request
		HttpRequest request = requestBuilder.build();

		// Send the request and get the response
		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		int statusCode = response.statusCode();

		// If there is an error, throw an exception
		if (statusCode >= 400) {
			throw new PocketBaseException(response.body());
		}

		// If the response is 204, return a JSON with the code and a message
		if (statusCode == 204) {
			return "{\"code\":" + 204 + ", \"message\": \"Successful.\"}";
		}

		return response.body();
	}

	/**
	 * Common method that tries to authenticate a user or admin, given its identity, password and the corresponding url.
	 */
	private String authorize(String identity, String password, String userOrAdminUrl) throws IOException, InterruptedException, PocketBaseException {
		// Create the input JSON
		String jsonData = "{ \"identity\": \"" + identity + "\", \"password\": \"" + password + "\" }";

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(userOrAdminUrl))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonData));

		return handleResponse(requestBuilder);
	}

	private String urlEncode(String s) {
		return s.replace(" ", "%20")
				.replace("&", "%26")
				.replace("|", "%7C");

	}


	// ==================== CRUD METHODS ====================

	/**
	 * Creates a new record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param data       the json of the data to insert
	 * @param authToken      the authorization token
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record createRecord(String collectionName, Map<String, Object> data, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records";

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(data))); // Write the JSON inside the request body

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder.header("Authorization", authToken);
		}

		// Send the request and get the response json
		String response = handleResponse(requestBuilder);

		JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
		Record record = new Record();

		// Iterate through the JSON object and set the values of the record
		jsonObject.entrySet().forEach(entry -> {
			switch (entry.getKey()) {
				case "id":
					record.setId(entry.getValue().getAsString());
					break;
				case "collectionId":
					record.setCollectionId(entry.getValue().getAsString());
					break;
				case "collectionName":
					record.setCollectionName(entry.getValue().getAsString());
					break;
				case "created":
					record.setCreated(entry.getValue().getAsString());
					break;
				case "updated":
					record.setUpdated(entry.getValue().getAsString());
					break;
				default:
					record.getValues().put(entry.getKey(), entry.getValue());
					break;
			}
		});

		return record;
	}

	/**
	 * Creates a new record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param data       the json of the data to insert
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record createRecord(String collectionName, Map<String, Object> data) throws IOException, PocketBaseException, InterruptedException {
		return createRecord(collectionName, data, null);
	}

	/**
	 * Gets all the records from an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param options        the options to filter the records, leave null for no options
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public CollectionPage readAllRecords(String collectionName, String options) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, options, null);
	}

	/**
	 * Gets all the records from a protected collection with the authorization token.
	 *
	 * @param collectionName the collection name
	 * @param authToken      the authorization token
	 * @param options        the options to filter the records, leave null for no options
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public CollectionPage readAllRecords(String collectionName, String options, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records";

		// Add the options if present
		if (options != null)
			url = url + "?" + options;

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(urlEncode(url)))
				.GET();

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder
					.header("Content-Type", "application/json")
					.header("Authorization", authToken);
		}

		String response = handleResponse(requestBuilder);

		// Get the "items" array
		JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
		JsonArray itemsArray = jsonObject.getAsJsonArray("items");
		return null;

	}

	/**
	 * Gets one record from an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public String readOneRecord(String collectionName, String recordId) throws IOException, PocketBaseException, InterruptedException {
		return readOneRecord(collectionName, recordId, null);
	}

	/**
	 * Gets one record from a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @param authToken      the authorization token
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public String readOneRecord(String collectionName, String recordId, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records/" + recordId;

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET();

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder
					.header("Content-Type", "application/json")
					.header("Authorization", authToken);
		}

		return handleResponse(requestBuilder);
	}

	/**
	 * Modifies an existing record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param jsonData       the json representing the data used to update the existing record
	 * @param recordId       the id of the record to update
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public String updateRecord(String collectionName, String jsonData, String recordId) throws IOException, PocketBaseException, InterruptedException {
		return updateRecord(collectionName, jsonData, recordId, null);
	}

	/**
	 * Modifies an existing record given its recordId inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param jsonData       the json representing the data used to update the existing record
	 * @param recordId       the id of the record to update
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public String updateRecord(String collectionName, String jsonData, String recordId, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records/" + recordId;

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.method("PATCH", HttpRequest.BodyPublishers.ofString(jsonData)); // Write the JSON inside the request body

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder.header("Authorization", authToken);
		}

		return handleResponse(requestBuilder);
	}

	/**
	 * Deletes an existing record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record to delete
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public String deleteRecord(String collectionName, String recordId) throws IOException, PocketBaseException, InterruptedException {
		return deleteRecord(collectionName, recordId, null);
	}

	/**
	 * Deletes an existing record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record to delete
	 * @return the json of the response
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public String deleteRecord(String collectionName, String recordId, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records/" + recordId;

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.DELETE(); // Write the JSON inside the request body

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder
					.header("Content-Type", "application/json")
					.header("Authorization", authToken);
		}


		return handleResponse(requestBuilder);
	}

	// ==================== AUTHENTICATION METHODS ====================


	/**
	 * Authenticate a user, given its identity and password.
	 *
	 * @param usersCollectionName the collection name
	 * @param identity            the identity (email)
	 * @param password            the password
	 * @return the json of the response
	 * @throws IOException the database is unreachable
	 */
	public String userAuthentication(String usersCollectionName, String identity, String password) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String usersUrl = address + "/api/collections/" + usersCollectionName + "/auth-with-password";

		return authorize(identity, password, usersUrl);
	}

	/**
	 * Authenticate an admin, given its identity and password.
	 *
	 * @param identity the identity (email)
	 * @param password the password
	 * @return the json of the response
	 * @throws IOException the database is unreachable
	 */
	public String adminAuthentication(String identity, String password) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String adminsUrl = address + "/api/admins/auth-with-password";

		// Create the input JSON
		return authorize(identity, password, adminsUrl);
	}



}
