package rocks.inspectit.shared.all.communication.data.mobile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
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
