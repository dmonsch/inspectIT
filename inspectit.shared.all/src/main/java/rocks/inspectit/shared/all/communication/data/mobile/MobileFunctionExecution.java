package rocks.inspectit.shared.all.communication.data.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;

import rocks.inspectit.shared.all.tracing.data.PropagationType;

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
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return PropagationType.MOBILE;
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

}
