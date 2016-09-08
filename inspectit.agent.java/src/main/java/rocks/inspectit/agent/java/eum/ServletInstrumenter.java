package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
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
	 * Configuration storage to read settings from.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

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
	private static final String JAVASCRIPT_URL_PREFIX = "inspectit_jsagent_";

	/**
	 * The url which gets called by our javascript for sending back the captured data.
	 */
	private static final String BEACON_SUB_PATH = "eum_handler";

	
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
		
		String jsScriptPrefix = getJavascriptPrefix();

		if (path.toLowerCase().startsWith(jsScriptPrefix.toLowerCase())) {
			String scriptArgumentsWithEnding = path.substring(jsScriptPrefix.length());
			String scriptArgumentsNoEnding = scriptArgumentsWithEnding.substring(0, scriptArgumentsWithEnding.lastIndexOf('.'));
			sendScript(res, JSAgentBuilder.buildJsFile(scriptArgumentsNoEnding));
			return true;
		} else if (path.equalsIgnoreCase(getBeaconUrl())) {
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

				Object sessionIdCookie = generateSessionIDCookie(httpRequestObj);


				ClassLoader cl = httpResponseObj.getClass().getClassLoader();
				TagInjectionResponseWrapper wrap = new TagInjectionResponseWrapper(httpResponseObj, sessionIdCookie, getScriptTag());
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
	 * @param httpRequestObj
	 */
	private Object generateSessionIDCookie(Object httpRequestObj) {

		// check if it already has an id set
		W_HttpServletRequest request = W_HttpServletRequest.wrap(httpRequestObj);
		Object[] cookies = request.getCookies();
		if (cookies != null) {
			for (Object cookieObj : cookies) {
				W_Cookie cookie = W_Cookie.wrap(cookieObj);
				if (cookie.getName().equals(JSAgentBuilder.SESSION_ID_COOKIE_NAME)) {
					return null;
				}
			}
		}

		String id = generateUEMId();

		//otherweise generate the cookie
		Object cookie = W_Cookie.newInstance(httpRequestObj.getClass().getClassLoader(), JSAgentBuilder.SESSION_ID_COOKIE_NAME, id);
		W_Cookie wrappedCookie = W_Cookie.wrap(cookie);
		wrappedCookie.setMaxAge(JSAgentBuilder.SESSION_COOKIE_MAX_AGE_SECONDS);
		wrappedCookie.setPath("/");
		return cookie;
	}

	/**
	 *
	 */
	private String generateUEMId() {
		return UUID.randomUUID().toString(); // will be unique
	}

	private String getBeaconUrl() {
		String base = configurationStorage.getEndUserMonitoringConfig().getScriptBaseUrl();
		if (!base.endsWith("/")) {
			base += "/";
		}
		return base + BEACON_SUB_PATH;
	}


	private String getJavascriptPrefix() {
		String base = configurationStorage.getEndUserMonitoringConfig().getScriptBaseUrl();
		if (!base.endsWith("/")) {
			base += "/";
		}
		return base + JAVASCRIPT_URL_PREFIX;
	}

	private String getScriptTag() {
		StringBuilder tags = new StringBuilder();
		tags.append("<script type=\"text/javascript\">\r\n");
		tags.append("window.inspectIT_settings = {\r\n");
		tags.append("eumManagementServer : \"").append(getBeaconUrl()).append("\"\r\n");
		tags.append("};\r\n");
		tags.append("</script>\r\n");
		tags.append("<script type=\"text/javascript\" src=\"").append(getJavascriptPrefix()).append("a12").append(".js\"></script>\r\n");
		return tags.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public void servletOrFilterExit(Object servletOrFilter) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		dataHandler = new DataHandler(coreService);
	}
}
