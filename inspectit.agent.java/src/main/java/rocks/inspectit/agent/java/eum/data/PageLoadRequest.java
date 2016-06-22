/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

/**
 * @author David Monschein
 *
 */
public class PageLoadRequest extends Request {
	private long _navigationStart;
	private long _unloadEventStart;
	private long _unloadEventEnd;
	private long _redirectStart;
	private long _redirectEnd;
	private long _fetchStart;
	private long _domainLookupStart;
	private long _domainLookupEnd;
	private long _connectStart;
	private long _connectEnd;
	private long _secureConnectionStart;
	private long _requestStart;
	private long _responseStart;
	private long _responseEnd;
	private long _domLoading;
	private long _domInteractive;
	private long _domContentLoadedEventStart;
	private long _domContentLoadedEventEnd;
	private long _domComplete;
	private long _loadEventStart;
	private long _loadEventEnd;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPageLoad() {
		return false;
	}

	/**
	 * Gets {@link #_navigationStart}.
	 *
	 * @return {@link #_navigationStart}
	 */
	public long getNavigationStart() {
		return this._navigationStart;
	}

	/**
	 * Sets {@link #_navigationStart}.
	 *
	 * @param _navigationStart
	 *            New value for {@link #_navigationStart}
	 */
	public void setNavigationStart(long _navigationStart) {
		this._navigationStart = _navigationStart;
	}

	/**
	 * Gets {@link #_unloadEventStart}.
	 *
	 * @return {@link #_unloadEventStart}
	 */
	public long getUnloadEventStart() {
		return this._unloadEventStart;
	}

	/**
	 * Sets {@link #_unloadEventStart}.
	 *
	 * @param unloadEventStart
	 *            New value for {@link #_unloadEventStart}
	 */
	public void setUnloadEventStart(long unloadEventStart) {
		this._unloadEventStart = unloadEventStart;
	}

	/**
	 * Gets {@link #_unloadEventEnd}.
	 *
	 * @return {@link #_unloadEventEnd}
	 */
	public long getUnloadEventEnd() {
		return this._unloadEventEnd;
	}

	/**
	 * Sets {@link #_unloadEventEnd}.
	 *
	 * @param unloadEventEnd
	 *            New value for {@link #_unloadEventEnd}
	 */
	public void setUnloadEventEnd(long unloadEventEnd) {
		this._unloadEventEnd = unloadEventEnd;
	}

	/**
	 * Gets {@link #_redirectStart}.
	 *
	 * @return {@link #_redirectStart}
	 */
	public long getRedirectStart() {
		return this._redirectStart;
	}

	/**
	 * Sets {@link #_redirectStart}.
	 *
	 * @param redirectStart
	 *            New value for {@link #_redirectStart}
	 */
	public void setRedirectStart(long redirectStart) {
		this._redirectStart = redirectStart;
	}

	/**
	 * Gets {@link #_redirectEnd}.
	 *
	 * @return {@link #_redirectEnd}
	 */
	public long getRedirectEnd() {
		return this._redirectEnd;
	}

	/**
	 * Sets {@link #_redirectEnd}.
	 *
	 * @param redirectEnd
	 *            New value for {@link #_redirectEnd}
	 */
	public void setRedirectEnd(long redirectEnd) {
		this._redirectEnd = redirectEnd;
	}

	/**
	 * Gets {@link #_fetchStart}.
	 *
	 * @return {@link #_fetchStart}
	 */
	public long getFetchStart() {
		return this._fetchStart;
	}

	/**
	 * Sets {@link #_fetchStart}.
	 *
	 * @param fetchStart
	 *            New value for {@link #_fetchStart}
	 */
	public void setFetchStart(long fetchStart) {
		this._fetchStart = fetchStart;
	}

	/**
	 * Gets {@link #_domainLookupStart}.
	 *
	 * @return {@link #_domainLookupStart}
	 */
	public long getDomainLookupStart() {
		return this._domainLookupStart;
	}

	/**
	 * Sets {@link #_domainLookupStart}.
	 *
	 * @param domainLookupStart
	 *            New value for {@link #_domainLookupStart}
	 */
	public void setDomainLookupStart(long domainLookupStart) {
		this._domainLookupStart = domainLookupStart;
	}

	/**
	 * Gets {@link #_domainLookupEnd}.
	 *
	 * @return {@link #_domainLookupEnd}
	 */
	public long getDomainLookupEnd() {
		return this._domainLookupEnd;
	}

	/**
	 * Sets {@link #_domainLookupEnd}.
	 *
	 * @param domainLookupEnd
	 *            New value for {@link #_domainLookupEnd}
	 */
	public void setDomainLookupEnd(long domainLookupEnd) {
		this._domainLookupEnd = domainLookupEnd;
	}

	/**
	 * Gets {@link #_connectStart}.
	 *
	 * @return {@link #_connectStart}
	 */
	public long getConnectStart() {
		return this._connectStart;
	}

	/**
	 * Sets {@link #_connectStart}.
	 *
	 * @param connectStart
	 *            New value for {@link #_connectStart}
	 */
	public void setConnectStart(long connectStart) {
		this._connectStart = connectStart;
	}

	/**
	 * Gets {@link #_connectEnd}.
	 *
	 * @return {@link #_connectEnd}
	 */
	public long getConnectEnd() {
		return this._connectEnd;
	}

