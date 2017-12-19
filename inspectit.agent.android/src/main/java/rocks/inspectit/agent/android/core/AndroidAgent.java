package rocks.inspectit.agent.android.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;
import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.broadcast.BatteryBroadcastReceiver;
import rocks.inspectit.agent.android.callback.CMRConnectionManager;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.callback.strategies.AbstractCallbackStrategy;
import rocks.inspectit.agent.android.callback.strategies.IntervalStrategy;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.delegation.AndroidAgentDelegator;
import rocks.inspectit.agent.android.interfaces.IScheduledExecutorService;
import rocks.inspectit.agent.android.interfaces.impl.ScheduledExecutorServiceImpl;
import rocks.inspectit.agent.android.module.AndroidModuleManager;
import rocks.inspectit.agent.android.sensor.AbstractMethodSensor;
import rocks.inspectit.agent.android.sensor.NetworkSensor;
import rocks.inspectit.agent.android.sensor.TraceSensor;
import rocks.inspectit.agent.android.speedindex.SIListenerManager;
import rocks.inspectit.agent.android.util.DependencyManager;

/**
 * The main Android Agent class which is responsible for managing and scheduling tasks.
 *
 * @author David Monschein
 */
public final class AndroidAgent {
	/**
	 * Resolve consistent log tag for the agent.
	 */
	private static String logTag;

	/**
	 * Broadcast receiver classes which will be created when the agent is initialized.
	 */
	private static final Class<?>[] BROADCAST_RECVS = new Class<?>[] { BatteryBroadcastReceiver.class };

	/**
	 * The sensor which is responsible for collecting traces of instrumented method executions.
	 */
	private static List<AbstractMethodSensor> sensorList;

	/**
	 * Reference to the {@link AndroidModuleManager}.
	 */
	private static AndroidModuleManager moduleManager;

	/**
	 * Reference to the {@link CMRConnectionManager}.
	 */
	private static CMRConnectionManager connectionManager;

	/**
	 * List of created broadcast receivers.
	 */
	private static List<AbstractBroadcastReceiver> createdReceivers = new ArrayList<>();

	/**
	 * Handler for scheduling timing tasks.
	 */
	private static Handler mHandler = new Handler();

	/**
	 * The context of the application.
	 */
	private static Context initContext;

	/**
	 * Callback manager component for the communication with the server which persists the
	 * monitoring data.
	 */
	private static CallbackManager callbackManager;

	/**
	 * Specifies whether the agents init method has been already called or not.
	 */
	private static boolean inited = false;

	/**
	 * Specifies whether the agents destroy method has benn already called or not.
	 */
	private static boolean closed = true;

	/**
	 * Contains all activities which are instrumented by the {@link SIListenerManager}.
	 */
	private static Map<Class<? extends Activity>, Boolean> activitySpeedindexMap;

	/**
	 * Responsible for measuring the speed index of activities which indicates how fast a activity
	 * is drawn.
	 */
	private static SIListenerManager speedindexManager;

	static {
		activitySpeedindexMap = new HashMap<Class<? extends Activity>, Boolean>();
		speedindexManager = new SIListenerManager();
	}

	/**
	 * Full static class, therefore no instance creation allowed.
	 */
	private AndroidAgent() {
	}

	/**
	 * Inits the agent with a given application context.
	 *
	 * @param ctx
	 *            context of the application
	 */
	public static synchronized void initAgent(Activity ctx) {
		if (ctx == null) {
			return;
		}

		if (!activitySpeedindexMap.containsKey(ctx.getClass())) {
			speedindexManager.registerListenerFor(ctx);
			activitySpeedindexMap.put(ctx.getClass(), true);
		}

		if (inited) {
			return;
		}
		// INITING VARS
		initContext = ctx;

		if (!loadAgentConfiguration(ctx)) {
			return;
		}

		Log.i(logTag, "Initing mobile agent for Android.");
		initDataCollector(ctx);
		initCallbackManager();

		IScheduledExecutorService schedExService = new ScheduledExecutorServiceImpl(mHandler);

		// OPEN COMMUNICATION WITH CMR
		connectionManager = new CMRConnectionManager(callbackManager, schedExService);
		DependencyManager.setCmrConnectionManager(connectionManager);
		connectionManager.establishCommunication();

		// INITING MODULES
		moduleManager = new AndroidModuleManager(ctx, schedExService);
		moduleManager.initModules();
		initTracerUtil(); // needs module

		// INITING SENSORS
		sensorList = new ArrayList<>();
		sensorList.add(new TraceSensor());
		sensorList.add(new NetworkSensor());

		for (AbstractMethodSensor sens : sensorList) {
			injectDependencies(sens);
		}

		// INIT BROADCASTS
		Log.i(logTag, "Initializing broadcast receivers programmatically.");
		if (AgentConfiguration.current.isCollectBatteryConsumption()) {
			initBroadcastReceivers(ctx);
		}

		// INIT DELEGATOR
		AndroidAgentDelegator delegator = new AndroidAgentDelegator();
		delegator.initDelegator(createdReceivers, moduleManager.getModules(), sensorList);

		// SET VALUES
		inited = true;
		closed = false;

		Log.i(logTag, "Finished initializing the Android Agent.");
	}

