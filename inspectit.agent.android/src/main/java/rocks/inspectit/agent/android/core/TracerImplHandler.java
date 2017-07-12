package rocks.inspectit.agent.android.core;

import io.opentracing.SpanContext;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sdk.opentracing.util.SystemTimer;

/**
 * @author David Monschein
 *
 */
public class TracerImplHandler {

	private CoreSpanReporter reporter;

	private TracerImpl tracer;

	public TracerImplHandler() {
		reporter = new CoreSpanReporter();

		tracer = new TracerImpl(new SystemTimer(), reporter, true);
	}

	public SpanImpl buildSpan(String name) {
		return tracer.buildSpan(name).start();
	}

	public SpanImpl buildSpanWithParent(String name, SpanContext parent) {
		return tracer.buildSpan(name).asChildOf(parent).start();
	}

	public SpanContext getCurrentContext() {
		return tracer.getCurrentContext();
	}

}
