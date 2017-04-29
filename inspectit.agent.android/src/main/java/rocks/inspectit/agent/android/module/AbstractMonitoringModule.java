package rocks.inspectit.agent.android.module;

import android.content.Context;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.core.AndroidDataCollector;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;

/**
 * Abstract Android module which has an init and an exit point.
 * 
 * @author David Monschein
 *
 */
public abstract class AbstractMonitoringModule {

	/**
	 * Link to the {@link AndroidDataCollector} component.
	 */
	protected AndroidDataCollector androidDataCollector;

	/**
	 * Link to the {@link CallbackManager} component.
	 */
	private CallbackManager callbackManager;

	/**
	 * Initializes the module with a given context.
	 * 
	 * @param ctx
	 *            context of the application
	 */
	public abstract void initModule(Context ctx);

	/**
	 * Shuts the module down.
	 */
	public abstract void shutdownModule();

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
