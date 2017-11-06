package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Monschein
 *
 */
public class SessionCreation extends MobileDefaultData {
	/**
	 * The name of the mobile application.
	 */
	private String appName;

	/**
	 * The device id.
	 */
	private String deviceId;

	private Map<String, String> additionalInformation;

	public SessionCreation() {
		this.additionalInformation = new HashMap<String, String>();
	}

	/**
	 * Gets {@link #appName}.
	 *
	 * @return {@link #appName}
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Gets {@link #deviceId}.
	 *
	 * @return {@link #deviceId}
	 */
	public String getDeviceId() {
		return deviceId;
	}
	/**
	 * Gets {@link #additionalInformation}.
	 *
	 * @return {@link #additionalInformation}
	 */
	public Map<String, String> getAdditionalInformation() {
		return additionalInformation;
	}

	/**
	 * Sets {@link #appName}.
	 *
	 * @param appName
	 *            New value for {@link #appName}
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * Sets {@link #deviceId}.
	 *
	 * @param deviceId
	 *            New value for {@link #deviceId}
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Sets {@link #additionalInformation}.
	 *
	 * @param additionalInformation
	 *            New value for {@link #additionalInformation}
	 */
	public void setAdditionalInformation(Map<String, String> additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	/**
	 * Puts an entry to the additional information map.
	 *
	 * @param first
	 *            the key of the information
	 * @param second
	 *            the value of the information
	 */
	public void putAdditionalInformation(String first, String second) {
		this.additionalInformation.put(first, second);
	}

}
