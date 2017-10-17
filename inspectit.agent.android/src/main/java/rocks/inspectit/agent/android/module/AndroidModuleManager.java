package rocks.inspectit.agent.android.module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.agent.android.util.DependencyManager;

/**
 * @author David Monschein
 *
 */
public class AndroidModuleManager {

	/**
	 * Modules which will be created when the agent is initialized.
	 */
	private static final Class<?>[] MODULES = new Class<?>[] { CrashModule.class, SystemResourcesModule.class, CoreSpanReporter.class };

	/**
	 * Maps a certain module class to an instantiated module object.
	 */
	private static List<AbstractMonitoringModule> instantiatedModules = new ArrayList<>();

	private final String LOG_TAG;
	private final Context applicationContext;
	private final Handler mHandler;

	public AndroidModuleManager(Context ctx, Handler mHandler) {
		LOG_TAG = AgentConfiguration.current.getLogTag();
		this.applicationContext = ctx;
		this.mHandler = mHandler;
	}

	public void initModules() {
		// INITING MODULES
		for (Class<?> exModule : MODULES) {
			try {
				final AbstractMonitoringModule createdModule = (AbstractMonitoringModule) exModule.newInstance();
				instantiatedModules.add(createdModule);
				injectDependencies(createdModule);
				createdModule.initModule(applicationContext);
			} catch (InstantiationException e) {
				Log.e(LOG_TAG, "Failed to create module of class '" + exModule.getClass().getName() + "'");
			} catch (IllegalAccessException e) {
				Log.e(LOG_TAG, "Failed to create module of class '" + exModule.getClass().getName() + "'");
			}
		}

		// INIT MODULE REOCCURING CALLS
		Log.i(LOG_TAG, "Creating and initializing existing modules.");
		for (AbstractMonitoringModule module : instantiatedModules) {
			setupScheduledMethods(module);
		}
	}

	public void shutdownModules() {
		for (AbstractMonitoringModule module : instantiatedModules) {
			if (module != null) {
				module.shutdownModule();
			}
		}
	}

	public List<AbstractMonitoringModule> getModules() {
		return instantiatedModules;
	}

	/**
	 * Schedules all operations for a module which should be executed periodically.
	 *
	 * @param module
	 *            a reference to the module which contains methods to schedule
	 */
	private void setupScheduledMethods(AbstractMonitoringModule module) {
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
	 * Passes references to important agent components to an android module.
	 *
	 * @param androidModule
	 *            the module
	 */
	private void injectDependencies(final AbstractMonitoringModule androidModule) {
		androidModule.setCallbackManager(DependencyManager.getCallbackManager());
		androidModule.setAndroidDataCollector(DependencyManager.getAndroidDataCollector());
	}

}
