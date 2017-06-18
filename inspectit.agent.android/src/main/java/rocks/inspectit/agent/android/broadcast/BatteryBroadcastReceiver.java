package rocks.inspectit.agent.android.broadcast;

import android.content.Context;
import android.content.Intent;

/**
 * @author David Monschein
 *
 */
public class BatteryBroadcastReceiver extends AbstractBroadcastReceiver {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFilterActions() {
		return new String[] { "android.intent.action.BATTERY_CHANGED", "android.intent.action.ACTION_POWER_CONNECTED", "android.intent.action.ACTION_POWER_DISCONNECTED" };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
	}

}
