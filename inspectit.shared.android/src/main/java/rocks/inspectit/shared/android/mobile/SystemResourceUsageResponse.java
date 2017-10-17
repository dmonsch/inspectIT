package rocks.inspectit.shared.android.mobile;

/**
 * @author David Monschein
 *
 */
public class SystemResourceUsageResponse extends MobileDefaultData {

	private float cpuUsage;

	// in kB
	private float memoryUsage;

	/**
	 * Gets {@link #cpuUsage}.
	 *
	 * @return {@link #cpuUsage}
	 */
	public float getCpuUsage() {
		return this.cpuUsage;
	}

	/**
	 * Sets {@link #cpuUsage}.
	 *
	 * @param cpuUsage
	 *            New value for {@link #cpuUsage}
	 */
	public void setCpuUsage(float cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	/**
	 * Gets {@link #memoryUsage}.
	 *
	 * @return {@link #memoryUsage}
	 */
	public float getMemoryUsage() {
		return this.memoryUsage;
	}

	/**
	 * Sets {@link #memoryUsage}.
	 *
	 * @param memoryUsage
	 *            New value for {@link #memoryUsage}
	 */
	public void setMemoryUsage(float memoryUsage) {
		this.memoryUsage = memoryUsage;
	}

}
