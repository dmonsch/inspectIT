package rocks.inspectit.agent.android.util;

import java.sql.Timestamp;

import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanContextImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;
import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * @author David Monschein
 *
 */
public class SpanTransformer {

	public static MobileSpan transform(SpanImpl impl) {
		// context to ident
		SpanIdent ident = transformSpanContext(impl.context());
		if (ident == null) {
			return null;
		}

		MobileSpan span = new MobileSpan();
		span.setSpanIdent(ident);

		// transform to inspectIT way of time handling
		long timestampMillis = impl.getStartTimeMicros() / 1000;
		double durationMillis = impl.getDuration() / 1000.0d;
		span.setTimeStamp(new Timestamp(timestampMillis));
		span.setDuration(durationMillis);

		// reference
		if (impl.context().getParentId() != ident.getId()) {
			span.setParentSpanId(impl.context().getParentId());
		}
		span.setReferenceType(impl.context().getReferenceType());

		// operation name (we save as tag)
		if (null != impl.getOperationName()) {
			span.addTag(ExtraTags.OPERATION_NAME, impl.getOperationName());
		}

		return span;
	}

	private static SpanIdent transformSpanContext(SpanContextImpl context) {
		if (context == null) {
			return null;
		}

		return new SpanIdent(context.getId(), context.getTraceId());
	}

}
