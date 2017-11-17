package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
public class ActivityStart extends MobileDefaultData {

	private String activityName;

	private String activityClass;

	private long startTimestamp;

	/**
	 * Gets {@link #activityName}.
	 *
	 * @return {@link #activityName}
	 */
	public String getActivityName() {
		return activityName;
	}

	/**
	 * Sets {@link #activityName}.
	 *
	 * @param activityName
	 *            New value for {@link #activityName}
	 */
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	/**
	 * Gets {@link #activityClass}.
	 *
	 * @return {@link #activityClass}
	 */
	public String getActivityClass() {
		return activityClass;
	}

	/**
	 * Sets {@link #activityClass}.
	 *
	 * @param activityClass
	 *            New value for {@link #activityClass}
	 */
	public void setActivityClass(String activityClass) {
		this.activityClass = activityClass;
	}

	/**
	 * Gets {@link #startTimestamp}.
	 *
	 * @return {@link #startTimestamp}
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * Sets {@link #startTimestamp}.
	 *
	 * @param startTimestamp
	 *            New value for {@link #startTimestamp}
	 */
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

}
