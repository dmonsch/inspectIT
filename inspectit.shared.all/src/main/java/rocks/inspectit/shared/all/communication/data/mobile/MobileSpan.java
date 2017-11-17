package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.opentracing.References;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * Special span type for all traces collected on a mobile device.
 *
 * @author David Monschein
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileSpan extends AbstractSpan {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 8027959082036217638L;

	/**
	 * Stores further information about this trace element. One-to-One relationship.
	 */
	private AbstractMobileSpanDetails details;

	/**
	 * Session ID of the device.
	 */
	private long sessionId;

	/**
	 * {@inheritDoc}
	 */
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
		mobileDetails.setOwningSpan(this);

		if (!isRoot()) {
			setReferenceType(References.FOLLOWS_FROM);
		} else {
			setReferenceType(References.CHILD_OF);
		}

		this.setPropagationType(mobileDetails.getPropagationType());
	}

	/**
	 * Collects the tags for a span instance.
	 */
	public void collectChildTags() {
		Map<String, String> allTags = new HashMap<String, String>();
		details.collectTags(allTags);
		this.addAllTags(allTags);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.details == null) ? 0 : this.details.hashCode());
		result = (prime * result) + (int) (this.sessionId ^ (this.sessionId >>> 32));
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
		MobileSpan other = (MobileSpan) obj;
		if (this.details == null) {
			if (other.details != null) {
				return false;
			}
		} else if (!this.details.equals(other.details)) {
			return false;
		}
		if (this.sessionId != other.sessionId) {
			return false;
		}
		return true;
	}

}
