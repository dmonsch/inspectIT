package rocks.inspectit.agent.android.config;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author David Monschein
 *
 */
public class AgentConfiguration {

	public static AgentConfiguration current = null;

	@JsonProperty
	private String sessionUrl;

	@JsonProperty
	private String beaconUrl;

	@JsonProperty
	private String logTag;

	/**
	 * Gets {@link #sessionUrl}.
	 *
	 * @return {@link #sessionUrl}
	 */
	public String getSessionUrl() {
		return this.sessionUrl;
	}

	/**
	 * Sets {@link #sessionUrl}.
	 *
	 * @param sessionUrl
	 *            New value for {@link #sessionUrl}
	 */
	public void setSessionUrl(String sessionUrl) {
		this.sessionUrl = sessionUrl;
	}

	/**
	 * Gets {@link #beaconUrl}.
	 *
	 * @return {@link #beaconUrl}
	 */
	public String getBeaconUrl() {
		return this.beaconUrl;
	}

	/**
	 * Sets {@link #beaconUrl}.
	 *
	 * @param beaconUrl
	 *            New value for {@link #beaconUrl}
	 */
	public void setBeaconUrl(String beaconUrl) {
		this.beaconUrl = beaconUrl;
	}

	/**
	 * Gets {@link #logTag}.
	 *
	 * @return {@link #logTag}
	 */
	public String getLogTag() {
		return logTag;
	}

	/**
	 * Sets {@link #logTag}.
	 *
	 * @param logTag
	 *            New value for {@link #logTag}
	 */
	public void setLogTag(String logTag) {
		this.logTag = logTag;
	}
}
