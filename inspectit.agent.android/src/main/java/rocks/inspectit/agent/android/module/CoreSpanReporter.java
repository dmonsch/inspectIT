package rocks.inspectit.agent.android.module;

import android.content.Context;
import rocks.inspectit.agent.android.util.SpanTransformer;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.communication.data.mobile.MobileFunctionExecution;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;

/**
 * @author David Monschein
 *
 */
public class CoreSpanReporter extends AbstractMonitoringModule implements Reporter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void report(SpanImpl span) {
		MobileSpan actualSpan = SpanTransformer.transform(span);

		MobileFunctionExecution details = new MobileFunctionExecution();
		details.setMethodSignature(span.getOperationName());

		actualSpan.setDetails(details);

		this.pushData(actualSpan);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(Context ctx) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdownModule() {
	}

}
