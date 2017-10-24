package rocks.inspectit.agent.android.module;

import java.util.LinkedList;

import android.content.Context;
import rocks.inspectit.agent.android.util.SpanTransformer;
import rocks.inspectit.agent.java.sdk.opentracing.Reporter;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.communication.data.mobile.AbstractMobileSpanDetails;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;
import rocks.inspectit.shared.all.communication.data.mobile.MobileFunctionExecution;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;

/**
 * @author David Monschein
 *
 */
public class CoreSpanReporter extends AbstractMonitoringModule implements Reporter {

	private static LinkedList<HttpNetworkRequest> networkRequestCorrelation = new LinkedList<HttpNetworkRequest>();

	public static void queueNetworkRequest(HttpNetworkRequest req) {
		networkRequestCorrelation.add(req);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void report(SpanImpl span) {
		MobileSpan actualSpan = SpanTransformer.transform(span);

		AbstractMobileSpanDetails details;
		if (span.getBaggageItem("net") == null) {
			MobileFunctionExecution funcDetails = new MobileFunctionExecution();
			funcDetails.setMethodSignature(span.getOperationName());
			details = funcDetails;
		} else {
			details = networkRequestCorrelation.pop();
		}

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
