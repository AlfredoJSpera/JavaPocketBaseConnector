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
Fetch a paginated records list.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");
PBCollection collection = pb.readAllRecords("COLLECTION_NAME");
List<PBRecord> records = collection.getItems();
```
#### Query Parameters
You can use the `PBQuery` class to sort, filter and expand the list records. 
The query syntax is the same as the one used in the [PocketBase official API](https://pocketbase.io/docs/api-rules-and-filters/).

Query parameters can be set in the constructor and can be `null` if not needed.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBCollection collection = pb.readAllRecords("COLLECTION_NAME",
		new PBQuery(
		    "-views", // sort
		    "views > 60 && title = \"My Post\"", // filter
		    null // expand
		)
);

List<PBRecord> records = collection.getItems();
```

`PBQuery` can also be used to set the current page and the page size for pagination.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBCollection collection = pb.readAllRecords("COLLECTION_NAME",
		new PBQuery(
		    1, // page
		    3  // perPage 
		)
);

List<PBRecord> records = collection.getItems();
```

### View one
Fetch a single record.

The non-system generated fields of the record can be accessed with the `getValues` method.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

Map<String, PBValue> values = record.getValues();
```

### Create
Create a new record.

To create a new record you can use the `createRecord` method.

The value of the fields can be put inside a `Map<String, PBValue>`.

*For now, only strings and list of strings can be put in the map*.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");
		
Map<String, PBValue> values = new HashMap<>();
values.put("field1", new PBValue().setString("Value1"));
values.put("field2", new PBValue().setString("Value2"));

PBRecord record = pb.createRecord("COLLECTION_NAME", values);
```

### Update
Update a single record.

You can modify the map obtained from the record.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

// Update the record
Map<String, PBValue> values = record.getValues();
values.put("field1", new PBValue().setString("New Value"));

pb.updateRecord("COLLECTION_NAME", "RECORD_ID", values);
```

### Delete
Delete a single record.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

pb.deleteRecord("COLLECTION_NAME", "RECORD_ID");
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

UserData userData = pb.userAuthentication("users_COLLECTION_NAME", EMAIL, PASSWORD);
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
pb.deleteRecord("COLLECTION_NAME", "RECORD_ID", adminToken);
```

## Files
To handle files you must use the methods that have the `multipart/form-data` content type instead of `application/json`.

### Create a record with files
To create a record with files you can use the `createRecordWithFiles` method.

You can put the value of the fields and the files in the same map.

*Todo: put link to multi-value tutorial.*

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

List<String> files = new ArrayList<>();
files.add("path/to/file.txt");

Map<String, PBValue> values = new HashMap<>();
values.put("field1", new PBValue().setString("Value1"));
values.put("field2", new PBValue().setString("Value2"));
values.put("file_field", new PBValue().setStringList(files));

PBRecord record = pb.createRecordWithFiles("COLLECTION_NAME", values);
```

### Add a file
To add a new file, use the `add(path)` method of `List` and use the `updateRecord` method.

You can also modify the other fields of the record.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

Map<String, PBValue> values = record.getValues();
// Regular fields
values.put("title", new PBValue().setString("New Value"));

List<String> files = values.get("file_field").getList();
files.add("path/to/file.txt");

pb.updateRecordWithFiles(COLLECTION, "RECORD_ID", values);
```

### Delete a file
To delete a file, set to an empty string `""` the file in the string list and use the `updateRecord` method.

You can also modify the other fields of the record.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

Map<String, PBValue> values = record.getValues();
// Regular fields
values.put("title", new PBValue().setString("New Value"));

List<String> files = values.get("file_field").getList();
files.set(INDEX, ""); // Remove the file

pb.updateRecordWithFiles(COLLECTION, "RECORD_ID", values);
```

### Delete all files
To delete all files, clear the list, add an empty string `""` and use the `updateRecord` method.

You can also modify the other fields of the record.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

Map<String, PBValue> values = record.getValues();
// Regular fields
values.put("title", new PBValue().setString("New Value"));

List<String> files = values.get("file_field").getList();
files.clear();
files.add("");

pb.updateRecordWithFiles("COLLECTION_NAME", "RECORD_ID", values);
```

### Replace a file
To replace a file, you can modify the record map by using the `set(index,path)` method of `List`.

Remember to delete the old file before adding the new one.

You can also modify the other fields of the record.

```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

Map<String, PBValue> values = record.getValues();
// Regular fields
values.put("title", new PBValue().setString("New Value"));

List<String> files = values.get("file_field").getList();
files.set(INDEX, ""); // Remove the old file
files.add(INDEX, "path/to/file.txt"); // Add the new file

pb.updateRecordWithFiles("COLLECTION_NAME", record.getId(), values);
```

### Download a file
To download a file on your machine, use the `downloadFile` method.
```java
PocketBase pb = new PocketBase("http://127.0.0.1:8090");

// Get the record
PBRecord record = pb.readOneRecord("COLLECTION_NAME", "RECORD_ID");

Map<String, PBValue> values = record.getValues();

List<String> files = values.get("file_field").getList();
String fileName = files.get(0);

pb.downloadFile("COLLECTION_NAME", "RECORD_ID", fileName, "path/to/put/file.txt");
```

## Multi-value fields
Files, Selects and Relations can be multi-value fields.
Files are always inside a list, while Selects (and Relations *to see*) can be inside a list or a single value.


