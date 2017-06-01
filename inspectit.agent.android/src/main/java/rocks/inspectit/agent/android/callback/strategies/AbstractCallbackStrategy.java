package rocks.inspectit.agent.android.callback.strategies;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import rocks.inspectit.agent.android.callback.CallbackTask;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCallbackData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;

/**
 * Abstract class which represents a strategy for handling the data which is
 * sent back to the server.
 *
 * @author David Monschein
 *
 */
public abstract class AbstractCallbackStrategy {
	/**
	 * JSON Object mapper for serializing and deserializing JSON strings.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * {@link MobileCallbackData} which holds the data which should be sent to
	 * the server.
	 */
	protected MobileCallbackData data;

	/**
	 * Constructs a new callback strategy and creates an empty
	 * {@link MobileCallbackData} object for holding data.
	 */
	public AbstractCallbackStrategy() {
		this.data = new MobileCallbackData();
	}

	/**
	 * Adds data to the data holder.
	 *
	 * @param dat
	 *            data which should be added
	 */
	public abstract void addData(MobileDefaultData dat);

	/**
	 * Stops the callback strategy.
	 */
	public abstract void stop();

	/**
	 * Immediately sends a data object to the server with not respecting the
	 * strategy.
	 *
	 * @param dat
	 *            data which should be sent immediately
	 * @param izHello
	 *            whether it is a session creation message or not
	 */
	public void sendImmediately(final MobileCallbackData dat, final boolean izHello) {
		this.sendBeacon(dat, izHello);
		data.clear();
	}

	/**
	 * Flushes all records gathered at the moment to the output REST interface.
	 */
	public void flush() {
		// send data currently in the buffer
		this.sendBeacon();
	}

	/**
	 * Sets the session id.
	 *
	 * @param id
	 *            session id
	 */
	public void setSessId(final String id) {
		this.data.setSessionId(id);
	}

	/**
	 * Sends the data to the server.
	 */
	public synchronized void sendBeacon() {
		this.sendBeacon(this.data, false);
		data.clear();
	}

	/**
	 * Sends a specified beacon to the server.
	 *
	 * @param dat
	 *            beacon
	 * @param helloReq
	 *            whether it is a session creation message or not
	 */
	private void sendBeacon(final MobileCallbackData dat, final boolean helloReq) {
		if ((dat != null) && (dat.getChildData().size() > 0)) {
			final String callbackUrl;
			if (helloReq) {
				callbackUrl = AgentConfiguration.current.getSessionUrl();
			} else {
				callbackUrl = AgentConfiguration.current.getBeaconUrl();
			}

			try {
				new CallbackTask(callbackUrl).execute(mapper.writeValueAsString(dat));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
