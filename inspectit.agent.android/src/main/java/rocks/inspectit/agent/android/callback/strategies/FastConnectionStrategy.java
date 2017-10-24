package rocks.inspectit.agent.android.callback.strategies;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import rocks.inspectit.agent.android.core.AndroidDataCollector;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;

/**
 * Checks whether the connection speed and only sends beacons if the connection is fast enough.
 * (Wifi or LTE)
 *
 * @author David Monschein
 *
 */
public class FastConnectionStrategy extends AbstractCallbackStrategy {

	/**
	 * Data collector for getting information about the connection.
	 */
	private AndroidDataCollector dataCollector;

	/**
	 * Creates a new strategy which only transfers data if the device has a wifi or a LTE
	 * connection.
	 */
	public FastConnectionStrategy() {
		dataCollector = DependencyManager.getAndroidDataCollector();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addData(MobileDefaultData dat) {
		this.data.addChildData(dat);
		sendEvaluation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addData(MobileSpan dat) {
		this.data.addChildSpan(dat);
		sendEvaluation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
	}

	private void sendEvaluation() {
		NetworkInfo info = dataCollector.getNetworkInfo();
		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				super.sendBeacon();
			} else if ((info.getType() == ConnectivityManager.TYPE_MOBILE) && (info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE)) {
				super.sendBeacon();
			}
		}
	}

}
