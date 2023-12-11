package connector;

import java.util.HashMap;
import java.util.Map;

public class Record {
	private String id;
	private String collectionId;
	private String collectionName;
	private String created;
	private String updated;
	private Map<String, Object> values = new HashMap<>();

	public Record() {
	}

	public Record(Map<String, Object> values) {
		this.values = values;
	}

	public Record(String id, String collectionId, String collectionName, String created, String updated) {
		this.id = id;
		this.collectionId = collectionId;
		this.collectionName = collectionName;
		this.created = created;
		this.updated = updated;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		return "Record{" +
				"id='" + id + '\'' +
				", collectionId='" + collectionId + '\'' +
				", collectionName='" + collectionName + '\'' +
				", created='" + created + '\'' +
				", updated='" + updated + '\'' +
				", values=" + values +
				'}';
	}
}
