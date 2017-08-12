package rocks.inspectit.agent.android.module.net;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @author David Monschein
 *
 */
public class HttpURLConnectionProxy extends HttpURLConnection {

	private HttpURLConnection proxyLink;

	public HttpURLConnectionProxy(HttpURLConnection conn) {
		super(conn.getURL());
		this.proxyLink = conn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		proxyLink.disconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean usingProxy() {
		return proxyLink.usingProxy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect() throws IOException {
		proxyLink.connect();
	}

}
