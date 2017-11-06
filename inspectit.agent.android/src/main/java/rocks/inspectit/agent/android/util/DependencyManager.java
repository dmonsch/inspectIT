package rocks.inspectit.agent.android.util;

import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.agent.android.callback.CMRConnectionManager;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.callback.strategies.AbstractCallbackStrategy;
import rocks.inspectit.agent.android.core.AndroidDataCollector;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;

/**
 * Holds the main components of the agent.
 *
 * @author David Monschein
 *
 */
public final class DependencyManager {
	/**
	 * Reference to the {@link CallbackManager}.
	 */
	private static CallbackManager callbackManager;

	/**
	 * Reference to the {@link AndroidDataCollector}.
	 */
	private static AndroidDataCollector androidDataCollector;

	private static CMRConnectionManager cmrConnectionManager;

	/**
	 * Reference to the {@link AbstractCallbackStrategy}.
	 */
	private static AbstractCallbackStrategy callbackStrategy;

	private static TracerImplHandler tracerImplHandler;

	private static Map<Class<? extends AbstractMonitoringModule>, AbstractMonitoringModule> moduleMapping;

	static {
		moduleMapping = new HashMap<>();
	}

	/**
	 * Helper class where no instance creation is allowed.
	 */
	private DependencyManager() {
	}

	/**
	 * @return the callbackManager
	 */
	public static CallbackManager getCallbackManager() {
		return callbackManager;
	}

	/**
	 * @param callbackManager
	 *            the callbackManager to set
	 */
	public static void setCallbackManager(final CallbackManager callbackManager) {
		DependencyManager.callbackManager = callbackManager;
	}

	/**
	 * @return the androidDataCollector
	 */
	public static AndroidDataCollector getAndroidDataCollector() {
		return androidDataCollector;
	}

	/**
	 * @param androidDataCollector
	 *            the androidDataCollector to set
	 */
	public static void setAndroidDataCollector(final AndroidDataCollector androidDataCollector) {
		DependencyManager.androidDataCollector = androidDataCollector;
	}

	/**
	 * @return the callbackStrategy
	 */
	public static AbstractCallbackStrategy getCallbackStrategy() {
		return callbackStrategy;
	}

	/**
	 * @param callbackStrategy
	 *            the callbackStrategy to set
	 */
	public static void setCallbackStrategy(final AbstractCallbackStrategy callbackStrategy) {
		DependencyManager.callbackStrategy = callbackStrategy;
	}

	public static AbstractMonitoringModule getModuleByClass(Class<? extends AbstractMonitoringModule> clazz) {
		return moduleMapping.get(clazz);
	}

	public static void putModule(Class<? extends AbstractMonitoringModule> clazz, AbstractMonitoringModule module) {
		moduleMapping.put(clazz, module);
	}

	/**
	 * Gets {@link #tracerImplHandler}.
	 *
	 * @return {@link #tracerImplHandler}
	 */
	public static TracerImplHandler getTracerImplHandler() {
		return tracerImplHandler;
	}

	/**
	 * Sets {@link #tracerImplHandler}.
	 *
	 * @param tracerUtil
	 *            New value for {@link #tracerImplHandler}
	 */
	public static void setTracerImplHandler(TracerImplHandler tracerUtil) {
		DependencyManager.tracerImplHandler = tracerUtil;
	}

	/**
	 * Gets {@link #cmrConnectionManager}.
	 *   
	 * @return {@link #cmrConnectionManager}  
	 */ 
	public static CMRConnectionManager getCmrConnectionManager() {
		return cmrConnectionManager;
	}

	/**  
	 * Sets {@link #cmrConnectionManager}.  
	 *   
	 * @param cmrConnectionManager  
	 *            New value for {@link #cmrConnectionManager}  
	 */
	public static void setCmrConnectionManager(CMRConnectionManager cmrConnectionManager) {
		DependencyManager.cmrConnectionManager = cmrConnectionManager;
	}
}
