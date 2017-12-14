package rocks.inspectit.agent.android.callback;

import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.agent.android.core.AndroidDataCollector;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;

/**
 * Manages the connection to the CMR.
 *
 * @author David Monschein
 *
 */
public class CMRConnectionManager {
	/**
	 * Time after which the Agent tries to reconnect to the CMR if there is no connection.
	 */
	private static final int RECONNECT_INTERVAL = 60000;

	/**
	 * Maximum tries for connecting to the CMR.
	 */
	private static final int RECONNECT_MAX_TRIES = 10;

	/**
	 * Maximum amount of package drops until we trigger a reconnect.
	 */
	private static final int MAX_DROPS_TILL_RECONNECT = 5;

	/**
	 * Link to the {@link CallbackManager}.
	 */
	private final CallbackManager callbackManager;

	/**
	 * Handler which is used to schedule runnables.
	 */
	private final Handler mHandler;

	/**
	 * Tag for logging.
	 */
	private final String logTag;

	/**
	 * Counts the amount of consecutive package drops.
	 */
	private int consecutiveDrops;

	/**
	 * Current number of tries to connect to the CMR.
	 */
	private int reconnectTries = 0;

	/**
	 * Creates a new CMR connection manager.
	 *
	 * @param callbackManager
	 *            reference to {@link CallbackManager} which can be used to transfer data to the CMR
	 * @param mHandler
	 *            handler for scheduling runnables
	 */
	public CMRConnectionManager(CallbackManager callbackManager, Handler mHandler) {
		this.callbackManager = callbackManager;
		this.mHandler = mHandler;
		this.logTag = AgentConfiguration.current.getLogTag();
		this.consecutiveDrops = 0;
	}

	/**
	 * Creates the connection the CMR.
	 */
	public void establishCommunication() {
		AndroidDataCollector androidDataCollector = DependencyManager.getAndroidDataCollector();
		SessionCreation helloRequest = new SessionCreation();
		helloRequest.setAppName(androidDataCollector.resolveAppName());
		helloRequest.setDeviceId(androidDataCollector.getDeviceId());

		for (Pair<String, String> tag : androidDataCollector.collectStaticTags(AgentConfiguration.current)) {
			helloRequest.putAdditionalInformation(tag.first, tag.second);
		}

		reconnectTries = 0;
		scheduleSessionCreationRequest(helloRequest);

		callbackManager.pushHelloMessage(helloRequest);
	}

	/**
	 * Report a send problem of the beacon. If the count of failed sends exceeds
	 * {@link CMRConnectionManager#MAX_DROPS_TILL_RECONNECT} a reconnect is triggered.
	 */
	protected void beaconSendProblem() {
		consecutiveDrops++;
		if (consecutiveDrops > MAX_DROPS_TILL_RECONNECT) {
			// schedule reconnection
			callbackManager.beforeReconnect();
			establishCommunication();
		}
	}

	/**
	 * Reports that a beacon has been sent successfully.
	 */
	protected void beaconSendSuccess() {
		if (consecutiveDrops > 0) {
			consecutiveDrops = 0;
		}
	}

	/**
	 * Schedules a session creation request.
	 *
	 * @param req
	 *            the session creation object
	 */
	private void scheduleSessionCreationRequest(final SessionCreation req) {
		final Runnable scheduleRunnable = new Runnable() {
			@Override
			public void run() {
				if (callbackManager.isSessionActive()) {
					reconnectTries = 0;
					return;
				} else {
					Log.w(logTag, "Couldn't create session. Retrying now.");
					if (reconnectTries < RECONNECT_MAX_TRIES) {
						Log.i(logTag, "Trying to connect to the CMR.");
						reconnectTries++;

						callbackManager.pushHelloMessage(req); // real work

						mHandler.postDelayed(this, RECONNECT_INTERVAL);
					} else {
						AndroidAgent.shutdownAgent("Max reconnect tries reached.");
					}
				}
			}
		};

		mHandler.postDelayed(scheduleRunnable, RECONNECT_INTERVAL);
	}
}
