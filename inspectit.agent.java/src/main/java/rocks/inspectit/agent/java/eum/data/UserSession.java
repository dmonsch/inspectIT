/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author David Monschein
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSession {

	private String browser;
	private String device;
	private String language;
	private String sessionId;

	private List<UserAction> userActions;

	public UserSession() {
	}

	public UserSession(String browser, String device, String lang, String id) {
		this.browser = browser;
		this.device = device;
		this.language = lang;
		this.sessionId = id;

		this.userActions = new ArrayList<UserAction>();
	}

	/**
	 * Gets {@link #browser}.
	 *
	 * @return {@link #browser}
	 */
	public String getBrowser() {
		return browser;
	}

	/**
	 * Sets {@link #browser}.
	 *
	 * @param browser
	 *            New value for {@link #browser}
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}

	/**
	 * Gets {@link #device}.
	 *
	 * @return {@link #device}
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * Sets {@link #device}.
	 *
	 * @param device
	 *            New value for {@link #device}
	 */
	public void setDevice(String device) {
		this.device = device;
	}

	/**
	 * Gets {@link #language}.
	 *
	 * @return {@link #language}
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets {@link #language}.
	 *
	 * @param language
	 *            New value for {@link #language}
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Gets {@link #sessionId}.
	 *
	 * @return {@link #sessionId}
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets {@link #sessionId}.
	 *
	 * @param sessionId
	 *            New value for {@link #sessionId}
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Gets {@link #userActions}.
	 *
	 * @return {@link #userActions}
	 */
	public List<UserAction> getUserActions() {
		return userActions;
	}

	/**
	 * Sets {@link #userActions}.
	 *
	 * @param userActions
	 *            New value for {@link #userActions}
	 */
	public void setUserActions(List<UserAction> userActions) {
		this.userActions = userActions;
	}

	public void addUserAction(UserAction action) {
		this.userActions.add(action);
	}

}
