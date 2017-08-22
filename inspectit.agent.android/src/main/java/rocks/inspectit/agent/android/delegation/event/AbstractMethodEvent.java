package rocks.inspectit.agent.android.delegation.event;

/**
 * @author David Monschein
 *
 */
abstract class AbstractMethodEvent implements IDelegationEvent {

	protected int sensorId;
	protected long methodId;
	protected String methodSignature;
	protected Object object;

	/**
	 * @param sensorId
	 * @param methodSignature
	 * @param parameters
	 */
	public AbstractMethodEvent(int sensorId, long methodId, String methodSignature, Object object) {
		super();
		this.sensorId = sensorId;
		this.methodSignature = methodSignature;
		this.object = object;
		this.methodId = methodId;
	}

}
