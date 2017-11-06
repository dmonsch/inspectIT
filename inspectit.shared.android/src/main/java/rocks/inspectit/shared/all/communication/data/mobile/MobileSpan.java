package rocks.inspectit.shared.all.communication.data.mobile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.opentracing.References;

/**
 * @author David Monschein
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileSpan extends AbstractSpan {
	/**
	 * Stores further information about this trace element. One-to-One relationship.
	 */
	private AbstractMobileSpanDetails details;

	private long sessionId;

	@Override
	@JsonIgnore
	public boolean isCaller() {
		return details.isExternalCall();
	}

	/**
	 * Gets {@link #details}.
	 *
	 * @return {@link #details}
	 */
	public AbstractMobileSpanDetails getDetails() {
		return details;
	}

	/**
	 * Gets {@link #sessionId}.
	 *
	 * @return {@link #sessionId}
	 */
	public long getSessionId() {
		return sessionId;
	}

	/**
	 * Sets teh details object. Should only be called once!
	 *
	 * @param mobileDetails
	 *            the details.
	 */
	public void setDetails(AbstractMobileSpanDetails mobileDetails) {
		this.details = mobileDetails;

		if (!isRoot()) {
			setReferenceType(References.FOLLOWS_FROM);
		} else {
			setReferenceType(References.CHILD_OF);
		}

		this.setPropagationType(mobileDetails.getPropagationType());
	}

}
