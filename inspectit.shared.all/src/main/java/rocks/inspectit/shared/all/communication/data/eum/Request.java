/**
 *
 */
package rocks.inspectit.shared.all.communication.data.eum;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Represents a request. This is either an ajax request, a page load request or a resource load
 * request.
 * 
 * @author David Monschein
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = AjaxRequest.class), @Type(value = PageLoadRequest.class), @Type(value = ResourceLoadRequest.class) })
public abstract class Request {

	/**
	 * Invocation sequence id. -> Not implemented yet.
	 */
	private long invocationSeqId;

	/**
	 * The URL for this request.
	 */
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

	/**
	 * Gets the type of this request.
	 *
	 * @return type of this request.
	 */
	public abstract RequestType getRequestType();
}
