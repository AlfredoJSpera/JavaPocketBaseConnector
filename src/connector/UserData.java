package connector;

import java.util.HashMap;
import java.util.Map;

public class UserData {
	private String id;
	private String created;
	private String updated;
	private String username;
	private String email;
	private boolean emailVisibility;
	private boolean verified;
	private String token;
	private String collectionId;
	private String collectionName;
	private Map<String, Object> values = new HashMap<>();

	public UserData() {
	}

	public UserData(String id, String created, String updated, String username, String email, boolean emailVisibility,
	                boolean verified, String token, String collectionId, String collectionName, Map<String, Object> values) {
		this.id = id;
		this.created = created;
		this.updated = updated;
		this.username = username;
		this.email = email;
		this.emailVisibility = emailVisibility;
		this.verified = verified;
		this.token = token;
		this.collectionId = collectionId;
		this.collectionName = collectionName;
		this.values = values;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isEmailVisibility() {
		return emailVisibility;
	}

	public void setEmailVisibility(boolean emailVisibility) {
		this.emailVisibility = emailVisibility;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
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

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "UserData{" +
				"id='" + id + '\'' +
				", created='" + created + '\'' +
				", updated='" + updated + '\'' +
				", username='" + username + '\'' +
				", email='" + email + '\'' +
				", emailVisibility=" + emailVisibility +
				", verified=" + verified +
				", token='" + token + '\'' +
				", collectionId='" + collectionId + '\'' +
				", collectionName='" + collectionName + '\'' +
				", values=" + values +
				'}';
	}
}
