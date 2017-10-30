package rocks.inspectit.shared.all.communication.data.mobile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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

	/**
	 * @return true, if this call left the mobile phone
	 */
	public abstract boolean isExternalCall();

	/**
	 * @return the propagation type of this span
	 */
	public abstract PropagationType getPropagationType();

}
