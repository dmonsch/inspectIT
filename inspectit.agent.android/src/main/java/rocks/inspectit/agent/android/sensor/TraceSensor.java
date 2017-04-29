package rocks.inspectit.agent.android.sensor;

import android.util.LongSparseArray;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.util.DependencyManager;

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
	private CallbackManager callbackManager = DependencyManager.getCallbackManager();

	/**
	 * Name of the class.
	 */
	private LongSparseArray<String> clazz;

	/**
	 * Signature of the method.
	 */
	private LongSparseArray<String> signature;

	// INNER
	/**
	 * Whether it is a new trace or not.
	 */
	private LongSparseArray<Boolean> newTrace;

	/**
	 * The id of the trace.
	 */
	private LongSparseArray<Long> traceId;

	/**
	 * Creates a new instance.
	 */
	public TraceSensor() {
		clazz = new LongSparseArray<String>();
		signature = new LongSparseArray<String>();
		newTrace = new LongSparseArray<Boolean>();
		traceId = new LongSparseArray<Long>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long id) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionThrown(long id, final String caused) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long id) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void secondAfterBody(long id) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOwner(long id, final String owner) {
		clazz.put(id, owner);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSignature(long id, final String methodSignature) {
		signature.put(id, methodSignature);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCallbackManager(CallbackManager callbackManager) {
		this.callbackManager = callbackManager;
	}

}
