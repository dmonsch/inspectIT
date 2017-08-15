package rocks.inspectit.agent.android.delegation.event;

/**
 * @author David Monschein
 *
 */
abstract class AbstractMethodEvent implements IDelegationEvent {

	protected long sensorId;
	protected String methodSignature;
	protected Object[] parameters;

	/**
	 * @param sensorId
	 * @param methodSignature
	 * @param parameters
	 */
	public AbstractMethodEvent(long sensorId, String methodSignature, Object[] parameters) {
		super();
		this.sensorId = sensorId;
		this.methodSignature = methodSignature;
		this.parameters = parameters;
	}

}
