package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * Class representing an initial request response from the server for
 * establishing a session.
 * 
 * @author David Monschein
 *
 */
public class SessionCreationResponse {
	/**
	 * The session id.
	 */
	private String sessionId;

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

}
