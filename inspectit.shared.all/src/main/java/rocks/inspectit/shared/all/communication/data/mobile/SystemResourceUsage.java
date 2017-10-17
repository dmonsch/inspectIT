package rocks.inspectit.shared.all.communication.data.mobile;

import rocks.inspectit.shared.android.mobile.SystemResourceUsageResponse;

/**
 * @author David Monschein
 *
 */
public class SystemResourceUsage extends MobileDefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -5781998757005571236L;

	private float cpuUsage;

	private float memoryUsage;

	public SystemResourceUsage(SystemResourceUsageResponse resp) {
		super(resp);

		this.cpuUsage = resp.getCpuUsage();
		this.memoryUsage = resp.getMemoryUsage();
	}

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
