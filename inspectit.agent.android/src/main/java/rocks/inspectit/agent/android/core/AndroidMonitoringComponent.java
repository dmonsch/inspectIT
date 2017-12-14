package rocks.inspectit.agent.android.core;

import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;

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
	public void setAndroidDataCollector(AndroidDataCollector androidDataCollector) {
		this.androidDataCollector = androidDataCollector;
	}

	/**
	 * Sets the {@link CallbackManager}.
	 *
	 * @param callbackManager
	 *            the callback manager to set
	 */
	public void setCallbackManager(CallbackManager callbackManager) {
		this.callbackManager = callbackManager;
	}

	/**
	 * Pushes data to the {@link AndroidMonitoringComponent#callbackManager}.
	 *
	 * @param data
	 *            data which should be passed to the callback manager
	 */
	protected void pushData(MobileDefaultData data) {
		callbackManager.pushData(data);
	}

	/**
	 * Pushed a mobile span to the {@link AndroidMonitoringComponent#callbackManager}.
	 *
	 * @param data
	 *            span which should be passed to the callback manager.
	 */
	protected void pushData(MobileSpan data) {
		callbackManager.pushData(data);
	}

}
