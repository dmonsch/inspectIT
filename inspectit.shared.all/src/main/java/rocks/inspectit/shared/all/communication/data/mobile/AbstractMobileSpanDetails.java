package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Details which provide additional information about a mobile span.
 *
 * @author David Monschein
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = HttpNetworkRequest.class),
	@JsonSubTypes.Type(value = MobileFunctionExecution.class)})
public abstract class AbstractMobileSpanDetails extends MobileDefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -2982950096466928703L;

	/**
	 * The span the details belong.to.
	 */
	@JsonIgnore
	private transient MobileSpan owner;

	/**
	 * @return true, if this call left the mobile phone
	 */
	public abstract boolean isExternalCall();

	/**
	 * @return the propagation type of this span
	 */
	public abstract PropagationType getPropagationType();

	/**
	 * Invoked by the owning span to collect all information in form of tags.
	 *
	 * @param tags
	 *            the map to add the tags to.
	 */
	public abstract void collectTags(Map<String, String> tags);

	/**
	 * Gets {@link #owner}.
	 *
	 * @return {@link #owner}
	 */
	@JsonIgnore
	public MobileSpan getOwningSpan() {
		return owner;
	}

	/**
	 * Sets {@link #owner}.
	 *
	 * @param owner
	 *            New value for {@link #owner}
	 */
	@JsonIgnore
	public void setOwningSpan(MobileSpan owner) {
		this.owner = owner;
	}

}
