/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

/**
 * @author David Monschein
 *
 */
public class ResourceLoadRequest extends Request {

	private String name;
	private long startTime;
	private long endTime;
	private String initiatorType;

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets {@link #name}.
	 *
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Sets {@link #startTime}.
	 *
	 * @param startTime
	 *            New value for {@link #startTime}
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets {@link #endTime}.
	 *
	 * @return {@link #endTime}
	 */
	public long getEndTime() {
		return this.endTime;
	}

	/**
	 * Sets {@link #endTime}.
	 *
	 * @param endTime
	 *            New value for {@link #endTime}
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets {@link #initiatorType}.
	 *
	 * @return {@link #initiatorType}
	 */
	public String getInitiatorType() {
		return this.initiatorType;
	}

	/**
	 * Sets {@link #initiatorType}.
	 *
	 * @param initiatorType
	 *            New value for {@link #initiatorType}
	 */
	public void setInitiatorType(String initiatorType) {
		this.initiatorType = initiatorType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestType getRequestType() {
		return RequestType.RESOURCELOAD;
	}

}
