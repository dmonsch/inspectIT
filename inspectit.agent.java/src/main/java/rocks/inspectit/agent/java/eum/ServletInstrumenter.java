package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.eum.data.DataHandler;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Implementation for IServletInstrumenter.
 *
 * @author Jonas Kunz
 */
@Component
public class ServletInstrumenter implements IServletInstrumenter, InitializingBean {
	/**
	 * The runtime linker for creating proxies.
	 */
	@Autowired
	private IRuntimeLinker linker;

	/**
	 * The default core service.
	 */
	@Autowired
	private ICoreService coreService;

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
	private static final String JAVASCRIPT_URL_PREFIX = "/wps/eumscript/inspectit_jsagent_";

	/**
	 * The url which gets called by our javascript for sending back the captured data.
	 */
	private static final String BEACON_URL = "/wps/contenthandler/eum_handler";
	/**
	 * the script tag which will be inserted in the head section of every html document.
	 */
	private static final String SCRIPT_TAG = "<script type=\"text/javascript\" src=\"/wps/eumscript/inspectit_jsagent_ablr12.js\"></script>\r\n";

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
			String scriptArgumentsWithEnding = path.substring(JAVASCRIPT_URL_PREFIX.length());
			String scriptArgumentsNoEnding = scriptArgumentsWithEnding.substring(0, scriptArgumentsWithEnding.lastIndexOf('.'));
			sendScript(res, JSAgentBuilder.buildJsFile(scriptArgumentsNoEnding));
			return true;
		} else if (path.equalsIgnoreCase(BEACON_URL)) {
			// send everything ok response
			res.setStatus(200);
			res.getWriter().flush();
			receiveBeacon(req);
			return true;
		} else {
			log.info("Entered: " + servletOrFilter.getClass().getName());
			return false;
		}

	}

	/**
	 * Receiving data from the injected javascript.
	 *
	 * @param req
	 *            the request which holds the data as parameters
	 */
	private void receiveBeacon(W_HttpServletRequest req) {
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
	 * @param resource
	 *            stream from the resource.
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
			while (fr.read(buf) != -1) {
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

	/**
	 * {@inheritDoc}
	 */
	public Object postProcessAfterInitialization(Object arg0, String arg1) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		dataHandler = new DataHandler(coreService);
	}
}
