package rocks.inspectit.agent.android.delegation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.sensor.AbstractMethodSensor;
import rocks.inspectit.agent.android.sensor.SensorAnnotation;

/**
 * Class which is used as fixed point to delegate method calls to the belonging dynamic endpoints in
 * the Android Agent.
 *
 * @author David Monschein
 *
 */
public class AndroidAgentDelegator {
	/**
	 * List of all actual {@link AbstractBroadcastReceiver} objects.
	 */
	private AbstractBroadcastReceiver[] broadcastReceivers;

	/**
	 * List of all actual {@link AbstractMonitoringModule} objects.
	 */
	private AbstractMonitoringModule[] monitoringModules;

	/**
	 * Maps a sensor id (int) to the belonging instance of a {@link AbstractMethodSensor}.
	 */
	private Map<Integer, AbstractMethodSensor> classSensorMapping;

	/**
	 * Whether the delegator has been initialized.
	 */
	private static boolean inited;

	/**
	 * Current instance of the delegator.
	 */
	private static AndroidAgentDelegator instance;

	static {
		inited = false;
	}

	// STATIC PROCESSING METHODS -> FASTER
	/**
	 * Delegates a start of an Android activity.
	 *
	 * @param thisObject
	 *            activity object
	 */
	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_START)
	public static void delegateOnStartEvent(Object thisObject) {
		if (inited) {
			instance.processOnStart(thisObject);
		}
	}

	/**
	 * Delegates a stop of an Android activity.
	 *
	 * @param thisObject
	 *            activity object
	 */
	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_STOP)
	public static void delegateOnStopEvent(Object thisObject) {
		if (inited) {
			instance.processOnStop(thisObject);
		}
	}

	/**
	 * Delegates a method call.
	 *
	 * @param sensorId
	 *            id of the sensor which should process the method call
	 * @param methodId
	 *            id of the method
	 * @param signature
	 *            signature of the method
	 * @param root
	 *            object which used to call the method or null if its a static method.
	 */
	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_METHOD_ENTER)
	public static void delegateOnMethodEnterEvent(int sensorId, long methodId, String signature, Object root) {
		if (inited) {
			instance.processOnMethodEnter(sensorId, methodId, signature, root);
		}
	}

	/**
	 * Delegates the end of a method call.
	 *
	 * @param sensorId
	 *            id of the sensor which should process the method call
	 * @param methodId
	 *            id of the method
	 * @param signature
	 *            signature of the method
	 * @param root
	 *            object which used to call the method or null if its a static method.
	 */
	@DelegationAnnotation(correspondsTo = DelegationPoint.ON_METHOD_EXIT)
	public static void delegateOnMethodExitEvent(int sensorId, long methodId, String signature, Object root) {
		if (inited) {
			instance.processOnMethodExit(sensorId, methodId, signature, root);
		}
	}

	/**
	 * Creates a new instance of {@link AndroidAgentDelegator}.
	 */
	public AndroidAgentDelegator() {
		classSensorMapping = new HashMap<>();
	}

	/**
	 * Initializes the current instance.
	 *
	 * @param rc
	 *            list of broadcast receivers
	 * @param ms
	 *            list of monitoring modules
	 * @param sl
	 *            list of method sensors
	 * 
	 * @see AndroidAgentDelegator#initDelegator(List, List, List)
	 */
	public void initDelegator(List<AbstractBroadcastReceiver> rc, List<AbstractMonitoringModule> ms, List<AbstractMethodSensor> sl) {
		initDelegator(rc.toArray(new AbstractBroadcastReceiver[rc.size()]), ms.toArray(new AbstractMonitoringModule[ms.size()]), sl.toArray(new AbstractMethodSensor[sl.size()]));
	}

	/**
	 * Initializes the current instance.
	 *
	 * @param receivers
	 *            array of broadcast receivers
	 * @param modules
	 *            array of monitoring modules
	 * @param sensors
	 *            array of method sensors
	 */
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

	/**
	 * Internal method to handle a method call.
	 *
	 * @param sensorId
	 *            id of the sensor which should process the method call
	 * @param methodId
	 *            id of the method
	 * @param signature
	 *            signature of the method
	 * @param root
	 *            object which used to call the method or null if its a static method.
	 */
	private void processOnMethodEnter(int sensorId, long methodId, String signature, Object root) {
		instance.classSensorMapping.get(sensorId).beforeBody(methodId, signature, root);
	}

	/**
	 * Internal method to handle the end of a method call.
	 *
	 * @param sensorId
	 *            id of the sensor which should process the method call
	 * @param methodId
	 *            id of the method
	 * @param signature
	 *            signature of the method
	 * @param root
	 *            object which used to call the method or null if its a static method.
	 */
	private void processOnMethodExit(int sensorId, long methodId, String signature, Object root) {
		instance.classSensorMapping.get(sensorId).firstAfterBody(methodId, signature, root);
	}

	/**
	 * Internal method to process a start of a Android activity.
	 *
	 * @param thisObject
	 *            object
	 */
	private void processOnStart(Object thisObject) {
		for (AbstractBroadcastReceiver brRecv : instance.broadcastReceivers) {
			brRecv.onStart(thisObject);
		}

		for (AbstractMonitoringModule module : monitoringModules) {
			module.onStartActivity(thisObject);
		}
	}

	/**
	 * Internal method to process a stop of a Android activity.
	 *
	 * @param thisObject
	 *            object
	 */
	private void processOnStop(Object thisObject) {
		for (AbstractBroadcastReceiver brRecv : instance.broadcastReceivers) {
			brRecv.onStop(thisObject);
		}

		for (AbstractMonitoringModule module : monitoringModules) {
			module.onStopActivity(thisObject);
		}
	}
}
