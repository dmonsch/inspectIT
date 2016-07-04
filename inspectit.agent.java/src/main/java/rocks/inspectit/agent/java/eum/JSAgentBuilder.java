package rocks.inspectit.agent.java.eum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Helper class for creating a javascript agent which only has some specified modules (plugins).
 *
 * @author David Monschein
 *
 */
public final class JSAgentBuilder {

	/**
	 * Maps argument chars (strings with one character) to plugin files.
	 */
	private static final Map<String, String> ARGUMENT_FILE_MAPPING = new HashMap<String, String>() {
		/**
		 * Generated UID.
		 */
		private static final long serialVersionUID = -6495278113683161832L;

		{
			put("a", "plugins/ajax.js");
			put("b", "plugins/async.js");
			put("l", "plugins/listener.js");
			put("r", "plugins/rum-speedindex.js");
			put("1", "plugins/navtimings.js");
			put("2", "plugins/restimings.js");
		}
	};

	/**
	 * The javascript for setting the session id.
	 */
	private static final String COOKIE_SETUP_JAVASCRIPT = "window.inspectIT_eum_cookieId = \"{{id}}\";\r\n";

	/**
	 * Javascript code which starts the execution at the end when all plugins are loaded.
	 */
	private static final String EXECUTE_START_JAVASCRIPT = "inspectIT.start();";

	/**
	 * the path to the javascript in the resources.
	 */
	private static final String SCRIPT_RESOURCE_PATH = "/js/";

	/**
	 * the path to the js agent without any plugins.
	 */
	private static final String JSBASE_RESOURCE = SCRIPT_RESOURCE_PATH + "inspectit_jsagent_base.js";

	/**
	 * Builds the JS agent from single char arguments.
	 *
	 * @param arguments
	 *            all arguments together as a string.
	 * @return the generated stream which builds the agent.
	 */
	protected static InputStream buildJsFile(String arguments) {
		// generate cookie script
		String generatedId = UUID.randomUUID().toString(); // will be unique
		String cookieSetupJS = COOKIE_SETUP_JAVASCRIPT.replace("{{id}}", generatedId);
		InputStream stringStream = new ByteArrayInputStream(cookieSetupJS.getBytes());

		List<InputStream> streams = new ArrayList<InputStream>();
		streams.add(stringStream);
		streams.add(JSAgentBuilder.class.getResourceAsStream(JSBASE_RESOURCE));

		// add wanted plugins
		String[] args = arguments.split("");
		for (String argument : args) {
			if (ARGUMENT_FILE_MAPPING.containsKey(argument)) {
				streams.add(JSAgentBuilder.class.getResourceAsStream(SCRIPT_RESOURCE_PATH + ARGUMENT_FILE_MAPPING.get(argument)));
			}
		}

		streams.add(new ByteArrayInputStream(EXECUTE_START_JAVASCRIPT.getBytes()));

		return new SequenceInputStream(Collections.enumeration(streams));
	}

	/**
	 * No instance creation allowed.
	 */
	private JSAgentBuilder() {
	}

}
