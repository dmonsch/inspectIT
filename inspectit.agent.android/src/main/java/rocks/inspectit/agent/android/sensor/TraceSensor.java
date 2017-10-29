package rocks.inspectit.agent.android.sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
public class TraceSensor extends AbstractMethodSensor {

	private static final Pattern METHOD_SIG_PATTERN = Pattern.compile("(L.*;)(.*)\\((.*?)\\)(.*)");

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
		System.out.println(methodSignature);
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
