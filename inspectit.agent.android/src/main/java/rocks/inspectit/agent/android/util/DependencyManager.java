package rocks.inspectit.agent.android.util;

import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.callback.strategies.AbstractCallbackStrategy;
import rocks.inspectit.agent.android.core.AndroidDataCollector;

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

	/**
	 * Reference to the {@link AbstractCallbackStrategy}.
	 */
	private static AbstractCallbackStrategy callbackStrategy;

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
}
