package rocks.inspectit.agent.android.sensor;

import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.android.mobile.SpanResponse;

/**
 * Sensor for dealing with operations executed before and after method instrumented executions.
 *
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 2)
public class TraceSensor extends AbstractMethodSensor {
	/**
	 * Reference to the {@link CallbackManager}.
	 */
	private CallbackManager callbackManager;

	/**
	 * Link to the tracer implementation.
	 */
	private TracerImplHandler tracerUtil;

	/**
	 * Thread local map which maps a method execution (method id) to the belonging span.
	 */
	private ThreadLocal<Map<Long, SpanImpl>> spanMapping = new ThreadLocal<>();

	/**
	 * Creates a new instance.
	 */
	public TraceSensor() {
		callbackManager = DependencyManager.getCallbackManager();
		tracerUtil = DependencyManager.getTracerImplHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, String methodSignature, Object object) {
		if (spanMapping.get() == null) {
			spanMapping.set(new HashMap<Long, SpanImpl>());
		}

		SpanImpl nSpan = tracerUtil.buildSpan(methodSignature);

		spanMapping.get().put(methodId, nSpan);
		nSpan.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, String methodSignature, Object object) {
		if (spanMapping.get() != null) {
			Map<Long, SpanImpl> implMapping = spanMapping.get();
			if (implMapping.containsKey(methodId)) {
				SpanImpl correspondingSpan = implMapping.get(methodId);
				correspondingSpan.finish();
				implMapping.remove(methodId);

				callbackManager.pushData(new SpanResponse(correspondingSpan));
			}
		}
	}

}
