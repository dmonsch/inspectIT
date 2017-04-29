package rocks.inspectit.agent.android.core;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;
import android.webkit.WebView;
import rocks.inspectit.agent.android.broadcast.AbstractBroadcastReceiver;
import rocks.inspectit.agent.android.broadcast.NetworkBroadcastReceiver;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.callback.strategies.AbstractCallbackStrategy;
import rocks.inspectit.agent.android.callback.strategies.IntervalStrategy;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.module.NetworkModule;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.agent.android.sensor.ISensor;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreationRequest;

/**
 * The main Android Agent class which is responsible for managing and scheduling
 * tasks.
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
	private static final String LOG_TAG = ExternalConfiguration.getLogTag();

	/**
	 * Broadcast receiver classes which will be created when the agent is
	 * initialized.
	 */
	private static final Class<?>[] BROADCAST_RECVS = new Class<?>[] { NetworkBroadcastReceiver.class };

	/**
	 * Modules which will be created when the agent is initialized.
	 */
	private static final Class<?>[] MODULES = new Class<?>[] { NetworkModule.class };

	/**
	 * Maps a certain module class to an instantiated module object.
	 */
	private static Map<Class<?>, AbstractMonitoringModule> instantiatedModules = new HashMap<Class<?>, AbstractMonitoringModule>();

	/**
	 * Maps a entry id to a specific sensor.
	 */
	private static LongSparseArray<ISensor> sensorMap = new LongSparseArray<>();

	/**
	 * Maps a class name to a class object.
	 */
	private static Map<String, ISensor> sensorClassMapping = new HashMap<>();

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
	 * Callback manager component for the communication with the server which
	 * persists the monitoring data.
	 */
	private static CallbackManager callbackManager;

	/**
	 * Network module which is responsible for capturing network monitoring
	 * data.
	 */
	private static NetworkModule networkModule;

	/**
	 * Queue of monitoring records which couldn't be sent till now because there
	 * is no session. This queue will be sent when there is an active
	 * connection.
	 */
	private static List<MobileDefaultData> defaultQueueInit = new ArrayList<>();

	/**
	 * Current entry id.
	 */
	private static long currentId;

	/**
	 * Specifies whether the agents init method has been already called or not.
	 */
	private static boolean inited = false;

	/**
	 * Specifies whether the agents destroy method has benn already called or
	 * not.
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
	public static synchronized void initAgent(final Activity ctx) {
		if (inited) {
			return;
		}

		if (ctx == null) {
			return;
		}
		// INITING VARS
		initContext = ctx;
		currentId = 0;

		Log.i(LOG_TAG, "Initing mobile agent for Android.");

		// INIT ANDROID DATA COLLECTOR
		final AndroidDataCollector androidDataCollector = new AndroidDataCollector();
		androidDataCollector.initDataCollector(ctx);
		DependencyManager.setAndroidDataCollector(androidDataCollector);

		// INITING CALLBACK
		final AbstractCallbackStrategy callbackStrategy = new IntervalStrategy(5000L);
		DependencyManager.setCallbackStrategy(callbackStrategy);

		callbackManager = new CallbackManager();
		DependencyManager.setCallbackManager(callbackManager);

		// OPEN COMMUNICATION WITH CMR
		final SessionCreationRequest helloRequest = new SessionCreationRequest();
		helloRequest.setAppName(androidDataCollector.resolveAppName());
		helloRequest.setDeviceId(androidDataCollector.getDeviceId());

		callbackManager.pushHelloMessage(helloRequest);
		for (String key : sensorClassMapping.keySet()) {
			sensorClassMapping.get(key).setCallbackManager(callbackManager);
		}

		// INITING MODULES
		for (Class<?> exModule : MODULES) {
			try {
				final AbstractMonitoringModule createdModule = (AbstractMonitoringModule) exModule.newInstance();
				instantiatedModules.put(exModule, createdModule);

				injectDependencies(createdModule);

				createdModule.initModule(ctx);
			} catch (InstantiationException e) {
				Log.e(LOG_TAG, "Failed to create module of class '" + exModule.getClass().getName() + "'");
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Failed to create module of class '" + exModule.getClass().getName() + "'");
			}
		}

		// INIT MODULE REOCCURING CALLS
		Log.i(LOG_TAG, "Creating and initializing existing modules.");
		for (Class<?> moduleEntry : MODULES) {
			final AbstractMonitoringModule module = instantiatedModules.get(moduleEntry);

			if (module instanceof NetworkModule) {
				networkModule = (NetworkModule) module;
			}

			setupScheduledMethods(module);
		}

		// INIT BROADCASTS
		Log.i(LOG_TAG, "Initializing broadcast receivers programmatically.");
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

		// SET VALUES
		inited = true;
		closed = false;

		swapInitQueues();

		Log.i(LOG_TAG, "Finished initializing the Android Agent.");
	}

	/**
	 * Shutsdown the agent. This methods suspends all modules and broadcast
	 * receivers.
	 */
	public static synchronized void destroyAgent() {
		if (closed) {
			return;
		}
		Log.i(LOG_TAG, "Shutting down the Android Agent.");

		// SHUTDOWN MODULES
		mHandler.removeCallbacksAndMessages(null);

		for (Class<?> exModule : MODULES) {
			final AbstractMonitoringModule module = instantiatedModules.get(exModule);
			if (module != null) {
				module.shutdownModule();
			}
		}

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
	 * Called from the main application when an onStart event in an activity
	 * occurs.
	 *
	 * @param activity
	 *            the activity which is "started"
	 */
	public static synchronized void onStartActivity(final Activity activity) {
		final String activityName = activity.getClass().getName();
		final String deviceId = DependencyManager.getAndroidDataCollector().getDeviceId();

		// TODO
	}

	/**
	 * Called from the main application when an onStop event in an activity
	 * occurs.
	 *
	 * @param activity
	 *            the activity which is "stopped"
	 */
	public static synchronized void onStopActivity(final Activity activity) {
		final String activityName = activity.getClass().getName();
		final String deviceId = DependencyManager.getAndroidDataCollector().getDeviceId();

		// TODO
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application.
	 *
	 * @param sensorClassName
	 *            The name of the sensor which should handle this call
	 * @param methodSignature
	 *            The signature of the method which has been called
	 * @param owner
	 *            The class which owns the method which has been called
	 * @return entry id for determine corresponding sensor at the exitBody call
	 */
	public static synchronized long enterBody(final String sensorClassName, final String methodSignature,
			final String owner) {
		final ISensor nSensor;
		if (sensorClassMapping.containsKey(sensorClassName)) {
			nSensor = sensorClassMapping.get(sensorClassName);
		} else {
			Class<?> clazz;
			try {
				clazz = Class.forName(sensorClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				Log.e(LOG_TAG, "Failed to create sensor instance.");
				return -1;
			}

			try {
				nSensor = (ISensor) clazz.newInstance();
				sensorClassMapping.put(sensorClassName, nSensor);
			} catch (InstantiationException | IllegalAccessException e) {
				Log.e(LOG_TAG, "Failed to create sensor instance.");
				return -1;
			}
		}
		nSensor.setOwner(currentId, owner); // BEFORE ALL OTHER
		nSensor.setSignature(currentId, methodSignature);

		nSensor.beforeBody(currentId);

		sensorMap.put(currentId, nSensor);

		return currentId++;
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application and is executed when a instrumented methods throws an
	 * exception.
	 *
	 * @param e
	 *            the exception which has been thrown
	 * @param enterId
	 *            the entry id for getting the responsible sensor instance
	 */
	public static synchronized void exitErrorBody(final Throwable e, final long enterId) {
		if ((enterId >= 0) && (sensorMap.indexOfKey(enterId) < 0)) {
			final ISensor eSensor = sensorMap.get(enterId);

			// call methods
			eSensor.exceptionThrown(enterId, e.getClass().getName());

			// clear
			sensorMap.remove(enterId);
		}
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application and is executed when a instrumented methods returns.
	 *
	 * @param enterId
	 *            the entry id for getting the responsible sensor instance
	 */
	public static synchronized void exitBody(final long enterId) {
		if ((enterId >= 0) && (sensorMap.indexOfKey(enterId) < 0)) {
			final ISensor eSensor = sensorMap.get(enterId);

			// call methods
			eSensor.firstAfterBody(enterId);
			eSensor.secondAfterBody(enterId);

			// clear
			sensorMap.remove(enterId);
			if (sensorMap.size() == 0) {
				currentId = 0;
			}
		}
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application uses a {@link WebView}.
	 *
	 * @param url
	 *            the url which is loaded by the webview
	 */
	public static void webViewLoad(final String url) {
		networkModule.webViewLoad(url, "GET");
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application uses a {@link WebView}.
	 *
	 * @param url
	 *            the url which is loaded by the webview
	 * @param data
	 *            the data which is sent by the post request
	 */
	public static void webViewLoadPost(final String url, final byte[] data) {
		networkModule.webViewLoad(url, "POST");
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application uses a {@link WebView}.
	 *
	 * @param url
	 *            the url which is loaded by the webview
	 * @param params
	 *            the parameters for the get request
	 */
	public static void webViewLoad(final String url, final Map<?, ?> params) {
		networkModule.webViewLoad(url, "GET");
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application creates a {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            the connection
	 */
	public static void httpConnect(final URLConnection connection) {
		networkModule.openConnection((HttpURLConnection) connection);
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application creates a {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            a reference to the created connection
	 */
	public static void httpConnect(final HttpURLConnection connection) {
		networkModule.openConnection(connection);
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application accesses the output stream of a
	 * {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            a reference to the connection
	 * @return the output stream for the connection
	 * @throws IOException
	 *             when the {@link HttpURLConnection#getOutputStream()} method
	 *             of the connection fails
	 */
	public static OutputStream httpOutputStream(final HttpURLConnection connection) throws IOException {
		return networkModule.getOutputStream(connection);
	}

	/**
	 * This method is called by code which is inserted into the original
	 * application when the application accesses the response code of a
	 * {@link HttpURLConnection}.
	 *
	 * @param connection
	 *            a reference to the connection
	 * @return the response code of the connection
	 * @throws IOException
	 *             when the {@link HttpURLConnection#getResponseCode()} method
	 *             of the connection fails
	 */
	public static int httpResponseCode(final HttpURLConnection connection) throws IOException {
		return networkModule.getResponseCode(connection);
	}

	/**
	 * Queues a monitoring record which will be sent after session creation.
	 *
	 * @param data
	 *            the record which should be queued
	 */
	public static void queueForInit(final MobileDefaultData data) {
		if (defaultQueueInit.size() < MAX_QUEUE) {
			defaultQueueInit.add(data);
		}
	}

	/**
	 * Swaps queues for Kieker records and common monitoring records and passes
	 * them to the {@link CallbackManager}.
	 */
	private static void swapInitQueues() {
		for (MobileDefaultData def : defaultQueueInit) {
			callbackManager.pushData(def);
		}

		defaultQueueInit.clear();
	}

	/**
	 * Schedules all operations for a module which should be executed
	 * periodically.
	 *
	 * @param module
	 *            a reference to the module which contains methods to schedule
	 */
	private static void setupScheduledMethods(final AbstractMonitoringModule module) {
		for (Method method : module.getClass().getMethods()) {
			if (method.isAnnotationPresent(ExecutionProperty.class)) {
				final ExecutionProperty exProp = method.getAnnotation(ExecutionProperty.class);

				// CREATE FINALS
				final long iVal = exProp.interval();
				final Method invokedMethod = method;
				final AbstractMonitoringModule receiver = module;
				final String className = module.getClass().getName();

				final Runnable loopRunnable = new Runnable() {
					@Override
					public void run() {
						try {
							invokedMethod.invoke(receiver);
							mHandler.postDelayed(this, iVal);
						} catch (IllegalAccessException e) {
							Log.e(LOG_TAG, "Failed to invoke interval method from module '" + className + "'");
						} catch (InvocationTargetException e) {
							Log.e(LOG_TAG, "Failed to invoke interval method from module '" + className + "'");
						}
					}
				};
				mHandler.postDelayed(loopRunnable, exProp.interval());
			}
		}
	}

	/**
	 * Passes references to important agent components to a broadcast receiver.
	 *
	 * @param recv
	 *            the broadcast receiver
	 */
	private static void injectDependencies(final AbstractBroadcastReceiver recv) {
		recv.setCallbackManager(DependencyManager.getCallbackManager());
		recv.setAndroidDataCollector(DependencyManager.getAndroidDataCollector());
	}

	/**
	 * Passes references to important agent components to an android module.
	 *
	 * @param androidModule
	 *            the module
	 */
	private static void injectDependencies(final AbstractMonitoringModule androidModule) {
		androidModule.setCallbackManager(DependencyManager.getCallbackManager());
		androidModule.setAndroidDataCollector(DependencyManager.getAndroidDataCollector());
	}
}
