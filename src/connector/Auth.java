package connector;

public class Auth {
	private String id;
	private String created;
	private String updated;
	private String email;
	private String token;

	public Auth(String id, String created, String updated, String email, String token) {
		this.id = id;
		this.created = created;
		this.updated = updated;
		this.email = email;
		this.token = token;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
