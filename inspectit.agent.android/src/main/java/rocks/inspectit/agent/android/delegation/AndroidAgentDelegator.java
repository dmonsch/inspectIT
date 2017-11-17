package rocks.inspectit.agent.android.delegation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.AbstractMethodSensor;
import rocks.inspectit.agent.android.sensor.SensorAnnotation;

/**
 * @author David Monschein
 *
 */
public class AndroidAgentDelegator {

	// TODO is a initing queue necessary?

	private AbstractBroadcastReceiver[] broadcastReceivers;
	private AbstractMonitoringModule[] monitoringModules;

	private Map<Integer, AbstractMethodSensor> classSensorMapping;

	private static boolean inited;

	private static AndroidAgentDelegator instance;

	static {
		inited = false;
	}

	// STATIC PROCESSING METHODS -> FASTER
	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_START)
	public static void delegateOnStartEvent(Object _this) {
		if (inited) {
			instance.processOnStart(_this);
		}
	}

	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_STOP)
	public static void delegateOnStopEvent(Object _this) {
		if (inited) {
			instance.processOnStop(_this);
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

	public void initDelegator(List<AbstractBroadcastReceiver> rc, List<AbstractMonitoringModule> ms, List<AbstractMethodSensor> sl) {
		initDelegator(rc.toArray(new AbstractBroadcastReceiver[rc.size()]), ms.toArray(new AbstractMonitoringModule[ms.size()]), sl.toArray(new AbstractMethodSensor[sl.size()]));
	}

	public void initDelegator(AbstractBroadcastReceiver[] receivers, AbstractMonitoringModule[] modules, AbstractMethodSensor[] sensors) {
		broadcastReceivers = receivers;
		monitoringModules = modules;

		for (AbstractMethodSensor sensor : sensors) {
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

	private void processOnStart(Object _this) {
		for (AbstractBroadcastReceiver brRecv : instance.broadcastReceivers) {
			brRecv.onStart(_this);
		}

		for (AbstractMonitoringModule module : monitoringModules) {
			module.onStartActivity(_this);
		}
	}

	private void processOnStop(Object _this) {
		for (AbstractBroadcastReceiver brRecv : instance.broadcastReceivers) {
			brRecv.onStop(_this);
		}

		for (AbstractMonitoringModule module : monitoringModules) {
			module.onStopActivity(_this);
		}
	}
}
