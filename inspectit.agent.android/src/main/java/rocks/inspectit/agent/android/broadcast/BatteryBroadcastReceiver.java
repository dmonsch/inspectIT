package rocks.inspectit.agent.android.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import rocks.inspectit.shared.all.communication.data.mobile.BatteryConsumption;

/**
 * Broadcast receiver which receives information about the current battery and charging state.
 *
 * @author David Monschein
 *
 */
public class BatteryBroadcastReceiver extends AbstractBroadcastReceiver {

	/**
	 * Starting timestamp (from {@link System#currentTimeMillis()}) for the current measurement.
	 */
	private long startTimestamp;

	/**
	 * The battery percentage at the start of the measurement (0.0 - 1.0).
	 */
	private float startPct;

	/**
	 * If the mobile device is in a charging state.
	 */
	private boolean charging;

	/**
	 * If the application is running currently.
	 */
	private boolean running;

	/**
	 * Creates a new broadcast receiver for battery events.
	 */
	public BatteryBroadcastReceiver() {
		charging = false;
		running = true;
	}

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
		// Are we charging / charged?
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);

		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float) scale;

		if (!isCharging) {
			if (!charging && running) {
				// we have a valid data point
				long endTimestamp = System.currentTimeMillis();
				float consumption = batteryPct - startPct;

				long timeInterval = endTimestamp - startTimestamp;

				BatteryConsumption bcrp = new BatteryConsumption();
				bcrp.setConsumptionPercent(consumption);
				bcrp.setTimeInterval(timeInterval);

				this.pushData(bcrp);
			}
		}

		// set new values
		startTimestamp = System.currentTimeMillis();
		startPct = batteryPct;
		charging = isCharging;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStart(Object thisObject) {
		onReceive(null, this.androidDataCollector.getBatteryIntent());
		running = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStop(Object thisObject) {
		onReceive(null, this.androidDataCollector.getBatteryIntent());
		running = false;
	}

}
