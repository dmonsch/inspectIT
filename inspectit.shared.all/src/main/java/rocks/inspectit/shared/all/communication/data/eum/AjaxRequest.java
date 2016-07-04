/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

/**
 * @author David Monschein
 *
 */
public class AjaxRequest extends Request {
	private long startTime;
	private long endTime;
	private int status;
	private String method;
	private String baseUrl;
	/**
	 * Gets {@link #startTime}.
	 *
	 * @return {@link #startTime}
	 */
	public long getStartTime() {
		return startTime;
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
		return endTime;
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
	 * Gets {@link #method}.
	 *
	 * @return {@link #method}
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * Sets {@link #method}.
	 *
	 * @param method
	 *            New value for {@link #method}
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequestType getRequestType() {
		return RequestType.AJAX;
	}
	/**
	 * Gets {@link #status}.
	 *
	 * @return {@link #status}
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * Sets {@link #status}.
	 *
	 * @param status
	 *            New value for {@link #status}
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	/**
	 * Gets {@link #baseUrl}.
	 *   
	 * @return {@link #baseUrl}  
	 */ 
	public String getBaseUrl() {
		return baseUrl;
	}
	/**  
	 * Sets {@link #baseUrl}.  
	 *   
	 * @param baseUrl  
	 *            New value for {@link #baseUrl}  
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
