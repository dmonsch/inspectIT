package rocks.inspectit.agent.android.broadcast;

import android.content.BroadcastReceiver;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.core.AndroidDataCollector;
import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Abstract class for setting up a broadcast receiver for Android.
 *
 * @author David Monschein
 *
 */
public abstract class AbstractBroadcastReceiver extends BroadcastReceiver {
	/**
	 * The data collector.
	 */
	protected AndroidDataCollector androidDataCollector;

	/**
	 * The callback manager.
	 */
	private CallbackManager callbackManager;

	/**
	 * Defines for which actions the receiver should be broadcasted.
	 *
	 * @return action types which should be reported
	 */
	public abstract String[] getFilterActions();

	public abstract void onStart();

	public abstract void onStop();

	/**
	 * Sets the {@link AndroidDataCollector}.
	 *
	 * @param androidDataCollector
	 *            data collector instance
	 */
	public void setAndroidDataCollector(AndroidDataCollector androidDataCollector) {
		this.androidDataCollector = androidDataCollector;
	}

	/**
	 * Sets the {@link CallbackManager}}.
	 *
	 * @param callbackManager
	 *            callbck manager instance
	 */
	public void setCallbackManager(CallbackManager callbackManager) {
		this.callbackManager = callbackManager;
	}

	/**
	 * Pushes data to the callback manager which is responsible for further
	 * tasks.
	 *
	 * @param data
	 *            the data which should be passed to the callback manager.
	 */
	protected void pushData(DefaultData data) {
		callbackManager.pushData(data);
	}
}
