package connector;

public class PBQuery {

	private final int page;
	private final int perPage;
	private final int skipTotal;
	private final String sort;
	private final String filter;
	private final String expand;

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

		if (sort != null)
			this.sort = "sort=" +  sort;
		else
			this.sort = null;

		if (filter != null)
			this.filter = "filter=" + filter;
		else
			this.filter = null;

		if (expand != null)
			this.expand = "expand=" + expand;
		else
			this.expand = null;
	}

	/**
	 * Used only for pagination.
	 * @param page    current page number
	 * @param perPage number of items per page
	 */
	public PBQuery(int page, int perPage) {
		this.page = page;
		this.perPage = perPage;

		this.skipTotal = 0;

		this.sort = null;
		this.filter = null;
		this.expand = null;
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
	public PBQuery(int page, int perPage, int skipTotal, String sort, String filter, String expand) {
		this.page = page;
		this.perPage = perPage;

		this.skipTotal = skipTotal;

		this.sort = "sort=" +  sort;
		this.filter = "filter=" + filter;
		this.expand = "expand=" + expand;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("page=").append(page)
				.append("&perPage=").append(perPage);
		if (skipTotal == 1)
			result.append("&skipTotal=").append(skipTotal);
		if (sort != null)
			result.append("&").append(sort);
		if (filter != null)
			result.append("&").append(filter);
		if (expand != null)
			result.append("&").append(expand);

		return result.toString();
	}
}
