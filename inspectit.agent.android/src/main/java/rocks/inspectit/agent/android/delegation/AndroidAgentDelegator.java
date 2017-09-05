package rocks.inspectit.agent.android.delegation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.ISensor;
import rocks.inspectit.agent.android.sensor.SensorAnnotation;

/**
 * @author David Monschein
 *
 */
public class AndroidAgentDelegator {

	// TODO is a initing queue necessary?

	private AbstractBroadcastReceiver[] broadcastReceivers;
	private AbstractMonitoringModule[] monitoringModules;

	private Map<Integer, ISensor> classSensorMapping;

	private static boolean inited;

	private static AndroidAgentDelegator instance;

	static {
		inited = false;
	}

	// STATIC PROCESSING METHODS -> FASTER
	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_START)
	public static void delegateOnStartEvent() {
		if (inited) {
			instance.processOnStart();
		}
	}

	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_STOP)
	public static void delegateOnStopEvent() {
		if (inited) {
			instance.processOnStop();
		}
	}

	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_METHOD_ENTER)
	public static void delegateOnMethodEnterEvent(int sensorId, long methodId, String signature, Object root) {
		if (inited) {
			instance.processOnMethodEnter(sensorId, methodId, signature, root);
		}
	}

	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_METHOD_EXIT)
	public static void delegateOnMethodExitEvent(int sensorId, long methodId, String signature, Object root) {
		if (inited) {
			instance.processOnMethodExit(sensorId, methodId, signature, root);
		}
	}

	public AndroidAgentDelegator() {
		classSensorMapping = new HashMap<>();
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
		// swapQueue(); // -> initing queue necessary and makes sense?
	}

	private void processOnMethodEnter(int sensorId, long methodId, String signature, Object root) {
		instance.classSensorMapping.get(sensorId).beforeBody(methodId, signature, root);
	}

	private void processOnMethodExit(int sensorId, long methodId, String signature, Object root) {
		instance.classSensorMapping.get(sensorId).firstAfterBody(methodId, signature, root);
	}

	private void processOnStart() {
		for (AbstractBroadcastReceiver brRecv : instance.broadcastReceivers) {
			brRecv.onStart();
		}
	}

	private void processOnStop() {
		for (AbstractBroadcastReceiver brRecv : instance.broadcastReceivers) {
			brRecv.onStop();
		}
	}
}
