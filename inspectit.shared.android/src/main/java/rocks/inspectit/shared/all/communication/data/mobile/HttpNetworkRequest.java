package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
public class HttpNetworkRequest extends AbstractMobileSpanDetails {
	private String url;

	private String method;

	private long duration;

	private int responseCode;

	private String contentType;

	/**
	 * Gets {@link #url}.
	 *
	 * @return {@link #url}
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets {@link #url}.
	 *
	 * @param url
	 *            New value for {@link #url}
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * Gets {@link #duration}.
	 *
	 * @return {@link #duration}
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Sets {@link #duration}.
	 *
	 * @param duration
	 *            New value for {@link #duration}
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * Gets {@link #responseCode}.
	 *
	 * @return {@link #responseCode}
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * Sets {@link #responseCode}.
	 *
	 * @param responseCode
	 *            New value for {@link #responseCode}
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Gets {@link #contentType}.
	 *
	 * @return {@link #contentType}
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets {@link #contentType}.
	 *
	 * @param contentType
	 *            New value for {@link #contentType}
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExternalCall() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.HTTP;
	}

}
