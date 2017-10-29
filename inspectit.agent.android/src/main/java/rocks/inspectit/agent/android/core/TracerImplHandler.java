package rocks.inspectit.agent.android.core;

import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import rocks.inspectit.agent.android.module.CoreSpanReporter;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sdk.opentracing.util.SystemTimer;

/**
 * @author David Monschein
 *
 */
public class TracerImplHandler {

	private Reporter reporter;

	private TracerImpl tracer;

	public TracerImplHandler() {
		reporter = (CoreSpanReporter) DependencyManager.getModuleByClass(CoreSpanReporter.class);
		tracer = new TracerImpl(new SystemTimer(), reporter, true);
	}

	public SpanImpl buildSpan(String name) {
		if (getCurrentContext() == null) {
			return tracer.buildSpan(name).start();
		} else {
			return buildSpanWithParent(name, getCurrentContext());
		}
	}

	public SpanContext getCurrentContext() {
		return tracer.getCurrentContext();
	}

	public <C> void inject(SpanContext ctx, Format<C> format, C carrier) {
		tracer.inject(ctx, format, carrier);
	}

	private SpanImpl buildSpanWithParent(String name, SpanContext parent) {
		return tracer.buildSpan(name).asChildOf(parent).start();
	}

}
