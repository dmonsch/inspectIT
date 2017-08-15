package rocks.inspectit.agent.android.sensor;

import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;

/**
 * Sensor for dealing with operations executed before and after method instrumented executions.
 *
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 2)
public class TraceSensor implements ISensor {
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
	public void beforeBody(long methodId, String methoSignature, Object[] parameters) {
		if (spanMapping.get() == null) {
			spanMapping.set(new HashMap<Long, SpanImpl>());
		}

		spanMapping.get().put(methodId, tracerUtil.buildSpan(methoSignature));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionThrown(long methodId, String methodSignature, Object[] parameters, String clazz) {
		if (spanMapping.get() != null) {
			Map<Long, SpanImpl> implMapping = spanMapping.get();
			if (implMapping.containsKey(methodId)) {
				SpanImpl correspondingSpan = implMapping.get(methodId);
				correspondingSpan.log(clazz).finish();
				implMapping.remove(methodId);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, String methodSignature, Object[] parameters) {
		if (spanMapping.get() != null) {
			Map<Long, SpanImpl> implMapping = spanMapping.get();
			if (implMapping.containsKey(methodId)) {
				SpanImpl correspondingSpan = implMapping.get(methodId);
				correspondingSpan.finish();
				implMapping.remove(methodId);
			}
		}
	}

}
