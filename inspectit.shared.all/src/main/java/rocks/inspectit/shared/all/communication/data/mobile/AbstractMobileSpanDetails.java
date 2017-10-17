package rocks.inspectit.shared.all.communication.data.mobile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author David Monschein
 *
 */
public abstract class AbstractMobileSpanDetails extends DefaultData {

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
