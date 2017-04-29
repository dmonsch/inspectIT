package rocks.inspectit.agent.android.broadcast;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Concrete broadcast receiver for network events.
 * 
 * @author David Monschein
 *
 */
public class NetworkBroadcastReceiver extends AbstractBroadcastReceiver {
	/**
	 * The action types which this receiver can process.
	 */
	private static final String[] ACTIONS = new String[] { "android.net.conn.CONNECTIVITY_CHANGE",
			"android.net.wifi.WIFI_STATE_CHANGED", };

	/**
	 * The id of the device.
	 */
	private String deviceId;

	/**
	 * Default instance creation.
	 */
	public NetworkBroadcastReceiver() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (deviceId == null) {
			deviceId = androidDataCollector.getDeviceId();
		}

		final NetworkInfo currentNetwork = androidDataCollector.getNetworkInfo(true); // force
		// reload
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFilterActions() {
		return ACTIONS;
	}

	/**
	 * Gets the name of the protocol from a sub-type id which is derived from
	 * the mobile network connection.
	 * 
	 * @param subtype
	 *            the sub-type id from a mobile network
	 * @return name of the protocol as string
	 */
	private String getProtocolName(final int subtype) {
		switch (subtype) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xrtt"; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "cdma"; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "edge"; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "evdo0"; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "evdoa"; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "gprs"; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "hsdpa"; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "hspa"; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "hsupa"; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "umts"; // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
			return "ehrpd"; // ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
			return "evdob"; // ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
			return "hspap"; // ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
			return "iden"; // ~25 kbps
		case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
			return "lte"; // ~ 10+ Mbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "unknown";
		default:
			return "unknown";
		}
	}
}
