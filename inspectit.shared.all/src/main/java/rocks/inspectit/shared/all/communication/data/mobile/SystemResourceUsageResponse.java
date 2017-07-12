package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
@InfluxCompatibleAnnotation(measurement = "resourceusage")
public class SystemResourceUsageResponse extends MobileDefaultData {

	@InfluxCompatibleAnnotation(key = "cpu", tag = false)
	private float cpuUsage;

	// in kB
	@InfluxCompatibleAnnotation(key = "memory", tag = false)
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
