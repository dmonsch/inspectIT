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

}