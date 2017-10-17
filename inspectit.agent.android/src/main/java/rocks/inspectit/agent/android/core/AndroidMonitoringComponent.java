package rocks.inspectit.agent.android.core;

import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.shared.android.mobile.MobileDefaultData;

/**
 * @author David Monschein
 *
 */
public class AndroidMonitoringComponent {

	/**
	 * Link to the {@link AndroidDataCollector} component.
	 */
	protected AndroidDataCollector androidDataCollector;

	/**
	 * Link to the {@link CallbackManager} component.
	 */
	private CallbackManager callbackManager;

	/**
	 * Sets the {@link AndroidDataCollector}}.
	 *
	 * @param androidDataCollector
	 *            the data collector to set
	 */
	public void setAndroidDataCollector(final AndroidDataCollector androidDataCollector) {
		this.androidDataCollector = androidDataCollector;
	}

	/**
	 * Sets the {@link CallbackManager}.
	 *
	 * @param callbackManager
	 *            the callback manager to set
	 */
	public void setCallbackManager(final CallbackManager callbackManager) {
		this.callbackManager = callbackManager;
	}

	/**
	 * Pushes data to the {@link CallbackManager}.
	 *
	 * @param data
	 *            data which should be passed to the callback manager
	 */
	protected void pushData(final MobileDefaultData data) {
		callbackManager.pushData(data);
	}

}
