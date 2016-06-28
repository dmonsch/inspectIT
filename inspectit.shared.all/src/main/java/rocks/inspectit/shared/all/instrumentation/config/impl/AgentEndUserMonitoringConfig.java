package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * @author Jonas Kunz
 *
 */
public class AgentEndUserMonitoringConfig {

	private boolean isEnabled;

	private String scriptBaseUrl;

	public AgentEndUserMonitoringConfig() {
		isEnabled = false;
		scriptBaseUrl = "/";
	}

	/**
	 * @param isEnabled
	 * @param scriptBaseUrl
	 */
	public AgentEndUserMonitoringConfig(boolean isEnabled, String scriptBaseUrl) {
		super();
		this.isEnabled = isEnabled;
		this.scriptBaseUrl = scriptBaseUrl;
	}

	/**
	 * Gets {@link #isEnabled}.
	 *
	 * @return {@link #isEnabled}
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Sets {@link #isEnabled}.
	 *
	 * @param isEnabled
	 *            New value for {@link #isEnabled}
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Gets {@link #scriptBaseUrl}.
	 *
	 * @return {@link #scriptBaseUrl}
	 */
	public String getScriptBaseUrl() {
		return scriptBaseUrl;
	}

	/**
	 * Sets {@link #scriptBaseUrl}.
	 *
	 * @param scriptBaseUrl
	 *            New value for {@link #scriptBaseUrl}
	 */
	public void setScriptBaseUrl(String scriptBaseUrl) {
		this.scriptBaseUrl = scriptBaseUrl;
	}

}