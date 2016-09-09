package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jonas Kunz
 *
 */
public enum JSAgentModule {

	// TODO: @David add tooltip and update names todo menaingful texts

	AJAX_MODULE('a', "plugins/ajax.js", "AJAX Capturing Module", "Description Here"), ASYNC_MODULE('b', "plugins/async.js", "Async Module", "Description Here"), LISTENER_MODULE('l',
			"plugins/listener.js", "Listener Instrumentation Module",
			"Description Here"), SPEEDINDEX_MODULE('r', "plugins/rum-speedindex.js", "Speed Index Module", "Description Here"), NAVTIMINGS_MODULE('1', "plugins/navtimings.js",
					"Navigation Timings Module", "Description Here"), RESTIMINGS_MODULE('2', "plugins/restimings.js", "Resource Timings Module", "Description Here");

	public static final Map<Character, JSAgentModule> IDENTIFIER_MAP;

	/**
	 * Increment for eah release where the JS Agent has changed. This value is embedded into the URL
	 * for fetching hte agent, therefore incrementing the revision ensures that the newest version
	 * is fetched instead of using an old one from the HTTP cache.
	 */
	public static final int JS_AGENT_REVISION = 1;

	static {
		HashMap<Character, JSAgentModule> temp = new HashMap<Character, JSAgentModule>();
		for (JSAgentModule mod : JSAgentModule.values()) {
			temp.put(mod.getIdentifier(), mod);
		}
		IDENTIFIER_MAP = Collections.unmodifiableMap(temp);
	}

	private char identifier;
	private String moduleSourceFile;
	private String uiName;
	private String uiDescription;

	/**
	 * @param identifier
	 * @param uiName
	 * @param moduleSourceFile
	 */
	private JSAgentModule(char identifier, String moduleSourceFile, String uiName, String uiDescription) {
		this.identifier = identifier;
		this.uiName = uiName;
		this.uiDescription = uiDescription;
		this.moduleSourceFile = moduleSourceFile;
	}

	/**
	 * Gets {@link #IDENTIFIER_MAP}.
	 *
	 * @return {@link #IDENTIFIER_MAP}
	 */
	public static Map<Character, JSAgentModule> getIdentifierMap() {
		return IDENTIFIER_MAP;
	}

	/**
	 * Gets {@link #identifier}.
	 *
	 * @return {@link #identifier}
	 */
	public char getIdentifier() {
		return this.identifier;
	}

	/**
	 * Gets {@link #uiName}.
	 *
	 * @return {@link #uiName}
	 */
	public String getUiName() {
		return this.uiName;
	}

	/**
	 * Gets {@link #moduleSourceFile}.
	 *
	 * @return {@link #moduleSourceFile}
	 */
	public String getModuleSourceFile() {
		return this.moduleSourceFile;
	}

	/**
	 * Gets {@link #uiDescription}.
	 *
	 * @return {@link #uiDescription}
	 */
	public String getUiDescription() {
		return this.uiDescription;
	}

}
