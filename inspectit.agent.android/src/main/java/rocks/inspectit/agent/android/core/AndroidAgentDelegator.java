package rocks.inspectit.agent.android.core;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.ISensor;

/**
 * @author David Monschein
 *
 */
public class AndroidAgentDelegator {

	private AbstractBroadcastReceiver[] broadcastReceivers;
	private AbstractMonitoringModule[] monitoringModules;

	private ISensor[] sensors;

	protected AndroidAgentDelegator() {
	}

	public void delegateMethodCall(String clazzName, String methodName, Object[] parameters) {
	}

	public void delegateOnStart() {
		for (AbstractBroadcastReceiver bRecv : broadcastReceivers) {
			bRecv.onStart();
		}
	}

	public void delegateOnStop() {
		for (AbstractBroadcastReceiver bRecv : broadcastReceivers) {
			bRecv.onStop();
		}
	}
}
