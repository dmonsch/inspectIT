package rocks.inspectit.shared.all.communication.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSession;

/**
 * @author David Monschein
 *
 */
@Entity
public class EUMData extends DefaultData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6706272659481517962L;

	@Transient
	private UserSession userSession;

	@Transient
	private List<PageLoadRequest> pageLoadRequests;
	@Transient
	private List<ResourceLoadRequest> resourceLoadRequests;
	@Transient
	private List<AjaxRequest> ajaxRequests;

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

}
