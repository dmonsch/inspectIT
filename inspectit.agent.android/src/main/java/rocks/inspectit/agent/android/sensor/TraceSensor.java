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
public class TraceSensor implements ISensor {
	/**
	 * Reference to the {@link CallbackManager}.
	 */
	private CallbackManager callbackManager;

	private TracerImplHandler tracerUtil;

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
	public void beforeBody(long id, String signature) {
		if (spanMapping.get() == null) {
			spanMapping.set(new HashMap<Long, SpanImpl>());
		}

		spanMapping.get().put(id, tracerUtil.buildSpan(signature));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionThrown(long id, final String caused) {
		if (spanMapping.get() != null) {
			Map<Long, SpanImpl> implMapping = spanMapping.get();
			if (implMapping.containsKey(id)) {
				SpanImpl correspondingSpan = implMapping.get(id);
				correspondingSpan.log(caused).finish();
				implMapping.remove(id);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long id) {
		if (spanMapping.get() != null) {
			Map<Long, SpanImpl> implMapping = spanMapping.get();
			if (implMapping.containsKey(id)) {
				SpanImpl correspondingSpan = implMapping.get(id);
				correspondingSpan.finish();
				implMapping.remove(id);
			}
		}
	}

}
