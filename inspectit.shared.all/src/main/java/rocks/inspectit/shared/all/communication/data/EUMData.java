package rocks.inspectit.shared.all.communication.data;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSession;

/**
 * Class for communicating eum data between agent and CMR.
 *
 * @author David Monschein
 *
 */
public class EUMData extends DefaultData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6706272659481517962L;

	private UserSession userSession;

	private List<PageLoadRequest> pageLoadRequests;

	private List<ResourceLoadRequest> resourceLoadRequests;

	private List<AjaxRequest> ajaxRequests;

	private String baseUrl;

	public EUMData() {
		this.pageLoadRequests = new ArrayList<PageLoadRequest>();
		this.resourceLoadRequests = new ArrayList<ResourceLoadRequest>();
		this.ajaxRequests = new ArrayList<AjaxRequest>();
	}

	/**
	 * Gets {@link #pageLoadRequests}.
	 *
	 * @return {@link #pageLoadRequests}
	 */
	public List<PageLoadRequest> getPageLoadRequests() {
		return pageLoadRequests;
	}

	/**
	 * Gets {@link #resourceLoadRequests}.
	 *
	 * @return {@link #resourceLoadRequests}
	 */
	public List<ResourceLoadRequest> getResourceLoadRequests() {
		return resourceLoadRequests;
	}

	/**
	 * Gets {@link #ajaxRequests}.
	 *
	 * @return {@link #ajaxRequests}
	 */
	public List<AjaxRequest> getAjaxRequests() {
		return ajaxRequests;
	}

	public void addPageLoadRequest(PageLoadRequest plReq) {
		this.pageLoadRequests.add(plReq);
	}

	public void addResourceLoadRequest(ResourceLoadRequest rlReq) {
		this.resourceLoadRequests.add(rlReq);
	}

	public void addAjaxRequest(AjaxRequest ajReq) {
		this.ajaxRequests.add(ajReq);
	}

	/**
	 * Gets {@link #userSession}.
	 *
	 * @return {@link #userSession}
	 */
	public UserSession getUserSession() {
		return userSession;
	}

	/**
	 * Sets {@link #userSession}.
	 *
	 * @param userSession
	 *            New value for {@link #userSession}
	 */
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	/**
	 * Gets {@link #baseUrl}.
	 *
	 * @return {@link #baseUrl}
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Sets {@link #baseUrl}.
	 *
	 * @param baseUrl
	 *            New value for {@link #baseUrl}
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other instanceof EUMData) {
			EUMData compare = (EUMData) other;
			return baseUrl.equals(compare.baseUrl) && userSession.equals(compare.userSession) && listEqual(compare.ajaxRequests, ajaxRequests) && listEqual(compare.pageLoadRequests, pageLoadRequests)
					&& listEqual(compare.resourceLoadRequests, resourceLoadRequests);
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.baseUrl == null) ? 0 : this.baseUrl.hashCode());
		result = (prime * result) + (this.userSession.hashCode() ^ (this.userSession.hashCode() >>> 32));
		for (AjaxRequest ar : ajaxRequests) {
			result = (prime * result) + ((ar == null) ? 0 : ar.hashCode());
		}
		for (ResourceLoadRequest rq : resourceLoadRequests) {
			result = (prime * result) + ((rq == null) ? 0 : rq.hashCode());
		}
		for (PageLoadRequest plr : pageLoadRequests) {
			result = (prime * result) + ((plr == null) ? 0 : plr.hashCode());
		}
		return result;
	}

	/**
	 * Determines whether two lists are equal with the order doesn't matter.
	 *
	 * @param a
	 *            first list
	 * @param b
	 *            second list
	 * @return true if the lists contain the same items, false otherwise
	 */
	private boolean listEqual(List<?> a, List<?> b) {
		return a.containsAll(b) && b.containsAll(a);
	}

}
