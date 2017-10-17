/***************************************************************************
 * Copyright (C) 2016 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package rocks.inspectit.shared.android.mobile;

import io.opentracing.Span;

/**
 * Monitoring record class for network requests.
 *
 * @author David Monschein
 *
 */
public class NetRequestResponse extends MobileDefaultData {
	/**
	 * URL which has been accessed.
	 */
	private String url;
	/**
	 * Method which was used.
	 */
	private String method;
	/**
	 * Duration of the request.
	 */
	private long duration;
	/**
	 * Response code of the request.
	 */
	private int responseCode;

	private String contentType;

	private Span ctxSpan;

	/**
	 * Creates an empty net request response with default values.
	 */
	public NetRequestResponse() {
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(final String method) {
		this.method = method;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(final long duration) {
		this.duration = duration;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(final int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Gets {@link #connectionType}.
	 *
	 * @return {@link #connectionType}
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets {@link #connectionType}.
	 *
	 * @param connectionType
	 *            New value for {@link #connectionType}
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets {@link #ctxSpan}.
	 *
	 * @return {@link #ctxSpan}
	 */
	public Span getContextSpan() {
		return ctxSpan;
	}

	/**
	 * Sets {@link #ctxSpan}.
	 *
	 * @param ctxSpan
	 *            New value for {@link #ctxSpan}
	 */
	public void setContextSpan(Span ctxSpan) {
		this.ctxSpan = ctxSpan;
	}
}
