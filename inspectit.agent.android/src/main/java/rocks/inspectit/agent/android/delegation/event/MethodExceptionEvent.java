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
	public MethodExceptionEvent(int sensorId, long methodId, String methodSignature, Object object, String exceptionClass) {
		super(sensorId, methodId, methodSignature, object);
		this.exceptionClass = exceptionClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AbstractBroadcastReceiver[] receivers, AbstractMonitoringModule[] modules, Map<Integer, ISensor> sensors) {
		if (sensors.containsKey(sensorId)) {
			ISensor belongingSensor = sensors.get(sensorId);
			belongingSensor.exceptionThrown(sensorId, methodId, methodSignature, object, exceptionClass);
		}
	}

}
