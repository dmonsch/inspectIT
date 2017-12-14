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
 * Handler which responsible for building spans. For more information consult the opentrang
 * documentation.
 *
 * @author David Monschein
 *
 */
public class TracerImplHandler {
	/**
	 * Reporter which receives all spans that have been finished.
	 */
	private Reporter reporter;

	/**
	 * Reference to {@link TracerImpl} instance.
	 */
	private TracerImpl tracer;

	/**
	 * Creates a new handler and sets the values for {@link TracerImplHandler#reporter} and
	 * {@link TracerImplHandler#tracer}.
	 */
	public TracerImplHandler() {
		reporter = (CoreSpanReporter) DependencyManager.getModuleByClass(CoreSpanReporter.class);
		tracer = new TracerImpl(new SystemTimer(), reporter, true);
	}

	/**
	 * Builds a span with a given name.
	 *
	 * @param name
	 *            name for the span
	 * @return created span
	 */
	public SpanImpl buildSpan(String name) {
		if (getCurrentContext() == null) {
			return tracer.buildSpan(name).start();
		} else {
			return buildSpanWithParent(name, getCurrentContext());
		}
	}

	/**
	 * Gets the current {@link SpanContext}.
	 *
	 * @return current {@link SpanContext}
	 */
	public SpanContext getCurrentContext() {
		return tracer.getCurrentContext();
	}

	/**
	 * Injects a {@link SpanContext} into a carrier which is used to propagate spans over front- and
	 * backend.
	 *
	 *
	 * @param <C>
	 *            the carrier type, which also parametrizes the Format.
	 * @param ctx
	 *            the SpanContext instance to inject into the carrier
	 * @param format
	 *            the Format of the carrier
	 * @param carrier
	 *            the carrier for the SpanContext state.
	 * 
	 * @see io.opentracing.Tracer
	 */
	public <C> void inject(SpanContext ctx, Format<C> format, C carrier) {
		tracer.inject(ctx, format, carrier);
	}

	/**
	 * Creates a span with a given parent.
	 *
	 * @param name
	 *            name for the span
	 * @param parent
	 *            reference to the parent context
	 * @return created span
	 */
	private SpanImpl buildSpanWithParent(String name, SpanContext parent) {
		return tracer.buildSpan(name).asChildOf(parent).start();
	}

}
