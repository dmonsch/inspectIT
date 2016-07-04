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

}
