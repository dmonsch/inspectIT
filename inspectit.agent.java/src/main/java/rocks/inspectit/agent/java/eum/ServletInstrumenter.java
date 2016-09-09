package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.eum.data.DataHandler;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
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
	private static final String BEACON_SUB_PATH = "inspectIT_beacon_handler";

	private String completeBeaconURL;
	private String completeJavascriptURLPrefix;
	private String completeScriptTags;


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

		if (path.toLowerCase().startsWith(completeJavascriptURLPrefix.toLowerCase())) {
			String scriptArgumentsWithEnding = path.substring(completeJavascriptURLPrefix.length());
			// remove revision and ignore it, we always send the newest version
			scriptArgumentsWithEnding = scriptArgumentsWithEnding.substring(scriptArgumentsWithEnding.indexOf('_') + 1);
			String scriptArgumentsNoEnding = scriptArgumentsWithEnding.substring(0, scriptArgumentsWithEnding.lastIndexOf('.'));
			sendScript(res, JSAgentBuilder.buildJsFile(scriptArgumentsNoEnding));
			return true;
		} else if (path.equalsIgnoreCase(completeBeaconURL)) {
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
	 * @param scriptSource
	 *            the source of the script to send.
	 */
	private void sendScript(W_HttpServletResponse res, String scriptSource) {
		// we respond with the script code
		res.setStatus(200);
		res.setContentType("application/javascript");
		res.addHeader("Cache-Control", "public, max-age=" + JSAgentBuilder.JS_AGENT_CACHE_MAX_AGE_SECONDS);

		PrintWriter writer = res.getWriter();
		writer.write(scriptSource);
		writer.flush();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object instrumentResponse(Object servletOrFilter, Object httpRequestObj, Object httpResponseObj) {
		if (W_HttpServletResponse.isInstance(httpResponseObj)) {
			if (!linker.isProxyInstance(httpResponseObj, TagInjectionResponseWrapper.class)) {

				Object sessionIdCookie = generateSessionIDCookie(httpRequestObj);


				ClassLoader cl = httpResponseObj.getClass().getClassLoader();
				TagInjectionResponseWrapper wrap = new TagInjectionResponseWrapper(httpResponseObj, sessionIdCookie, completeScriptTags);
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
		completeBeaconURL = buildBeaconUrl();
		completeJavascriptURLPrefix = buildJavascriptPrefix();
		completeScriptTags = buildScriptTag();
	}

	private String buildBeaconUrl() {
		String base = configurationStorage.getEndUserMonitoringConfig().getScriptBaseUrl();
		if (!base.endsWith("/")) {
			base += "/";
		}
		return base + BEACON_SUB_PATH;
	}


	private String buildJavascriptPrefix() {
		String base = configurationStorage.getEndUserMonitoringConfig().getScriptBaseUrl();
		if (!base.endsWith("/")) {
			base += "/";
		}
		return base + JAVASCRIPT_URL_PREFIX;
	}

	private String buildScriptTag() {
		StringBuilder tags = new StringBuilder();
		tags.append("<script type=\"text/javascript\">\r\n");
		tags.append("window.inspectIT_settings = {\r\n");
		tags.append("eumManagementServer : \"").append(completeBeaconURL).append("\"\r\n");
		tags.append("};\r\n");
		tags.append("</script>\r\n");
		tags.append("<script type=\"text/javascript\" src=\"");
		tags.append(completeJavascriptURLPrefix);
		tags.append(JSAgentModule.JS_AGENT_REVISION).append("_");
		tags.append(configurationStorage.getEndUserMonitoringConfig().getActiveModules());
		tags.append(".js\"></script>\r\n");
		return tags.toString();
	}
}
