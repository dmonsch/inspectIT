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
	 * Serial UID.
	 */
	private static final long serialVersionUID = 8027959082036217638L;

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