	/**
	 * Sets {@link #_connectEnd}.
	 *
	 * @param connectEnd
	 *            New value for {@link #_connectEnd}
	 */
	public void setConnectEnd(long connectEnd) {
		this._connectEnd = connectEnd;
	}

	/**
	 * Gets {@link #_secureConnectionStart}.
	 *
	 * @return {@link #_secureConnectionStart}
	 */
	public long getSecureConnectionStart() {
		return this._secureConnectionStart;
	}

	/**
	 * Sets {@link #_secureConnectionStart}.
	 *
	 * @param secureConnectionStart
	 *            New value for {@link #_secureConnectionStart}
	 */
	public void setSecureConnectionStart(long secureConnectionStart) {
		this._secureConnectionStart = secureConnectionStart;
	}

	/**
	 * Gets {@link #_requestStart}.
	 *
	 * @return {@link #_requestStart}
	 */
	public long getRequestStart() {
		return this._requestStart;
	}

	/**
	 * Sets {@link #_requestStart}.
	 *
	 * @param requestStart
	 *            New value for {@link #_requestStart}
	 */
	public void setRequestStart(long requestStart) {
		this._requestStart = requestStart;
	}

	/**
	 * Gets {@link #_responseStart}.
	 *
	 * @return {@link #_responseStart}
	 */
	public long getResponseStart() {
		return this._responseStart;
	}

	/**
	 * Sets {@link #_responseStart}.
	 *
	 * @param responseStart
	 *            New value for {@link #_responseStart}
	 */
	public void setResponseStart(long responseStart) {
		this._responseStart = responseStart;
	}

	/**
	 * Gets {@link #_responseEnd}.
	 *
	 * @return {@link #_responseEnd}
	 */
	public long getResponseEnd() {
		return this._responseEnd;
	}

	/**
	 * Sets {@link #_responseEnd}.
	 *
	 * @param responseEnd
	 *            New value for {@link #_responseEnd}
	 */
	public void setResponseEnd(long responseEnd) {
		this._responseEnd = responseEnd;
	}

	/**
	 * Gets {@link #_domLoading}.
	 *
	 * @return {@link #_domLoading}
	 */
	public long getDomLoading() {
		return this._domLoading;
	}

	/**
	 * Sets {@link #_domLoading}.
	 *
	 * @param domLoading
	 *            New value for {@link #_domLoading}
	 */
	public void setDomLoading(long domLoading) {
		this._domLoading = domLoading;
	}

	/**
	 * Gets {@link #_domInteractive}.
	 *
	 * @return {@link #_domInteractive}
	 */
	public long getDomInteractive() {
		return this._domInteractive;
	}

	/**
	 * Sets {@link #_domInteractive}.
	 *
	 * @param domInteractive
	 *            New value for {@link #_domInteractive}
	 */
	public void setDomInteractive(long domInteractive) {
		this._domInteractive = domInteractive;
	}

	/**
	 * Gets {@link #_domContentLoadedEventStart}.
	 *
	 * @return {@link #_domContentLoadedEventStart}
	 */
	public long getDomContentLoadedEventStart() {
		return this._domContentLoadedEventStart;
	}

	/**
	 * Sets {@link #_domContentLoadedEventStart}.
	 *
	 * @param domContentLoadedEventStart
	 *            New value for {@link #_domContentLoadedEventStart}
	 */
	public void setDomContentLoadedEventStart(long domContentLoadedEventStart) {
		this._domContentLoadedEventStart = domContentLoadedEventStart;
	}

	/**
	 * Gets {@link #_domContentLoadedEventEnd}.
	 *
	 * @return {@link #_domContentLoadedEventEnd}
	 */
	public long getDomContentLoadedEventEnd() {
		return this._domContentLoadedEventEnd;
	}

	/**
	 * Sets {@link #_domContentLoadedEventEnd}.
	 *
	 * @param domContentLoadedEventEnd
	 *            New value for {@link #_domContentLoadedEventEnd}
	 */
	public void setDomContentLoadedEventEnd(long domContentLoadedEventEnd) {
		this._domContentLoadedEventEnd = domContentLoadedEventEnd;
	}

	/**
	 * Gets {@link #_domComplete}.
	 *
	 * @return {@link #_domComplete}
	 */
	public long getDomComplete() {
		return this._domComplete;
	}

	/**
	 * Sets {@link #_domComplete}.
	 *
	 * @param domComplete
	 *            New value for {@link #_domComplete}
	 */
	public void setDomComplete(long domComplete) {
		this._domComplete = domComplete;
	}

	/**
	 * Gets {@link #_loadEventStart}.
	 *
	 * @return {@link #_loadEventStart}
	 */
	public long getLoadEventStart() {
		return this._loadEventStart;
	}

	/**
	 * Sets {@link #_loadEventStart}.
	 *
	 * @param loadEventStart
	 *            New value for {@link #_loadEventStart}
	 */
	public void setLoadEventStart(long loadEventStart) {
		this._loadEventStart = loadEventStart;
	}

	/**
	 * Gets {@link #_loadEventEnd}.
	 *
	 * @return {@link #_loadEventEnd}
	 */
	public long getLoadEventEnd() {
		return this._loadEventEnd;
	}

	/**
	 * Sets {@link #_loadEventEnd}.
	 *
	 * @param loadEventEnd
	 *            New value for {@link #_loadEventEnd}
	 */
	public void setLoadEventEnd(long loadEventEnd) {
		this._loadEventEnd = loadEventEnd;
	}
}
