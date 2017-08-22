package rocks.inspectit.agent.android.delegation.event;

import java.util.Map;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.ISensor;

/**
 * @author David Monschein
 *
 */
public class MethodExitEvent extends AbstractMethodEvent {

	/**
	 * @param sensorId
	 * @param methodSignature
	 * @param parameters
	 */
	public MethodExitEvent(int sensorId, long methodId, String methodSignature, Object object) {
		super(sensorId, methodId, methodSignature, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AbstractBroadcastReceiver[] receivers, AbstractMonitoringModule[] modules, Map<Integer, ISensor> sensors) {
		if (sensors.containsKey(sensorId)) {
			ISensor belongingSensor = sensors.get(sensorId);
			belongingSensor.firstAfterBody(sensorId, methodId, methodSignature, object);
		}
	}

}
