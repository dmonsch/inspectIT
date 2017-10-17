package rocks.inspectit.shared.all.communication.data.mobile;

import io.opentracing.References;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;

/**
 * @author David Monschein
 *
 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	 * @param eumDetails
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

}
