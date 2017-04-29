package rocks.inspectit.agent.android.core;

/**
 * The data in this class gets set external by code inserted into the original
 * application.
 * 
 * @author David Monschein
 */
public final class ExternalConfiguration {

	/**
	 * Log tag for consistent log outputs of the agent.
	 */
	private static final String LOG_TAG = "Android Agent";

	/**
	 * URL for sending monitoring beacons.
	 */
	private static String beaconUrl;
	/**
	 * URL for sending initial hello request to create a session.
	 */
	private static String helloUrl;

	/**
	 * New instance creation not allowed.
	 */
	private ExternalConfiguration() {
	}

	/**
	 * @return the beaconUrl
	 */
	public static String getBeaconUrl() {
		return beaconUrl;
	}

	/**
	 * @param beaconUrl
	 *            the beaconUrl to set
	 */
	public static void setBeaconUrl(final String beaconUrl) {
		ExternalConfiguration.beaconUrl = beaconUrl;
	}

	/**
	 * @return the helloUrl
	 */
	public static String getHelloUrl() {
		return helloUrl;
	}

	/**
	 * @param helloUrl
	 *            the helloUrl to set
	 */
	public static void setHelloUrl(final String helloUrl) {
		ExternalConfiguration.helloUrl = helloUrl;
	}

	/**
	 * @return the logTag
	 */
	public static String getLogTag() {
		return LOG_TAG;
	}
}
