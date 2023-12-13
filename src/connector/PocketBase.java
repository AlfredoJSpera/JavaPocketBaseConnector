package connector;

import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class with all the methods to operate on <a href="https://pocketbase.io/">PocketBase</a> collections and records.
 */
public class PocketBase {
	private final String address;
	private final Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(PBValues.class, new PBValues.PBValuesTypeAdapter())
			.serializeNulls()
			.create();

	/**
	 * Instantiates a new PocketBase connection.
	 *
	 * @param address the string address of the database
	 */
	public PocketBase(String address) {
		this.address = address;
	}

	// ==================== COMMON METHODS ====================

	public String getAddress() {
		return address;
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
	 * Handles and returns the response of the HTTP request.
	 *
	 * @param requestBuilder the request builder
	 * @return the json string response of the HTTP request
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
	 * Handles the errors that happened inside the HTTP request.
	 *
	 * @param body the json string of the response
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

			// Find the error causes and put them in a list
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

			// Exception with specific error causes
			throw new PocketBaseException(errorCode, errorMessage, infos);
		} else {
			// For general errors
			throw new PocketBaseException(errorCode, errorMessage);
		}
	}

	/**
	 * Authenticates a user or admin.
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
	 * Builds a record from a JSON object.
	 *
	 * @param object the JSON object
	 * @return the record built
	 */
	private PBRecord buildRecord(JsonObject object) {
		PBRecord record = new PBRecord();

		System.out.println("object = " + object);
		// Iterate through the JSON object and set the values of the record
		object.entrySet().forEach(entry -> {
			switch (entry.getKey()) {
				// Fixed fields created by the PocketBase system
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
				// Record fields (PBValues)
				default:
					List<String> stringList = null;

					// If the field is a multi-value, then it is also an array
					// even if there is only one file. Convert the JsonArray
					// to a List of Strings
					if (entry.getValue().toString().charAt(0) == '[') {
						stringList = new ArrayList<>();
						for (JsonElement element : entry.getValue().getAsJsonArray()) {
							stringList.add(element.getAsString());
						}
					}

					if (stringList != null && stringList.isEmpty())
						// Empty array
						record.getValues().put(entry.getKey(), null);
					else if (stringList != null) {
						// Array with elements
						record.getValues().put(entry.getKey(), new PBValues().setStringList(stringList));
					} else
						// Single value
						record.getValues().put(entry.getKey(), new PBValues().setString(entry.getValue().getAsString()));
					break;
			}
		});

		return record;
	}

	/**
	 * Encodes a string to be used in a URL.
	 *
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
	 * @param recordValues   the map containing the values to insert
	 * @param authToken      the authorization token
	 * @return the record created
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBRecord createRecord(String collectionName, Map<String, PBValues> recordValues, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records";

		String inputJson = gson.toJson(recordValues);

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(inputJson));

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
	 * Creates a new record inside a collection.
	 *
	 * @param collectionName the collection name
	 * @param recordValues   the map containing the values to insert
	 * @return the record created
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBRecord createRecord(String collectionName, Map<String, PBValues> recordValues) throws IOException, PocketBaseException, InterruptedException {
		return createRecord(collectionName, recordValues, null);
	}

	/**
	 * Creates a new record inside a protected collection with an authorization token and files.<br>
	 * <b>This method uses the multipart/form-data content type.</b>
	 *
	 * @param collectionName the collection name
	 * @param recordValues   the map containing the values to insert
	 * @param files          the map containing the files to insert, where the String is the name of the field in the db
	 * @param authToken      the authorization token
	 * @return the record created
	 * @throws IOException         the database is unreachable
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 */
	public PBRecord createRecordWithFiles(String collectionName, Map<String, PBValues> recordValues, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records";

		// Insert everything in the multipart body
		MultiPartBodyPublisher publisher = new MultiPartBodyPublisher();

		for (Map.Entry<String, PBValues> entry : recordValues.entrySet()) {
			if (PBValues.isStringList(entry.getValue())){
				// STRING LIST
				for (String string : entry.getValue().getStringList()) {
					publisher.addPart(entry.getKey(), string);
				}
			} else if (PBValues.isFileList(entry.getValue())) {
				// FILE LIST
				for (File file : entry.getValue().getFileList()) {
					publisher.addPart(entry.getKey(), file.toPath());
				}
			// TODO: add support for relations
			} else if (PBValues.isString(entry.getValue())){
				// STRING
				publisher.addPart(entry.getKey(), entry.getValue().getString());
			}
		}

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
				.POST(publisher.build());

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
	 * Creates a new record inside a collection with files.<br>
	 * <b>This method uses the multipart/form-data content type.</b>
	 *
	 * @param collectionName the collection name
	 * @param recordValues   the map containing the values to insert
	 * @return the record created
	 * @throws IOException         the database is unreachable
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 */
	public PBRecord createRecordWithFiles(String collectionName, Map<String, PBValues> recordValues) throws IOException, PocketBaseException, InterruptedException {
		return createRecordWithFiles(collectionName, recordValues, null);
	}

	/**
	 * Gets all the records from a protected collection with the authorization token and query options.
	 *
	 * @param collectionName the collection name
	 * @param authToken      the authorization token
	 * @param queryOptions   the options for the query of the records
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBCollection readAllRecords(String collectionName, String authToken, PBQuery queryOptions) throws IOException, PocketBaseException, InterruptedException {
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
		PBCollection collectionPage = new PBCollection(
				jsonObject.get("page").getAsString(),
				jsonObject.get("perPage").getAsString(),
				jsonObject.get("totalPages").getAsString(),
				jsonObject.get("totalItems").getAsString()
		);

		// Put items in the collection page
		JsonArray items = jsonObject.getAsJsonArray("items");
		items.forEach(item -> {
			JsonObject itemObject = item.getAsJsonObject();
			PBRecord record = buildRecord(itemObject);
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
	public PBCollection readAllRecords(String collectionName, String authToken) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, authToken, null);
	}

	/**
	 * Gets all the records from a collection with query options.
	 *
	 * @param collectionName the collection name
	 * @param queryOptions   the options for the query of the records
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBCollection readAllRecords(String collectionName, PBQuery queryOptions) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, null, queryOptions);
	}

	/**
	 * Gets all the records from a collection.
	 *
	 * @param collectionName the collection name
	 * @return a page with the records and info about the page
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBCollection readAllRecords(String collectionName) throws IOException, PocketBaseException, InterruptedException {
		return readAllRecords(collectionName, null, null);
	}

	/**
	 * Gets one record from a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @param authToken      the authorization token
	 * @return the record found
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBRecord readOneRecord(String collectionName, String recordId, String authToken) throws IOException, PocketBaseException, InterruptedException {
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

		// Send the request and get the response json
		String response = handleResponse(requestBuilder);
		return buildRecord(gson.fromJson(response, JsonObject.class));
	}

	/**
	 * Gets one record from a collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @return the record found
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBRecord readOneRecord(String collectionName, String recordId) throws IOException, PocketBaseException, InterruptedException {
		return readOneRecord(collectionName, recordId, null);
	}

	/**
	 * Updates an existing record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param updatedValues  the updated values of the record
	 * @param authToken      the authorization token
	 * @return the updated record
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBRecord updateRecord(String collectionName, String recordId, Map<String, PBValues> updatedValues, String authToken) throws IOException, PocketBaseException, InterruptedException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records/" + recordId;

		String inputJson = gson.toJson(updatedValues);

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.method("PATCH", HttpRequest.BodyPublishers.ofString(inputJson));

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder.header("Authorization", authToken);
		}

		String response = handleResponse(requestBuilder);
		return buildRecord(gson.fromJson(response, JsonObject.class));
	}

	/**
	 * Updates an existing record inside a collection.
	 *
	 * @param collectionName the collection name
	 * @param updatedValues  the updated values of the record
	 * @return the updated record
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 * @throws IOException         the database is unreachable
	 */
	public PBRecord updateRecord(String collectionName, String recordId, Map<String, PBValues> updatedValues) throws IOException, PocketBaseException, InterruptedException {
		return updateRecord(collectionName, recordId, updatedValues, null);
	}

	public PBRecord updateRecordWithFiles(String collectionName, String recordId, Map<String, PBValues> updatedValues , String authToken) {

	}

	public PBRecord updateRecordWithFiles(String collectionName, String recordId, Map<String, PBValues> updatedValues) {
		return updateRecordWithFiles(collectionName, recordId, updatedValues, null);
	}

	/**
	 * Deletes an existing record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record to delete
	 * @param authToken      the authorization token
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
				.DELETE();

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder
					.header("Content-Type", "application/json")
					.header("Authorization", authToken);
		}

		return handleResponse(requestBuilder).equals("204");
	}

	/**
	 * Deletes an existing record inside a collection.
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
	 * Authenticates a regular user.
	 *
	 * @param usersCollectionName the collection name
	 * @param identity            the identity (email)
	 * @param password            the password
	 * @return the user data
	 * @throws IOException the database is unreachable
	 */
	public UserData userAuthentication(String usersCollectionName, String identity, String password) throws IOException, PocketBaseException, InterruptedException {
		String usersUrl = address + "/api/collections/" + usersCollectionName + "/auth-with-password";

		String response = authorize(identity, password, usersUrl);

		JsonObject json = gson.fromJson(response, JsonObject.class);
		JsonObject record = json.getAsJsonObject("record");
		String token = json.get("token").getAsString();

		UserData userData = new UserData();

		// Iterate through the JSON object and set the values of the record
		record.entrySet().forEach(entry -> {
			switch (entry.getKey()) {
				// Fixed fields created by the PocketBase system
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
	 * Authenticates an admin.
	 *
	 * @param identity the identity (email)
	 * @param password the password
	 * @return the json of the response
	 * @throws IOException the database is unreachable
	 */
	public AdminData adminAuthentication(String identity, String password) throws IOException, PocketBaseException, InterruptedException {
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

	// ==================== FILE HANDLING ====================

	/**
	 * Downloads a file to the local machine from a record inside a protected collection with an authorization token.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @param fileName       the name of the file
	 * @param savePath       the path where to save the file
	 * @param thumb          optional thumbnail parameter of the file given the size, leave null if not needed
	 * @param authToken      the authorization token
	 * @return the downloaded file
	 * @throws IOException         the database is unreachable
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 */
	public File downloadFile(String collectionName, String recordId, String fileName, String savePath, String thumb, String authToken) throws IOException, PocketBaseException, InterruptedException {
		String url = address + "/api/files/" + collectionName + "/" + recordId + "/" + fileName;

		if (thumb != null)
			url += "?thumb=" + thumb;

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET();

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder
					.header("Content-Type", "application/json")
					.header("Authorization", authToken);
		}

		HttpResponse<InputStream> response = HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

		try (InputStream in = response.body()) {
			Path outputPath = Path.of(savePath);
			Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);

			return outputPath.toFile();
		}
	}
	/**
	 * Downloads a file to the local machine from a record inside a collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @param fileName       the name of the file
	 * @param savePath       the path where to save the file
	 * @param thumb          optional thumbnail parameter of the file given the size, leave null if not needed
	 * @return the downloaded file
	 * @throws IOException         the database is unreachable
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 */
	public File downloadFile(String collectionName, String recordId, String fileName, String savePath, String thumb) throws IOException, PocketBaseException, InterruptedException {
		return downloadFile(collectionName, recordId, fileName, savePath, thumb, null);
	}

	/**
	 * Downloads a file to the local machine from a record inside a collection.
	 *
	 * @param collectionName the collection name
	 * @param recordId       the id of the record
	 * @param fileName       the name of the file
	 * @param savePath       the path where to save the file
	 * @return the downloaded file
	 * @throws IOException         the database is unreachable
	 * @throws PocketBaseException in case of error throws a message with the details of the error
	 */
	public File downloadFile(String collectionName, String recordId, String fileName, String savePath) throws IOException, PocketBaseException, InterruptedException {
		return downloadFile(collectionName, recordId, fileName, savePath, null, null);
	}

	/*
	public PBRecord updateRecordWithFiles(String collectionName, String recordId, Map<String, PBValues> updatedValues, Map<String, File> files, String authToken) throws IOException, InterruptedException, PocketBaseException {
		// Create the URL
		String url = address + "/api/collections/" + collectionName + "/records/" + recordId;

		// Insert everything in the multipart body
		MultiPartBodyPublisher publisher = new MultiPartBodyPublisher();

		// case 0: no files
		// case 1: one file
		// case 2: more than one file






		// Values
		for (Map.Entry<String, Object> entry : updatedValues.entrySet()) {
			if (entry.getValue() != null)
				publisher.addPart(entry.getKey(), entry.getValue().toString());
		}

		// Files
		for (Map.Entry<String, File> entry : files.entrySet()) {
			publisher.addPart(entry.getKey(), entry.getValue().toPath());
		}

		// Open HTTP connection
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
				.method("PATCH", publisher.build());

		// Add the authorization token if present
		if (authToken != null) {
			requestBuilder = requestBuilder.header("Authorization", authToken);
		}

		// Send the request and get the response json
		String response = handleResponse(requestBuilder);

		JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
		return buildRecord(jsonObject);
	}

	public PBRecord updateRecordWithFiles(String collectionName, String recordId, Map<String, PBValues> updatedValues, Map<String, File> files) throws IOException, InterruptedException, PocketBaseException {
		return updateRecordWithFiles(collectionName, recordId, updatedValues, files, null);

	}

	 */


}
