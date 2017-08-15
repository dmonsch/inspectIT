package rocks.inspectit.agent.android.delegation.event;

import java.util.Map;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.ISensor;

/**
 * @author David Monschein
 *
 */
public class MethodExceptionEvent extends AbstractMethodEvent {

	private String exceptionClass;

	/**
	 * @param sensorId
	 * @param methodSignature
	 * @param parameters
	 */
	public MethodExceptionEvent(long sensorId, String methodSignature, Object[] parameters, String exceptionClass) {
		super(sensorId, methodSignature, parameters);
		this.exceptionClass = exceptionClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AbstractBroadcastReceiver[] receivers, AbstractMonitoringModule[] modules, Map<Long, ISensor> sensors) {
		if (sensors.containsKey(sensorId)) {
			ISensor belongingSensor = sensors.get(sensorId);
			belongingSensor.exceptionThrown(methodSignature.hashCode(), methodSignature, parameters, exceptionClass);
		}
	}

}
