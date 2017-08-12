package rocks.inspectit.agent.android.module.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import io.opentracing.SpanContext;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.module.AbstractMonitoringModule;
import rocks.inspectit.agent.android.module.util.ConnectionState;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.NetRequestResponse;

/**
 * Special module which handles the instrumented network requests.
 *
 * @author David Monschein
 *
 */
public class NetworkModule extends AbstractMonitoringModule {

	private static final int DELETE_AFTER = 120000;
	/**
	 * Maps a single connection to a specific connection state.
	 */
	private Map<HttpURLConnection, ConnectionState> connectionStateMap;

	/**
	 * Tracer implementation.
	 */
	private TracerImplHandler tracer;

	/**
	 * Creates a new default instance.
	 */
	public NetworkModule() {
		this.tracer = DependencyManager.getTracerImplHandler();
	}

	/**
	 * Executed when a connection is opened. This creates a new entry in the state map for the
	 * connection.
	 *
	 * @param conn
	 *            the connection which has been created
	 */
	public void openConnection(HttpURLConnection conn) {
		if (!this.connectionStateMap.containsKey(conn)) {
			final ConnectionState connState = new ConnectionState();
			connState.setNetworkConnectionType(this.androidDataCollector.getNetworkConnectionType());
			connState.update(ConnectionState.ConnectionPoint.CONNECT);
			this.connectionStateMap.put(conn, connState);

			// set opentracing property
			SpanContext currentContext = tracer.getCurrentContext();
			if (currentContext != null) {
				conn.setRequestProperty("Span-Context", currentContext.toString());
			}
		}
	}

	/**
	 * Executed when the response code of a connection is retrieved. This updates the connection
	 * state for the belonging connection.
	 *
	 * @param conn
	 *            the connection
	 * @return the response code
	 * @throws IOException
	 *             thrown when {@link HttpURLConnection#getResponseCode()} throws an exception
	 */
	public int getResponseCode(HttpURLConnection conn) throws IOException {
		if (connectionStateMap.containsKey(conn)) {
			final int respCode = conn.getResponseCode();
			connectionStateMap.get(conn).update(ConnectionState.ConnectionPoint.RESPONSECODE);
			return respCode;
		} else {
			return conn.getResponseCode();
		}
	}

	/**
	 * Executed when the output stream of a connection is requested. This updates the connection
	 * state for the belonging connection.
	 *
	 * @param conn
	 *            the connection
	 * @return the output stream for the connection
	 * @throws IOException
	 *             thrown when {@link HttpURLConnection#getOutputStream()}} throws an exception
	 */
	public OutputStream getOutputStream(HttpURLConnection conn) throws IOException {
		if (connectionStateMap.containsKey(conn)) {
			final OutputStream out = conn.getOutputStream();
			connectionStateMap.get(conn).update(ConnectionState.ConnectionPoint.OUTPUT);
			return out;
		} else {
			return conn.getOutputStream();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(Context ctx) {
		connectionStateMap = new HashMap<HttpURLConnection, ConnectionState>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdownModule() {
		this.collectData();
	}

	/**
	 * Executed every 60 seconds and looks whether there are closed connections and then passes data
	 * to the {@link CallbackManager} about the connection.
	 */
	@ExecutionProperty(interval = 60000L)
	public void collectData() {
		final List<HttpURLConnection> removedConns = new ArrayList<HttpURLConnection>();

		for (HttpURLConnection conn : connectionStateMap.keySet()) {
			ConnectionState state = connectionStateMap.get(conn);
			boolean finished = false;

			if (state.probablyFinished()) {
				long startStamp = state.getPointTimestamp(ConnectionState.ConnectionPoint.CONNECT);
				long responseStamp = state.getPointTimestamp(ConnectionState.ConnectionPoint.RESPONSECODE);

				try {
					int responseCode = conn.getResponseCode();
					String url = conn.getURL() != null ? conn.getURL().toString() : "";
					String method = conn.getRequestMethod();

					// LOG IT
					final NetRequestResponse response = new NetRequestResponse();
					response.setDuration(responseStamp - startStamp);
					response.setMethod(method);
					response.setUrl(url);
					response.setResponseCode(responseCode);
					response.setConnectionType(state.getNetworkConnectionType());

					this.pushData(response);
					finished = true;
				} catch (IOException e) {
					Log.w(AgentConfiguration.current.getLogTag(), "There was a problem with monitoring a network request.");
				}
			}

			if (finished || (state.getLastUpdatedDiff() >= DELETE_AFTER)) {
				removedConns.add(conn);
			}
		}

		for (HttpURLConnection toRemove : removedConns) {
			connectionStateMap.remove(toRemove);
		}
	}
}