	/**
	 * Shuts down the agent. This methods suspends all modules and broadcast receivers.
	 */
	public static synchronized void destroyAgent() {
		if (closed || !inited || !AgentConfiguration.current.isShutdownOnDestroy()) {
			return;
		}
		Log.i(logTag, "Shutting down the Android Agent.");

		// SHUTDOWN MODULES
		mHandler.removeCallbacksAndMessages(null);
		moduleManager.shutdownModules();

		// SHUTDOWN BROADCASTS
		for (BroadcastReceiver exReceiver : createdReceivers) {
			if (exReceiver != null) {
				initContext.unregisterReceiver(exReceiver);
			}
		}

		// SHUTDOWN CALLBACK
		callbackManager.shutdown();

		// SET VALUES
		inited = false;
		closed = true;
	}

	/**
	 * Shuts down the Android Agent.
	 *
	 * @param message
	 *            the reason why the agent is shut down
	 */
	public static void shutdownAgent(String message) {
		Log.w(AgentConfiguration.current.getLogTag(), "The Android Agent encountered a problem (\"" + message + "\") and will shut down.");
		destroyAgent();
	}

	/**
	 * Adds all references to necessary components to a {@link AndroidMonitoringComponent}.
	 *
	 * @param comp
	 *            target {@link AndroidMonitoringComponent}
	 */
	private static void injectDependencies(AndroidMonitoringComponent comp) {
		comp.setAndroidDataCollector(DependencyManager.getAndroidDataCollector());
		comp.setCallbackManager(DependencyManager.getCallbackManager());
	}

	/**
	 * Initializes a {@link AndroidDataCollector} instance and adds it to the
	 * {@link DependencyManager}.
	 *
	 * @param ctx
	 *            the context which can be used to access data by the {@link AndroidDataCollector}
	 */
	private static void initDataCollector(Context ctx) {
		// INIT ANDROID DATA COLLECTOR
		final AndroidDataCollector androidDataCollector = new AndroidDataCollector();
		androidDataCollector.initDataCollector(ctx);
		DependencyManager.setAndroidDataCollector(androidDataCollector);
	}

	/**
	 * Initializes a {@link TracerImplHandler} instance and adds it to the
	 * {@link DependencyManager}.
	 */
	private static void initTracerUtil() {
		DependencyManager.setTracerImplHandler(new TracerImplHandler());
	}

	/**
	 * Initializes a {@link CallbackManager} instance and adds it to the {@link DependencyManager}.
	 */
	private static void initCallbackManager() {
		// INITING CALLBACK
		final AbstractCallbackStrategy callbackStrategy = new IntervalStrategy(5000L);
		DependencyManager.setCallbackStrategy(callbackStrategy);
		callbackManager = new CallbackManager();
		DependencyManager.setCallbackManager(callbackManager);
	}

	/**
	 * Initializes all broadcast receiver classes contained in {@link AndroidAgent#BROADCAST_RECVS}.
	 *
	 * @param ctx
	 *            application context which can be used to register the broadcast receivers
	 */
	private static void initBroadcastReceivers(Context ctx) {
		for (Class<?> bRecvEntry : BROADCAST_RECVS) {
			try {
				final AbstractBroadcastReceiver bRecv = (AbstractBroadcastReceiver) bRecvEntry.newInstance();
				injectDependencies(bRecv);

				// create filter
				final IntentFilter filter = new IntentFilter();
				for (String action : bRecv.getFilterActions()) {
					filter.addAction(action);
				}

				// register
				ctx.registerReceiver(bRecv, filter);

			} catch (InstantiationException e) {
				Log.e(logTag, "Failed to init broadcast receiver of class '" + bRecvEntry.getClass().getName() + "'");
			} catch (IllegalAccessException e) {
				Log.e(logTag, "Failed to init broadcast receiver of class '" + bRecvEntry.getClass().getName() + "'");
			}
		}
	}

	/**
	 * Passes references to important agent components to a broadcast receiver.
	 *
	 * @param recv
	 *            the broadcast receiver
	 */
	private static void injectDependencies(AbstractBroadcastReceiver recv) {
		recv.setCallbackManager(DependencyManager.getCallbackManager());
		recv.setAndroidDataCollector(DependencyManager.getAndroidDataCollector());
	}

	/**
	 * Loads the configuration of the agent from the asset file created within the instrumentation
	 * process.
	 *
	 * @param ctx
	 *            the application which is needed to access the asset file
	 * @return true if the configuration has been loaded successfully, false otherwise
	 */
	private static boolean loadAgentConfiguration(Context ctx) {
		// LOADING CONFIGURATION
		AgentConfiguration config = null;
		AssetManager assetManager = ctx.getAssets();
		ObjectMapper tempMapper = new ObjectMapper();
		try {
			InputStream configStream = assetManager.open("inspectit_agent_config.json");
			config = tempMapper.readValue(configStream, AgentConfiguration.class);
		} catch (IOException e1) {
			shutdownAgent("Couldn't read agent configuration file.");
			return false;
		}
		if (config == null) {
			return false;
		}
		// APPLY CONFIG
		applyConfiguration(config);
		return true;
	}

	/**
	 * Applies the given agent configuration.
	 *
	 * @param conf
	 *            configuration for the agent
	 */
	private static void applyConfiguration(AgentConfiguration conf) {
		logTag = conf.getLogTag();
		AgentConfiguration.current = conf;
	}
}
