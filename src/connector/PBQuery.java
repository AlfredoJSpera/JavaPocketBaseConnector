package connector;

public class PBQuery {

	private final int page;
	private final int perPage;
	private final int skipTotal;
	private String sort = "sort=";
	private String filter = "filter=";
	private String expand = "expand=";

	/**
	 * Sets page to 1, perPage to 500 and skipTotal to 1.
	 * Instead of using <code>'</code> use <code>"</code> for strings.
	 *
	 * @param sort   sort by field, leave to null if not needed
	 * @param filter filter by field, leave to null if not needed
	 * @param expand expand field, leave to null if not needed
	 */
	public PBQuery(String sort, String filter, String expand) {
		this.page = 1;
		this.perPage = 500;
		this.skipTotal = 1;

		this.sort += sort;
		this.filter += filter;
		this.expand += expand;
	}

	/**
	 * Sets everything.
	 * Instead of using <code>'</code> use <code>"</code> for strings.
	 * @param page     page number
	 * @param perPage  number of items per page
	 * @param sort     sort by field
	 * @param filter   filter by field
	 * @param expand   expand field
	 */
	public PBQuery(int page, int perPage, String sort, String filter, String expand) {
		this.page = page;
		this.perPage = perPage;

		this.skipTotal = 0;

		this.sort = sort;
		this.filter = filter;
		this.expand = expand;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("page=").append(page)
				.append("&perPage=").append(perPage);

		if (skipTotal == 1)
				result.append("&skipTotal=").append(skipTotal);

		result.append("&").append(sort)
				.append("&").append(filter)
				.append("&").append(expand);

		return result.toString();
	}
}
