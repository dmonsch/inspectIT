package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
public class HttpNetworkRequest extends AbstractMobileSpanDetails {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -6955687075035464630L;

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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.contentType == null) ? 0 : this.contentType.hashCode());
		result = (prime * result) + (int) (this.duration ^ (this.duration >>> 32));
		result = (prime * result) + ((this.method == null) ? 0 : this.method.hashCode());
		result = (prime * result) + this.responseCode;
		result = (prime * result) + ((this.url == null) ? 0 : this.url.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		HttpNetworkRequest other = (HttpNetworkRequest) obj;
		if (this.contentType == null) {
			if (other.contentType != null) {
				return false;
			}
		} else if (!this.contentType.equals(other.contentType)) {
			return false;
		}
		if (this.duration != other.duration) {
			return false;
		}
		if (this.method == null) {
			if (other.method != null) {
				return false;
			}
		} else if (!this.method.equals(other.method)) {
			return false;
		}
		if (this.responseCode != other.responseCode) {
			return false;
		}
		if (this.url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!this.url.equals(other.url)) {
			return false;
		}
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
