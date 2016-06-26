/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

/**
 * @author David Monschein
 *
 */
abstract public class UserAction {

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

}
