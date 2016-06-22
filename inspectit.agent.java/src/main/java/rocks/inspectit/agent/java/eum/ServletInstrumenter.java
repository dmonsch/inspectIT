package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.eum.data.DataHandler;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Implementation for IServletInstrumenter.
 *
 * @author Jonas Kunz
 */
@Component
public class ServletInstrumenter implements IServletInstrumenter {

	// TODO: important: only instrument non container-core filters! Can be done by adding a
	// blacklisting / whitelisting mechanism

	/**
	 * The runtime linker for creating proxies.
	 */
	@Autowired
	private IRuntimeLinker linker;

	/**
	 * Handles the data which we get from the JS agent.
	 */
	private DataHandler dataHandler;

	/**
	 * The logger.
	 */
	@Log
	Logger log;

	/**
	 * The url to which the instrumentation script is mapped, should not overwrite any server
	 * resources.
	 */
	private static final String JAVASCRIPT_URL_PREFIX = "/wps/eumscript/";

	/**
	 * The url which gets called by our javascript for sending back the captured data.
	 */
	private static final String BEACON_URL = "/wps/contenthandler/eum_handler";

	// TODO: insert the cookie in the agent and not in a seperate file
	private static final String COOKIE_SETUP_JAVASCRIPT = "window.inspectIT_eum_cookieId = \"{{id}}\";";
	/**
	 * the script tag which will be inserted in the head section of every html document.
	 */
	private static final String SCRIPT_TAG = "\r\n<script type=\"text/javascript\" src=\"/wps/eumscript/EUMCookie.js\">\r\n</script>\r\n<script type=\"text/javascript\" src=\"/wps/eumscript/inspectit_js_agent.js\"></script>\r\n";
	/**
	 * the path to the javascript in the resources.
	 */
	private static final String SCRIPT_RESOURCE_PATH = "/js/";

	public ServletInstrumenter() {
		dataHandler = new DataHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean interceptRequest(Object servletOrFilter, Object requestObj, Object responseObj) {
		// check the types:
		if (!W_HttpServletRequest.isInstance(requestObj) || !W_HttpServletResponse.isInstance(responseObj)) {
			return false;
		}
		W_HttpServletRequest req = W_HttpServletRequest.wrap(requestObj);
		W_HttpServletResponse res = W_HttpServletResponse.wrap(responseObj);

		String path = null;
		try {
			path = new URI(req.getRequestURI()).getPath();
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
			return false;
		}

		if (path.toLowerCase().startsWith(JAVASCRIPT_URL_PREFIX.toLowerCase())) {
			String scriptPath = SCRIPT_RESOURCE_PATH + path.substring(JAVASCRIPT_URL_PREFIX.length());
			if (scriptPath.equals(SCRIPT_RESOURCE_PATH + "EUMCookie.js")) {
				// sending an dynamic generated id for cookie
				String generatedId = UUID.randomUUID().toString(); // will be unique
				String cookieSetupJS = COOKIE_SETUP_JAVASCRIPT.replace("{{id}}", generatedId);
				InputStream stringStream = new ByteArrayInputStream(cookieSetupJS.getBytes());
				sendScript(res, stringStream);
			} else {
				sendScript(res, getClass().getResourceAsStream(scriptPath));
			}
			return true;
		} else if (path.equalsIgnoreCase(BEACON_URL)) {
			// send everything ok response
			res.setStatus(200);
			res.getWriter().flush();
			recieveBeacon(req);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Receiving data from the injected javascript.
	 *
	 * @param req
	 *            the request which holds the data as parameters
	 */
	private void recieveBeacon(W_HttpServletRequest req) {
		BufferedReader reader = req.getReader();
		StringBuffer callbackData = new StringBuffer();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				callbackData.append(line);
			}
		} catch (IOException e) {
			return;
		}

		String contentData = callbackData.toString();
		dataHandler.insertBeacon(contentData);
	}

	/**
	 * Sends the script using the given response object.
	 *
	 * @param res
	 *            the response to write
	 * @param resourcePath
	 *            path to the javascript resources
	 */
	private void sendScript(W_HttpServletResponse res, InputStream resource) {
		// we respond with the script code
		res.setStatus(200);
		res.setContentType("application/javascript");
		res.setCharacterEncoding("UTF-8");
		// TODO: set the caching header
		// TODO: compression (gzip) ?
		// res.setHeader(arg0, arg1);
		InputStreamReader fr = null;
		InputStream in = resource;
		try {
			fr = new InputStreamReader(in);
			CharBuffer buf = CharBuffer.allocate(4096);
			while (fr.ready()) {
				fr.read(buf);
				res.getWriter().write(buf.array(), 0, buf.position());
				buf.position(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object instrumentResponse(Object servletOrFilter, Object httpRequestObj, Object httpResponseObj) {
		if (W_HttpServletResponse.isInstance(httpResponseObj)) {
			if (!linker.isProxyInstance(httpResponseObj, TagInjectionResponseWrapper.class)) {

				ClassLoader cl = httpResponseObj.getClass().getClassLoader();
				TagInjectionResponseWrapper wrap = new TagInjectionResponseWrapper(httpResponseObj, SCRIPT_TAG);
				Object proxy = linker.createProxy(TagInjectionResponseWrapper.class, wrap, cl);
				if (proxy == null) {
					return httpResponseObj;
				} else {
					return proxy;
				}

			}
		}
		return httpResponseObj;
	}

	/**
	 * {@inheritDoc}
	 */
	public void servletOrFilterExit(Object servletOrFilter) {
		log.info("Exited: " + servletOrFilter.getClass().getName());
	}
}
