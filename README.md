# JavaPocketBaseConnector
This is a Java connector for [PocketBase](https://pocketbase.io) that lets you use your database in Java.

It is still a work in progress.

---
## Todo
### Important
- [x] Change how the `update` method takes in the record id 
- [x] Test the update of files
- [ ] Test the `expand` query parameter in `PBQuery`
- [ ] Add support for `expand` in `readOneRecord`
- [ ] Create a jar file
- [ ] Figure out how to create a github release and add the jar
- [ ] Add support for multiple files in a single field

### Other
- [ ] OAuth2
- [ ] Realtime

---
## Usage
***TODO: Add jar instructions***

After downloading both the jar and [pocketbase.exe](https://github.com/pocketbase/pocketbase/releases), start the server with `pocketbase.exe serve` and then you can use the connector in your Java project.
### List/Search
**Fetch a paginated records list.**
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");
PBCollection collection = pb.readAllRecords("collection_name");
List<PBRecord> recordList = collection.getItems();
```
#### Query Parameters
You can use the `PBQuery` class to sort, filter and expand the list records. 
The query syntax is the same as the one used in the [PocketBase official API](https://pocketbase.io/docs/api-rules-and-filters/).

Query parameters can be set in the constructor and can be `null` if not needed.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBCollection collection = pb.readAllRecords("collection_name",
		new PBQuery(
		    "-views", // sort
		    "views > 60 && title = \"My Post\"", // filter
		    null // expand
		)
);

List<PBRecord> recordList = collection.getItems();
```

`PBQuery` can also be used to set the current page and the page size for pagination.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBCollection collection = pb.readAllRecords("collection_name",
		new PBQuery(
		    1, // page
		    3  // perPage 
		)
);

List<PBRecord> recordList = collection.getItems();
```

### View one
**Fetch a single record.**

The non-system generated fields of the record can be accessed with the `getValues` method.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBRecord record = pb.readOneRecord("collection_name", "record_id");

Map<String, Object> values = record.getValues();
```

### Create
**Create a new record.**

To create a new record you can use the `createRecord` method.

The value of the fields can be put inside a `Map<String, Object>`.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

Map<String, Object> values = new HashMap<>();
values.put("field1", "Hello World");
values.put("field2", "...");

PBRecord record = pb.createRecord(collection_name, values);
```

#### Create a record with files
To create a record with files you can use the `createRecordWithFiles` method.

It uses `multipart/form-data` instead of `application/json`.

You can put the value of the fields and the files in two different maps.

*I'm still working on the support for multiple files in a single field.*

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

Map<String, Object> values = new HashMap<>();
values.put("field1", "Hello World");
values.put("field2", "...");

Map<String, File> files = new HashMap<>();
files.put("file_field", new File("path/to/file"));

PBRecord record = pb.createRecordWithFiles("collection_name", values, files);
```

### Update
**Update a single record.**

You can modify the map obtained from the record.

_This must be changed._

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("collection_name", "record_id");

// Update the record
Map<String, Object> values = record.getValues();
values.put("content", "New Content");

record = pb.updateRecord("posts", record);
```

### Delete
**Delete a single record.**
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

pb.deleteRecord("collection_name", "record_id");
```

## Files
### Upload a file
*To change*
Use the `createRecordWithFiles` to create a new record with the file or use the `updateRecord` method and put the file in the `files` map.

### Delete a file
To delete a file, use the `updateRecord` method and set the value of the file field to `null`.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBRecord record = pb.readOneRecord("collection_name", "record_name");

Map<String, Object> values = record.getValues();
values.put("file_field", null);

pb.updateRecord("collection_name", "record_name", values);
```

### Download a file
To download a file, use the `downloadFile` method.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

String fileName = pb.readOneRecord("collection_name", "record_name").getValues().get("file_field").toString();

pb.downloadFile("collection_name", "record_name", fileName, "path/to/put/file.txt");
```

## Authentication
You can authenticate as an admin or a regular user. In both cases, you'll get an object with all the data of the authentication.

The most important field is the `token` that you can use to authenticate yourself when using some methods.

### Authenticate as admin
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

AdminData adminData = pb.adminAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
String adminToken = adminData.getToken();
```

### Authenticate as app user
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

UserData userData = pb.userAuthentication("users_collection_name", EMAIL, PASSWORD);
String userToken = userData.getToken();
```

### Using the token
Almost all the methods have an optional `token` parameter that you can use to authenticate yourself.
This is necessary if the collection can be accessed only by certain users specified in the PocketBase admin console.

If the user does not have access to the collection, an exception will be thrown.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

AdminData adminData = pb.adminAuthentication(ADMIN_EMAIL, ADMIN_PASSWORD);
String adminToken = adminData.getToken();

// Example of a method that uses the token
pb.deleteRecord("collection_name", "record_id", adminToken);
```
