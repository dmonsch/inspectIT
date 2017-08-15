package rocks.inspectit.agent.android.delegation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.delegation.event.IDelegationEvent;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.ISensor;
import rocks.inspectit.agent.android.sensor.SensorAnnotation;

/**
 * @author David Monschein
 *
 */
public class AndroidAgentDelegator {
	private AbstractBroadcastReceiver[] broadcastReceivers;
	private AbstractMonitoringModule[] monitoringModules;

	private Map<Long, ISensor> classSensorMapping;

	private static boolean inited;
	private static Queue<IDelegationEvent> initingQueue;

	private static AndroidAgentDelegator instance;

	static {
		initingQueue = new LinkedList<>();
		inited = false;
	}

	public AndroidAgentDelegator() {
		classSensorMapping = new HashMap<>();
	}

	public static void delegateEvent(IDelegationEvent event) {
		if (inited) {
			instance.processEvent(event);
		} else {
			initingQueue.add(event);
		}
	}

	public void initDelegator(List<AbstractBroadcastReceiver> rc, List<AbstractMonitoringModule> ms, List<ISensor> sl) {
		initDelegator(rc.toArray(new AbstractBroadcastReceiver[rc.size()]), ms.toArray(new AbstractMonitoringModule[ms.size()]), sl.toArray(new ISensor[sl.size()]));
	}

	public void initDelegator(AbstractBroadcastReceiver[] receivers, AbstractMonitoringModule[] modules, ISensor[] sensors) {
		broadcastReceivers = receivers;
		monitoringModules = modules;

		for (ISensor sensor : sensors) {
			SensorAnnotation annot = sensor.getClass().getAnnotation(SensorAnnotation.class);
			classSensorMapping.put(annot.id(), sensor);
		}

		// inited and swap queued events
		instance = this;
		inited = true;
		swapQueue();
	}

	private void processEvent(IDelegationEvent delegEvent) {
		delegEvent.process(broadcastReceivers, monitoringModules, classSensorMapping);
	}

	private void swapQueue() {
		for (IDelegationEvent delegEvent : initingQueue) {
			instance.processEvent(delegEvent);
		}
	}
}
