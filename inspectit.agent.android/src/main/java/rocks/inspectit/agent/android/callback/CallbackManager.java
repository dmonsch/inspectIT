package rocks.inspectit.agent.android.callback;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import rocks.inspectit.agent.android.callback.strategies.AbstractCallbackStrategy;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCallbackData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreationRequest;

/**
 * Component which handles the connection to the server which persists the
 * monitoring data.
 *
 * @author David Monschein
 *
 */
public class CallbackManager {
	/**
	 * Consistent log tag for the agent.
	 */
	private static final String LOG_TAG = AgentConfiguration.current.getLogTag();

	/**
	 * The callback strategy which is used to send data.
	 */
	private AbstractCallbackStrategy strategy = DependencyManager.getCallbackStrategy();

	/**
	 * Whether there is an existing session.
	 */
	private boolean sessActive;

	/**
	 * Contains all data which isn't sent already because there is no
	 * connection.
	 */
	private List<MobileDefaultData> sessQueue;

	/**
	 * Creates a new callback manager.
	 */
	public CallbackManager() {
		this.sessActive = false;
		this.sessQueue = new ArrayList<MobileDefaultData>();
	}

	/**
	 * Pass data to the callback manager and the manager manages the
	 * transmission to the server.
	 *
	 * @param data
	 *            data which should be transferred to the server
	 */
	public void pushData(final MobileDefaultData data) {
		if (!sessActive) {
			this.sessQueue.add(data);
		} else {
			this.strategy.addData(data);
		}
	}

	/**
	 * Pushes a hello message for session creation.
	 *
	 * @param response
	 *            hello request which should be sent to the servers
	 */
	public void pushHelloMessage(final SessionCreationRequest response) {
		final MobileCallbackData data = new MobileCallbackData();
		data.setSessionId(null);

		final List<MobileDefaultData> childs = new ArrayList<MobileDefaultData>();
		childs.add(response);

		data.setChildData(childs);

		// directly send it
		strategy.sendImmediately(data, true);
	}

	/**
	 * Shuts down the callback manager.
	 */
	public void shutdown() {
		strategy.flush();
		strategy.stop();
	}

	/**
	 * Flushes all entries in the session queue to the callback strategy.
	 */
	private void swapQueue() {
		for (MobileDefaultData data : sessQueue) {
			strategy.addData(data);
		}
		sessQueue.clear();
	}

	/**
	 * Applies a given session id which is used to communicate with the server.
	 *
	 * @param id
	 *            session id
	 */
	public void applySessionId(final String id) {
		if (!sessActive) {
			Log.i(LOG_TAG, "Created session with id '" + id + "' for communicating with the CMR.");

			strategy.setSessId(id);
			sessActive = true;

			swapQueue();
		}
	}
}
