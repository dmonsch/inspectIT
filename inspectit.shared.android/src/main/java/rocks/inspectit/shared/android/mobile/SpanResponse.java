package rocks.inspectit.shared.android.mobile;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.opentracing.Span;

/**
 * @author David Monschein
 *
 */
public class SpanResponse extends MobileDefaultData {

	@JsonProperty
	private Span span;

	public SpanResponse(Span impl) {
		span = impl;
	}

	/**
	 * Gets {@link #span}.
	 *
	 * @return {@link #span}
	 */
	public Span getSpan() {
		return span;
	}

	/**
	 * Sets {@link #span}.
	 *
	 * @param span
	 *            New value for {@link #span}
	 */
	public void setSpan(Span span) {
		this.span = span;
	}

}
