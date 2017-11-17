package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * Monitoring record holding information about the resource usage of the monitored mobile
 * application.
 *
 * @author David Monschein
 *
 */
public class SystemResourceUsage extends MobileDefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -5781998757005571236L;

	/**
	 * The CPU usage in percent (0.0 - 1.0).
	 */
	private float cpuUsage;

	/**
	 * The memory(RAM) usage in kB.
	 */
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
