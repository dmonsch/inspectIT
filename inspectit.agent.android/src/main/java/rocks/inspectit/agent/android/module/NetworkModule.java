package rocks.inspectit.agent.android.module;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.webkit.WebView;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.module.util.ConnectionState;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.shared.all.communication.data.mobile.NetRequestResponse;

/**
 * Special module which handles the instrumented network requests.
 * 
 * @author David Monschein
 *
 */
public class NetworkModule extends AbstractMonitoringModule {
	/**
	 * Time after which the existing connections get collected and sent to the
	 * monitoring server.
	 */
	private final long collectAfter;

	/**
	 * Maps a single connection to a specific connection state.
	 */
	private Map<HttpURLConnection, ConnectionState> connectionStateMap;

	/**
	 * Creates a new default instance.
	 */
	public NetworkModule() {
		this(30000L);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param collectionInterval
	 *            Time after which the existing connections get collected and
	 *            sent to the monitoring server.
	 */
	public NetworkModule(final long collectionInterval) {
		this.collectAfter = collectionInterval;
	}

	/**
	 * Executed when a connection is opened. This creates a new entry in the
	 * state map for the connection.
	 * 
	 * @param conn
	 *            the connection which has been created
	 */
	public void openConnection(final HttpURLConnection conn) {
		if (!this.connectionStateMap.containsKey(conn)) {
			final ConnectionState connState = new ConnectionState();
			connState.update(ConnectionState.ConnectionPoint.CONNECT);
			this.connectionStateMap.put(conn, connState);
		}
	}

	/**
	 * Executed when the response code of a connection is retrieved. This
	 * updates the connection state for the belonging connection.
	 * 
	 * @param conn
	 *            the connection
	 * @return the response code
	 * @throws IOException
	 *             thrown when {@link HttpURLConnection#getResponseCode()}
	 *             throws an exception
	 */
	public int getResponseCode(final HttpURLConnection conn) throws IOException {
		if (connectionStateMap.containsKey(conn)) {
			final int respCode = conn.getResponseCode();
			connectionStateMap.get(conn).update(ConnectionState.ConnectionPoint.RESPONSECODE);
			return respCode;
		} else {
			return conn.getResponseCode();
		}
	}

	/**
	 * Executed when the output stream of a connection is requested. This
	 * updates the connection state for the belonging connection.
	 * 
	 * @param conn
	 *            the connection
	 * @return the output stream for the connection
	 * @throws IOException
	 *             thrown when {@link HttpURLConnection#getOutputStream()}}
	 *             throws an exception
	 */
	public OutputStream getOutputStream(final HttpURLConnection conn) throws IOException {
		if (connectionStateMap.containsKey(conn)) {
			final OutputStream out = conn.getOutputStream();
			connectionStateMap.get(conn).update(ConnectionState.ConnectionPoint.OUTPUT);
			return out;
		} else {
			return conn.getOutputStream();
		}
	}

	/**
	 * Executed when the original application wants a {@link WebView} to load a
	 * specific URL.
	 * 
	 * @param url
	 *            the URL which is passed to the {@link WebView}
	 * @param method
	 *            the method of the request (either GET or POST)
	 */
	public void webViewLoad(final String url, final String method) {
		final NetRequestResponse netRequest = new NetRequestResponse();

		netRequest.setDeviceId(androidDataCollector.getDeviceId());
		netRequest.setDuration(0);
		netRequest.setMethod(method);
		netRequest.setResponseCode(200);
		netRequest.setUrl(url);

		this.pushData(netRequest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(final Context ctx) {
		connectionStateMap = new HashMap<HttpURLConnection, ConnectionState>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdownModule() {
	}

	/**
	 * Executed every 60 seconds and looks whether there are closed connections
	 * and then passes data to the {@link CallbackManager} about the connection.
	 */
	@ExecutionProperty(interval = 60000L)
	public void collectData() {
		final List<HttpURLConnection> removedConns = new ArrayList<HttpURLConnection>();

		for (HttpURLConnection conn : connectionStateMap.keySet()) {
			final ConnectionState state = connectionStateMap.get(conn);
			if (state.getLastUpdatedDiff() >= collectAfter) {
				if (state.probablyFinished()) {
					final long startStamp = state.getPointTimestamp(ConnectionState.ConnectionPoint.CONNECT);
					final long responseStamp = state.getPointTimestamp(ConnectionState.ConnectionPoint.RESPONSECODE);

					try {
						final int responseCode = conn.getResponseCode();
						final String url = conn.getURL().toString();
						final String method = conn.getRequestMethod();

						// LOG IT
						final NetRequestResponse response = new NetRequestResponse();
						response.setDuration(responseStamp - startStamp);
						response.setMethod(method);
						response.setUrl(url);
						response.setResponseCode(responseCode);
						response.setDeviceId(androidDataCollector.getDeviceId());

						this.pushData(response);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				removedConns.add(conn);
			}
		}

		for (HttpURLConnection toRemove : removedConns) {
			connectionStateMap.remove(toRemove);
		}
	}
}
