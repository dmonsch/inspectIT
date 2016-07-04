/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Monschein
 *
 */
public class PageLoadAction extends UserAction {
	private PageLoadRequest pageLoadRequest;
	private List<Request> requests;

	public PageLoadAction() {
		this.requests = new ArrayList<Request>();
	}

	/**
	 * Gets {@link #pageLoadRequest}.
	 *
	 * @return {@link #pageLoadRequest}
	 */
	public PageLoadRequest getPageLoadRequest() {
		return pageLoadRequest;
	}

	/**
	 * Sets {@link #pageLoadRequest}.
	 *
	 * @param pageLoadRequest
	 *            New value for {@link #pageLoadRequest}
	 */
	public void setPageLoadRequest(PageLoadRequest pageLoadRequest) {
		this.pageLoadRequest = pageLoadRequest;
	}

	/**
	 * Gets {@link #requests}.
	 *
	 * @return {@link #requests}
	 */
	public List<Request> getRequests() {
		return requests;
	}

	/**
	 * Sets {@link #requests}.
	 *
	 * @param requests
	 *            New value for {@link #requests}
	 */
	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	public void addRequest(Request r) {
		this.requests.add(r);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Request> getChildRequests() {
		List<Request> copy = new ArrayList<Request>(this.requests);
		copy.add(pageLoadRequest);
		return copy;
	}
}
