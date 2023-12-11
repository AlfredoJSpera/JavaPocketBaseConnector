package connector;

public class AdminData {
	private String id;
	private String created;
	private String updated;
	private int avatar;
	private String email;
	private String token;

	public AdminData(String id, String created, String updated, int avatar, String email, String token) {
		this.id = id;
		this.created = created;
		this.updated = updated;
		this.avatar = avatar;
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

	public int getAvatar() {
		return avatar;
	}

	public void setAvatar(int avatar) {
		this.avatar = avatar;
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

	@Override
	public String toString() {
		return "AdminData{" +
				"id='" + id + '\'' +
				", created='" + created + '\'' +
				", updated='" + updated + '\'' +
				", avatar=" + avatar +
				", email='" + email + '\'' +
				", token='" + token + '\'' +
				'}';
	}
}
