package rocks.inspectit.shared.all.communication.data.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author David Monschein
 *
 */
public class MobileFunctionExecution extends AbstractMobileSpanDetails {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 795929811165409499L;

	@JsonProperty
	private String methodSignature;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExternalCall() {
		return false;
	}

	/**
	 * Gets {@link #methodSignature}.
	 *
	 * @return {@link #methodSignature}
	 */
	public String getMethodSignature() {
		return methodSignature;
	}

	/**
	 * Sets {@link #methodSignature}.
	 *
	 * @param methodSignature
	 *            New value for {@link #methodSignature}
	 */
	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.methodSignature == null) ? 0 : this.methodSignature.hashCode());
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
		MobileFunctionExecution other = (MobileFunctionExecution) obj;
		if (this.methodSignature == null) {
			if (other.methodSignature != null) {
				return false;
			}
		} else if (!this.methodSignature.equals(other.methodSignature)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.MOBILE;
	}

}
