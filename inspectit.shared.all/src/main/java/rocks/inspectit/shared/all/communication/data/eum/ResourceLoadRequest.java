/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

/**
 * Request which contains informations about a single resource load. (e.g. CSS file)
 *
 * @author David Monschein
 */
public class ResourceLoadRequest extends Request {

	/**
	 * Start time of the resource load request.
	 */
	private long startTime;

	/**
	 * End time of the resource load request.
	 */
	private long endTime;

	/**
	 * Determines from what the resource loading got triggered.
	 */
	private String initiatorType;

	/**
	 * The size in octets of the resource.
	 */
	private long transferSize;

	/**
	 * The URL which triggered the resource load.
	 */
	private String initiatorUrl;

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

	/**
	 * Gets {@link #transferSize}.
	 *
	 * @return {@link #transferSize}
	 */
	public long getTransferSize() {
		return transferSize;
	}

	/**
	 * Sets {@link #transferSize}.
	 *
	 * @param transferSize
	 *            New value for {@link #transferSize}
	 */
	public void setTransferSize(long transferSize) {
		this.transferSize = transferSize;
	}

	/**
	 * Gets {@link #initiatorUrl}.
	 *
	 * @return {@link #initiatorUrl}
	 */
	public String getInitiatorUrl() {
		return initiatorUrl;
	}

	/**
	 * Sets {@link #initiatorUrl}.
	 *
	 * @param initiatorUrl
	 *            New value for {@link #initiatorUrl}
	 */
	public void setInitiatorUrl(String initiatorUrl) {
		this.initiatorUrl = initiatorUrl;
	}

}