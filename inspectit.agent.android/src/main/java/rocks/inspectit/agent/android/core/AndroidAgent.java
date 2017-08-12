package rocks.inspectit.agent.android.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

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
import rocks.inspectit.agent.android.module.AndroidModuleManager;
import rocks.inspectit.agent.android.sensor.TraceSensor;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;

/**
 * The main Android Agent class which is responsible for managing and scheduling tasks.
 *
 * @author David Monschein
 */
public final class AndroidAgent {
	/**
	 * Max size for stored records when there is no connection.
	 */
	private static final int MAX_QUEUE = 500;

	/**
	 * Resolve consistent log tag for the agent.
	 */
	private static String LOG_TAG;

	/**
	 * Broadcast receiver classes which will be created when the agent is initialized.
	 */
	private static final Class<?>[] BROADCAST_RECVS = new Class<?>[] { BatteryBroadcastReceiver.class };

	/**
	 * The sensor which is responsible for collecting traces of instrumented method executions.
	 */
	private static TraceSensor traceSensor;

	private static AndroidModuleManager moduleManager;
	private static CMRConnectionManager connectionManager;

	/**
	 * List of created broadcast receivers.
	 */
	private static List<BroadcastReceiver> createdReceivers = new ArrayList<BroadcastReceiver>();

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
	 * Queue of monitoring records which couldn't be sent till now because there is no session. This
	 * queue will be sent when there is an active connection.
	 */
	private static List<MobileDefaultData> defaultQueueInit = new ArrayList<>();

	/**
	 * Specifies whether the agents init method has been already called or not.
	 */
	private static boolean inited = false;

	/**
	 * Specifies whether the agents destroy method has benn already called or not.
	 */
	private static boolean closed = true;

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
		if (inited) {
			return;
		}

		if (ctx == null) {
			return;
		}
		// INITING VARS
		initContext = ctx;

		if (!loadAgentConfiguration(ctx)) {
			return;
		}

		Log.i(LOG_TAG, "Initing mobile agent for Android.");
		initDataCollector(ctx);
		initCallbackManager(ctx);

		// OPEN COMMUNICATION WITH CMR
		connectionManager = new CMRConnectionManager(callbackManager, mHandler);
		connectionManager.establishCommunication(ctx);

		// INITING SENSORS
		traceSensor = new TraceSensor();

		// INITING MODULES
		moduleManager = new AndroidModuleManager(ctx, mHandler);
		moduleManager.initModules();

		// INIT BROADCASTS
		Log.i(LOG_TAG, "Initializing broadcast receivers programmatically.");
		initBroadcastReceivers(ctx);

		// SET VALUES
		inited = true;
		closed = false;

		swapInitQueues();

		Log.i(LOG_TAG, "Finished initializing the Android Agent.");
	}

	/**
	 * Shuts down the agent. This methods suspends all modules and broadcast receivers.
	 */
	public static synchronized void destroyAgent() {
		if (closed || !inited) {
			return;
		}
		Log.i(LOG_TAG, "Shutting down the Android Agent.");

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

	public static void shutdownAgent(String message) {
		Log.w(AgentConfiguration.current.getLogTag(), "The Android Agent encountered a problem (\"" + message + "\") and will shut down.");
		destroyAgent();
	}

	/**
	 * This method is called by code which is inserted into the original application.
	 *
	 * @param sensorClassName
	 *            The name of the sensor which should handle this call
	 * @param methodSignature
	 *            The signature of the method which has been called
	 * @param owner
	 *            The class which owns the method which has been called
	 * @return entry id for determine corresponding sensor at the exitBody call
	 */
	public static synchronized void enterBody(int methodId, String methodSignature) {
		if (traceSensor != null) {
			traceSensor.beforeBody(methodId, methodSignature);
		}
	}

	/**
	 * This method is called by code which is inserted into the original application and is executed
	 * when a instrumented methods throws an exception.
	 *
	 * @param e
	 *            the exception which has been thrown
	 * @param enterId
	 *            the entry id for getting the responsible sensor instance
	 */
	public static synchronized void exitErrorBody(Throwable e, int methodId) {
		if (traceSensor != null) {
			traceSensor.exceptionThrown(methodId, e.getClass().getName());
		}
	}

	/**
	 * This method is called by code which is inserted into the original application and is executed
	 * when a instrumented methods returns.
	 *
	 * @param enterId
	 *            the entry id for getting the responsible sensor instance
	 */
	public static synchronized void exitBody(int methodId) {
		if (traceSensor != null) {
			traceSensor.firstAfterBody(methodId);
		}
	}

	/**
	 * This method is called by code which is inserted into the original application when the
	 * application creates a {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            a reference to the created connection
	 */
	public static void httpConnect(HttpURLConnection connection) {
		if (moduleManager.getNetworkModule() != null) {
			moduleManager.getNetworkModule().openConnection(connection);
		}
	}

	/**
	 * This method is called by code which is inserted into the original application when the
	 * application accesses the output stream of a {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            a reference to the connection
	 * @return the output stream for the connection
	 * @throws IOException
	 *             when the {@link HttpURLConnection#getOutputStream()} method of the connection
	 *             fails
	 */
	public static OutputStream httpOutputStream(HttpURLConnection connection) throws IOException {
		if (moduleManager.getNetworkModule() != null) {
			return moduleManager.getNetworkModule().getOutputStream(connection);
		} else {
			return connection.getOutputStream();
		}
	}

	/**
	 * This method is called by code which is inserted into the original application when the
	 * application accesses the response code of a {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            a reference to the connection
	 * @return the response code of the connection
	 * @throws IOException
	 *             when the {@link HttpURLConnection#getResponseCode()} method of the connection
	 *             fails
	 */
	public static int httpResponseCode(HttpURLConnection connection) throws IOException {
		if (moduleManager.getNetworkModule() != null) {
			return moduleManager.getNetworkModule().getResponseCode(connection);
		} else {
			return connection.getResponseCode();
		}
	}

	/**
	 * Queues a monitoring record which will be sent after session creation.
	 *
	 * @param data
	 *            the record which should be queued
	 */
	public static void queueForInit(MobileDefaultData data) {
		if (defaultQueueInit.size() < MAX_QUEUE) {
			defaultQueueInit.add(data);
		}
	}

	/**
	 * Swaps queues for already collected records and passes them to the {@link CallbackManager}.
	 */
	private static void swapInitQueues() {
		for (MobileDefaultData def : defaultQueueInit) {
			callbackManager.pushData(def);
		}

		defaultQueueInit.clear();
	}

	private static void initDataCollector(Context ctx) {
		// INIT ANDROID DATA COLLECTOR
		final AndroidDataCollector androidDataCollector = new AndroidDataCollector();
		androidDataCollector.initDataCollector(ctx);
		DependencyManager.setAndroidDataCollector(androidDataCollector);
	}

	private static void initCallbackManager(Context ctx) {
		// INITING CALLBACK
		final AbstractCallbackStrategy callbackStrategy = new IntervalStrategy(5000L);
		DependencyManager.setCallbackStrategy(callbackStrategy);
		callbackManager = new CallbackManager();
		DependencyManager.setCallbackManager(callbackManager);
		TracerImplHandler tracerImplHandler = new TracerImplHandler();
		DependencyManager.setTracerImplHandler(tracerImplHandler);
	}

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
				Log.e(LOG_TAG, "Failed to init broadcast receiver of class '" + bRecvEntry.getClass().getName() + "'");
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Failed to init broadcast receiver of class '" + bRecvEntry.getClass().getName() + "'");
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
		LOG_TAG = conf.getLogTag();
		AgentConfiguration.current = conf;
	}
}
