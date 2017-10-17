package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.Map;

import rocks.inspectit.shared.android.mobile.SessionCreationRequest;

/**
 * @author David Monschein
 *
 */
public class SessionCreation extends MobileDefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -7916188901232481459L;

	/**
	 * The name of the mobile application.
	 */
	private String appName;

	/**
	 * The device id.
	 */
	private String deviceId;

	private Map<String, String> additionalInformation;

	/**
	 * Creates a new instance.
	 */
	public SessionCreation(SessionCreationRequest req) {
		super(req);

		additionalInformation = req.getAdditionalInformation();
		appName = req.getAppName();
		deviceId = req.getDeviceId();
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

}
