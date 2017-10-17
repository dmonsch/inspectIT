package rocks.inspectit.agent.android.core;

import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;

/**
 * @author David Monschein
 *
 */
public class CoreSpanReporter implements Reporter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void report(SpanImpl span) {
		SpanContextImpl context = span.context();

		System.out.println(span.context().getId());
		System.out.println(context);
	}

}
