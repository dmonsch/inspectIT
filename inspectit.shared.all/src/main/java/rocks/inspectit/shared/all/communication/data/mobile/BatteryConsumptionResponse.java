package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
public class BatteryConsumptionResponse extends MobileDefaultData {

	private float consumptionPercent;
	private long timeInterval;

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
