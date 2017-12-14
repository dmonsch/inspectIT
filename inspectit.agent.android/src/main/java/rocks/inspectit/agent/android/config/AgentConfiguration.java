package rocks.inspectit.agent.android.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds the configuration of the agent and is JSON compatible so it can be stored in files easily.
 *
 * @author David Monschein
 *
 */
public class AgentConfiguration {
	/**
	 * Current configuration instance.
	 */
	public static AgentConfiguration current = null;

	/**
	 * The REST URL which is used to establish a session.
	 */
	@JsonProperty
	private String sessionUrl;

	/**
	 * The REST URL which is used to send data.
	 */
	@JsonProperty
	private String beaconUrl;

	/**
	 * The log tag which is used by the Android Agent.
	 */
	@JsonProperty
	private String logTag;

	/**
	 * Whether the location should be collected or not.
	 */
	@JsonProperty
	private boolean collectLocation;

	@JsonProperty
	private boolean collectBatteryConsumption;

	@JsonProperty
	private boolean shutdownOnDestroy;

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

	/**
	 * Gets {@link #collectLocation}.
	 *
	 * @return {@link #collectLocation}
	 */
	public boolean isCollectLocation() {
		return collectLocation;
	}

	/**
	 * Sets {@link #collectLocation}.
	 *
	 * @param collectLocation
	 *            New value for {@link #collectLocation}
	 */
	public void setCollectLocation(boolean collectLocation) {
		this.collectLocation = collectLocation;
	}

	/**
	 * Gets {@link #collectBatteryConsumption}.
	 *
	 * @return {@link #collectBatteryConsumption}
	 */
	public boolean isCollectBatteryConsumption() {
		return collectBatteryConsumption;
	}

	/**
	 * Sets {@link #collectBatteryConsumption}.
	 *
	 * @param collectBatteryConsumption
	 *            New value for {@link #collectBatteryConsumption}
	 */
	public void setCollectBatteryConsumption(boolean collectBatteryConsumption) {
		this.collectBatteryConsumption = collectBatteryConsumption;
	}

	/**
	 * Gets {@link #shutdownOnDestroy}.
	 *
	 * @return {@link #shutdownOnDestroy}
	 */
	public boolean isShutdownOnDestroy() {
		return shutdownOnDestroy;
	}

	/**
	 * Sets {@link #shutdownOnDestroy}.
	 *
	 * @param shutdownOnDestroy
	 *            New value for {@link #shutdownOnDestroy}
	 */
	public void setShutdownOnDestroy(boolean shutdownOnDestroy) {
		this.shutdownOnDestroy = shutdownOnDestroy;
	}
}
