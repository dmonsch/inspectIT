package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
public class SystemResourceUsage extends MobileDefaultData {
	private float cpuUsage;

	private float memoryUsage;

	/**
	 * Gets {@link #cpuUsage}.
	 *
	 * @return {@link #cpuUsage}
	 */
	public float getCpuUsage() {
		return cpuUsage;
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
		return memoryUsage;
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
