package rocks.inspectit.agent.android.sensor;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;

/**
 * @author David Monschein
 *
 */
public class HttpURLConnectionAdapter implements TextMap {

	private HttpURLConnection connection;

	public HttpURLConnectionAdapter(HttpURLConnection connection) {
		this.connection = connection;
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
		connection.setRequestProperty(key, value);
	}

	public HttpURLConnection getConnection() {
		return this.connection;
	}

}
