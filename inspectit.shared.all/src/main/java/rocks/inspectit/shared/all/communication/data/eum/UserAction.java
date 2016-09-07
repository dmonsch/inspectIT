/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

import java.util.List;

/**
 * @author David Monschein
 *
 */
public abstract class UserAction {

	private UserSession userSession;
	private String baseUrl;

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

	public abstract List<Request> getChildRequests();

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
