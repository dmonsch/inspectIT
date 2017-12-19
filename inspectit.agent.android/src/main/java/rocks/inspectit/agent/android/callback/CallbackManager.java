package rocks.inspectit.agent.android.callback;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import rocks.inspectit.agent.android.callback.strategies.AbstractCallbackStrategy;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCallbackData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;

/**
 * Component which handles the connection to the CMR which persists the monitoring data.
 *
 * @author David Monschein
 *
 */
public class CallbackManager {
	/**
	 * Consistent log tag for the agent.
	 */
	private final String logTag;

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
	 * Contains all mobile spans which could'nt be sent to the CMR because there is no active
	 * connection.
	 */
	private List<MobileSpan> sessQueueSpans;

	/**
	 * Creates a new callback manager.
	 */
	public CallbackManager() {
		this.sessActive = false;
		this.sessQueue = new ArrayList<MobileDefaultData>();
		this.sessQueueSpans = new ArrayList<MobileSpan>();
		this.logTag = AgentConfiguration.current.getLogTag();
	}

	/**
	 * Forces the send of all collected monitoring data.
	 */
	public void forceSend() {
		this.strategy.sendBeacon();
	}

	/**
	 * Pass data to the callback manager and the manager manages the
	 * transmission to the server.
	 *
	 * @param data
	 *            data which should be transferred to the server
	 */
	public void pushData(MobileDefaultData data) {
		if (!sessActive) {
			this.sessQueue.add(data);
		} else {
			this.strategy.addData(data);
		}
	}

	/**
	 * Passes a span, which has been propagated on a mobile device, to the callback strategy.
	 *
	 * @param data
	 *            the span which should be transferred to the CMR
	 */
	public void pushData(MobileSpan data) {
		if (!sessActive) {
			this.sessQueueSpans.add(data);
		} else {
			this.strategy.addData(data);
		}
	}

	/**
	 * Pushes a hello message for session creation.
	 *
	 * @param request
	 *            hello request which should be sent to the servers
	 */
	public void pushHelloMessage(SessionCreation request) {
		final MobileCallbackData data = new MobileCallbackData();
		data.setSessionId(null);

		List<MobileDefaultData> childs = new ArrayList<MobileDefaultData>();
		childs.add(request);

		data.setChildData(childs);

		// directly send it
		strategy.sendImmediately(data, true);
	}

	/**
	 * Determines if we have an active session for communicating with the CMR.
	 *
	 * @return true if there is an active connection - false otherwise
	 */
	public boolean isSessionActive() {
		return sessActive;
	}

	/**
	 * Shuts down the callback manager.
	 */
	public void shutdown() {
		strategy.flush();
		strategy.stop();
	}

	/**
	 * Invalidates the current session.
	 */
	public void beforeReconnect() {
		sessActive = false;
	}

	/**
	 * Flushes all entries in the session queue to the callback strategy.
	 */
	private void swapQueue() {
		for (MobileDefaultData data : sessQueue) {
			strategy.addData(data);
		}
		for (MobileSpan data : sessQueueSpans) {
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
	public void applySessionId(String id) {
		if (!sessActive) {
			Log.i(logTag, "Created session with id '" + id + "' for communicating with the CMR.");

			strategy.setSessId(id);
			sessActive = true;

			swapQueue();
		}
	}
}
