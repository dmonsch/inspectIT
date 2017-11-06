package rocks.inspectit.agent.android.sensor;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map.Entry;

import android.util.Log;
import io.opentracing.propagation.TextMap;
import rocks.inspectit.agent.android.config.AgentConfiguration;

/**
 * @author David Monschein
 *
 */
public class HttpURLConnectionAdapter implements TextMap {

	/**
	 * Consistent log tag for the agent.
	 */
	private final String LOG_TAG;

	private HttpURLConnection connection;

	public HttpURLConnectionAdapter(HttpURLConnection connection) {
		this.connection = connection;
		this.LOG_TAG = AgentConfiguration.current.getLogTag();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Entry<String, String>> iterator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(String key, String value) {
		try {
			connection.addRequestProperty(key, value);
		} catch (IllegalStateException e) {
			// no critical problem
			Log.w(LOG_TAG, "Failed to set the request property used for distributed tracing.");
		}
	}

	public HttpURLConnection getConnection() {
		return this.connection;
	}

}
