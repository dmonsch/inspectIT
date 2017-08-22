package rocks.inspectit.agent.android.delegation.event;

import java.util.Map;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.ISensor;

/**
 * @author David Monschein
 *
 */
public class OnStopEvent implements IDelegationEvent {

	public OnStopEvent() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(AbstractBroadcastReceiver[] receivers, AbstractMonitoringModule[] modules, Map<Integer, ISensor> sensors) {
		for (AbstractBroadcastReceiver recv : receivers) {
			recv.onStop();
		}
	}
}
