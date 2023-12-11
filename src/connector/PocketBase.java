package connector;

import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 *
	 * @param requestBuilder the request builder
	 * @return the json response of the HTTP request
	 */
	private String handleResponse(HttpRequest.Builder requestBuilder) throws IOException, InterruptedException, PocketBaseException {
		// Build the request
		HttpRequest request = requestBuilder.build();

		// Send the request and get the response
		HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

		int statusCode = response.statusCode();

		// If there is an error, throw an exception
		if (statusCode >= 400) {
			handleResponseError(response.body());
		}

		// Used for a successful delete request
		if (statusCode == 204) {
			return "204";
		}

		return response.body();
	}

	/**
	 * Method that handles the errors of the HTTP request.
	 *
	 * @param body the json body of the response
	 */
	private void handleResponseError(String body) throws PocketBaseException {
		JsonObject errorJson = gson.fromJson(body, JsonObject.class);

		// Get the error code and message
		int errorCode = errorJson.get("code").getAsInt();
		String errorMessage = errorJson.get("message").getAsString();

		// Get the info code and message
		JsonObject data = errorJson.get("data").getAsJsonObject();

		if (!data.isEmpty()) {
			// Get a string to iterate through
			String entrySetStr = data.entrySet().toString();

			List<String> errorPoints = extractWords(entrySetStr, "\\b\\w+\\s*=\\{");
			List<ErrorInformationWrapper> infos = new ArrayList<>();

			for (String errorPoint : errorPoints) {
				JsonObject errorPointJson = null;
				try {
					errorPointJson = data.get(errorPoint).getAsJsonObject();
					String infoCode = errorPointJson.get("code").getAsString();
					String infoMessage = errorPointJson.get("message").getAsString();
					infos.add(new ErrorInformationWrapper(errorPoint, infoCode, infoMessage));
				} catch (Exception e) {
					infos.add(new ErrorInformationWrapper(errorPoint, "Unknown Code", "Unknown Error"));
				}
			}

			// Exception with specific errors
			throw new PocketBaseException(errorCode, errorMessage, infos);
		} else {
			// For general errors
			throw new PocketBaseException(errorCode, errorMessage);
		}
	}

	/**
	 * Extracts all the words from a string that match a regex.
	 *
	 * @param input the input string
	 * @return the list of words
	 */
	private static List<String> extractWords(String input, String regex) {
		List<String> resultList = new ArrayList<>();

		// Define the regex pattern
		Pattern pattern = Pattern.compile(regex);

		// Create a matcher object
		Matcher matcher = pattern.matcher(input);

		// Find all matches
		while (matcher.find()) {
			// Get the matched word and add it to the list
			String matchedWord = matcher.group().trim();
			resultList.add(matchedWord.substring(0, matchedWord.length() - 2));
		}

		return resultList;
	}

	/**
	 * Common method that tries to authenticate a user or admin
	 *
	 * @param identity       the identity (email)
	 * @param password       the password
	 * @param userOrAdminUrl the url for the user or the admin
	 */
	private String authorize(String identity, String password, String userOrAdminUrl) throws IOException, InterruptedException, PocketBaseException {
		// Create the input JSON
		JsonObject inputJson = new JsonObject();
		inputJson.addProperty("identity", identity);
		inputJson.addProperty("password", password);

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(userOrAdminUrl))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(inputJson.toString()));

		return handleResponse(requestBuilder);
	}

	/**
	 * Common method that builds a record from a JSON object.
	 *
	 * @param object the JSON object
	 * @return the record built
	 */
	private Record buildRecord(JsonObject object) {
		Record record = new Record();

		// Iterate through the JSON object and set the values of the record
		object.entrySet().forEach(entry -> {
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
					record.getValues().put(entry.getKey(), entry.getValue().getAsString());
					break;
			}
		});

		return record;
	}

	/**
	 * Encodes a string to be used in a URL.
	 * @param s the string to encode
	 * @return the encoded string
	 */
	private String urlEncode(String s) {
		return s.replace(" ", "%20")
				.replace("&&", "%26%26")
				.replace("|", "%7C")
				.replace("<", "%3C")
				.replace(">", "%3E")
				.replace("-", "%2D")
				.replace("\"", "%22");

	}

	// ==================== CRUD METHODS ====================

	/**
	 * Creates a new record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param data           the map containing the data to insert
	 * @param authToken      the authorization token
	 * @return the record created
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
		return buildRecord(jsonObject);
	}

	/**
	 * Creates a new record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param record         the record containing to insert
	 * @param authToken      the authorization token
	 * @return the record created
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record createRecord(String collectionName, Record record, String authToken) throws IOException, PocketBaseException, InterruptedException {
		return createRecord(collectionName, record.getValues(), authToken);
	}

	/**
	 * Creates a new record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param data           the map containing the data to insert
	 * @return the record created
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record createRecord(String collectionName, Map<String, Object> data) throws IOException, PocketBaseException, InterruptedException {
		return createRecord(collectionName, data, null);
	}

	/**
	 * Creates a new record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param record         the record containing the data to insert
	 * @return the record created
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record createRecord(String collectionName, Record record) throws IOException, PocketBaseException, InterruptedException {
		return createRecord(collectionName, record.getValues(), null);
	}


	/**
	 * Gets all the records from a protected collection with the authorization token and query options.
	 *
	 * @param collectionName the collection name
	 * @param queryOptions   the options for the query of the records
	 * @param authToken      the authorization token
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public CollectionPage readAllRecords(String collectionName, PBQuery queryOptions, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records";

		// Add the options if present
		if (queryOptions != null)
			url = url + "?" + queryOptions;

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

		// Send the request and get the response json
		String response = handleResponse(requestBuilder);

		// Create the collection page
		JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
		CollectionPage collectionPage = new CollectionPage(
				jsonObject.get("page").getAsString(),
				jsonObject.get("perPage").getAsString(),
				jsonObject.get("totalPages").getAsString(),
				jsonObject.get("totalItems").getAsString()
		);

		// Items in the collection page
		JsonArray items = jsonObject.getAsJsonArray("items");
		items.forEach(item -> {
			JsonObject itemObject = item.getAsJsonObject();
			Record record = buildRecord(itemObject);
			collectionPage.getItems().add(record);
		});

		return collectionPage;
	}

	/**
	 * Gets all the records from a protected collection with the authorization token.
	 *
	 * @param collectionName the collection name
	 * @param authToken      the authorization token
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public CollectionPage readAllRecords(String collectionName, String authToken) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, null, authToken);
	}

	/**
	 * Gets all the records from an unprotected collection with query options.
	 *
	 * @param collectionName the collection name
	 * @param queryOptions   the options for the query of the records
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public CollectionPage readAllRecords(String collectionName, PBQuery queryOptions) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, queryOptions, null);
	}

	/**
	 * Gets all the records from an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public CollectionPage readAllRecords(String collectionName) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, null, null);
	}

	/**
	 * Gets one record from a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @param authToken      the authorization token
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record readOneRecord(String collectionName, String recordId, String authToken) throws IOException, PocketBaseException, InterruptedException {
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

		String response = handleResponse(requestBuilder);
		return buildRecord(gson.fromJson(response, JsonObject.class));
	}

	/**
	 * Gets one record from an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @return the record found
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record readOneRecord(String collectionName, String recordId) throws IOException, PocketBaseException, InterruptedException {
		return readOneRecord(collectionName, recordId, null);
	}

	/**
	 * Modifies an existing record given its recordId inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param updatedRecord  the updated updatedRecord
	 * @return the updated record
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record updateRecord(String collectionName, Record updatedRecord, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records/" + updatedRecord.getId();

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(updatedRecord.getValues()))); // Write the JSON inside the request body

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder.header("Authorization", authToken);
		}

		String response = handleResponse(requestBuilder);
		return buildRecord(gson.fromJson(response, JsonObject.class));
	}

	/**
	 * Modifies an existing record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param updatedRecord  the updated updatedRecord
	 * @return the updated record
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public Record updateRecord(String collectionName, Record updatedRecord) throws IOException, PocketBaseException, InterruptedException {
		return updateRecord(collectionName, updatedRecord, null);
	}

	/**
	 * Deletes an existing record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record to delete
	 * @return true if the record has been deleted, otherwise an exception is thrown
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public boolean deleteRecord(String collectionName, String recordId, String authToken) throws IOException, PocketBaseException, InterruptedException {
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


		return handleResponse(requestBuilder).equals("204");
	}

	/**
	 * Deletes an existing record inside an unprotected collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record to delete
	 * @return true if the record has been deleted, otherwise an exception is thrown
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public boolean deleteRecord(String collectionName, String recordId) throws IOException, PocketBaseException, InterruptedException {
		return deleteRecord(collectionName, recordId, null);
	}

	// ==================== AUTHENTICATION METHODS ====================


	/**
	 * Authenticate a regular user.
	 *
	 * @param usersCollectionName the collection name
	 * @param identity            the identity (email)
	 * @param password            the password
	 * @return the json of the response
	 * @throws IOException the database is unreachable
	 */
	public UserData userAuthentication(String usersCollectionName, String identity, String password) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String usersUrl = address + "/api/collections/" + usersCollectionName + "/auth-with-password";

		String response = authorize(identity, password, usersUrl);

		JsonObject json = gson.fromJson(response, JsonObject.class);
		JsonObject record = json.getAsJsonObject("record");
		String token = json.get("token").getAsString();

		UserData userData = new UserData();

		// Iterate through the JSON object and set the values of the record
		record.entrySet().forEach(entry -> {
			switch (entry.getKey()) {
				case "id":
					userData.setId(entry.getValue().getAsString());
					break;
				case "collectionId":
					userData.setCollectionId(entry.getValue().getAsString());
					break;
				case "collectionName":
					userData.setCollectionName(entry.getValue().getAsString());
					break;
				case "created":
					userData.setCreated(entry.getValue().getAsString());
					break;
				case "updated":
					userData.setUpdated(entry.getValue().getAsString());
					break;
				case "username":
					userData.setUsername(entry.getValue().getAsString());
					break;
				case "email":
					userData.setEmail(entry.getValue().getAsString());
					break;
				case "emailVisibility":
					userData.setEmailVisibility(entry.getValue().getAsBoolean());
					break;
				case "verified":
					userData.setVerified(entry.getValue().getAsBoolean());
					break;
				default:
					userData.getValues().put(entry.getKey(), entry.getValue().getAsString());
					break;
			}
		});

		return userData;
	}

	/**
	 * Authenticate an admin.
	 *
	 * @param identity the identity (email)
	 * @param password the password
	 * @return the json of the response
	 * @throws IOException the database is unreachable
	 */
	public AdminData adminAuthentication(String identity, String password) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String adminsUrl = address + "/api/admins/auth-with-password";

		String response = authorize(identity, password, adminsUrl);

		JsonObject json = gson.fromJson(response, JsonObject.class);
		JsonObject admin = json.getAsJsonObject("admin");
		String token = json.get("token").getAsString();

		return new AdminData(
				admin.get("id").getAsString(),
				admin.get("created").getAsString(),
				admin.get("updated").getAsString(),
				admin.get("avatar").getAsInt(),
				admin.get("email").getAsString(),
				token
		);
	}


}
