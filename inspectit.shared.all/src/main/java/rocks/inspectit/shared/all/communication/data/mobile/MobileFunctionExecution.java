package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Holds information about a function execution on a mobile device within a monitored application.
 *
 * @author David Monschein
 *
 */
public class MobileFunctionExecution extends AbstractMobileSpanDetails {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 795929811165409499L;

	/**
	 * The signature of the function which has been executed.
	 */
	@JsonProperty
	private String methodSignature;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void collectTags(Map<String, String> tags) {
		if (!methodSignature.isEmpty()) {
			tags.put(ExtraTags.OPERATION_NAME, methodSignature);
		}
	}

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

}
