/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author David Monschein
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = AjaxRequest.class), @Type(value = PageLoadRequest.class), @Type(value = ResourceLoadRequest.class) })
abstract public class Request {
	private long invocationSeqId;
	private String url;

	/**
	 * Gets {@link #invocationSeqId}.
	 *
	 * @return {@link #invocationSeqId}
	 */
	public long getInvocationSeqId() {
		return invocationSeqId;
	}

	/**
	 * Sets {@link #invocationSeqId}.
	 *
	 * @param invocationSeqId
	 *            New value for {@link #invocationSeqId}
	 */
	public void setInvocationSeqId(long invocationSeqId) {
		this.invocationSeqId = invocationSeqId;
	}

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

	abstract public boolean isPageLoad();
}
