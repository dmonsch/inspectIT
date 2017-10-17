package rocks.inspectit.shared.all.communication.data.mobile;

import rocks.inspectit.shared.android.mobile.BatteryConsumptionResponse;

/**
 * @author David Monschein
 *
 */
public class BatteryConsumption extends MobileDefaultData {

	private float consumptionPercent;

	private long timeInterval;

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 2725738546375825464L;

	/**
	 * @param analog
	 */
	public BatteryConsumption(BatteryConsumptionResponse analog) {
		super(analog);

		this.setConsumptionPercent(analog.getConsumptionPercent());
		this.setTimeInterval(analog.getTimeInterval());
	}

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
