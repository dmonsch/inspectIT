package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * Monitoring record which holds information about the memory consumption of the application.
 *
 * @author David Monschein
 *
 */
public class BatteryConsumption extends MobileDefaultData {

	/**
	 * The percentage of battery which has been consumed by the application (0.0 - 1.0).
	 */
	private float consumptionPercent;

	/**
	 * The amount of milliseconds(ms) which belongs to the
	 * {@link BatteryConsumption#consumptionPercent}.
	 */
	private long timeInterval;

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 2725738546375825464L;

	/**
	 * Gets {@link #consumptionPercent}.
	 *
	 * @return {@link #consumptionPercent}
	 */
	public float getConsumptionPercent() {
		return consumptionPercent;
	}

	/**
	 * Sets {@link #consumptionPercent}.
	 *
	 * @param consumptionPercent
	 *            New value for {@link #consumptionPercent}
	 */
	public void setConsumptionPercent(float consumptionPercent) {
		this.consumptionPercent = consumptionPercent;
	}

	/**
	 * Gets {@link #timeInterval}.
	 *
	 * @return {@link #timeInterval}
	 */
	public long getTimeInterval() {
		return timeInterval;
	}

	/**
	 * Sets {@link #timeInterval}.
	 *
	 * @param timeInterval
	 *            New value for {@link #timeInterval}
	 */
	public void setTimeInterval(long timeInterval) {
		this.timeInterval = timeInterval;
	}

}
