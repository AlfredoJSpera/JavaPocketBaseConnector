package connector;

import java.util.ArrayList;
import java.util.List;

public class CollectionPage {
	private String page;
	private String perPage;
	private String totalPages;
	private String totalItems;
	private List<Record> items = new ArrayList<>();

	public CollectionPage() {
	}

	public CollectionPage(String page, String perPage, String totalPages, String totalItems) {
		this.page = page;
		this.perPage = perPage;
		this.totalPages = totalPages;
		this.totalItems = totalItems;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getPerPage() {
		return perPage;
	}

	public void setPerPage(String perPage) {
		this.perPage = perPage;
	}

	public String getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(String totalPages) {
		this.totalPages = totalPages;
	}

	public String getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(String totalItems) {
		this.totalItems = totalItems;
	}

	public List<Record> getItems() {
		return items;
	}

	public void setItems(List<Record> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "CollectionPage{" +
				"page='" + page + '\'' +
				", perPage='" + perPage + '\'' +
				", totalPages='" + totalPages + '\'' +
				", totalItems='" + totalItems + '\'' +
				", items=" + items +
				'}';
	}
}
