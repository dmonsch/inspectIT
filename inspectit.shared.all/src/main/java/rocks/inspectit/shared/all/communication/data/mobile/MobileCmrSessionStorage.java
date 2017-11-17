package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rocks.inspectit.shared.all.util.Pair;

/**
 * A simple class which is capable of handling sessions for mobile devices.
 *
 * @author David Monschein
 *
 */
public class MobileCmrSessionStorage {

	/**
	 * Maps a session ID to a list of key-value pairs.
	 */
	private Map<String, List<Pair<String, String>>> sessionTagMapping;

	/**
	 * Creates a new session storage.
	 */
	public MobileCmrSessionStorage() {
		sessionTagMapping = new HashMap<String, List<Pair<String, String>>>();
	}

	/**
	 * Creates a session entry.
	 *
	 * @return the generated session ID
	 */
	public String createEntry() {
		String nId = createSessionIdEntry();
		sessionTagMapping.put(nId, new ArrayList<Pair<String, String>>());

		return nId;
	}

	/**
	 * Saves a key-value pair for a given session ID.
	 *
	 * @param sessionEntry
	 *            session ID
	 * @param tagKey
	 *            key
	 * @param tagValue
	 *            value
	 */
	public void putTag(String sessionEntry, String tagKey, String tagValue) {
		if (hasEntry(sessionEntry)) {
			sessionTagMapping.get(sessionEntry).add(new Pair<String, String>(tagKey, tagValue));
		}
	}

	/**
	 * Checks if a specified session exists.
	 *
	 * @param sessionEntry
	 *            session ID
	 * @return true if there is a session for the given ID, false otherwise
	 */
	public boolean hasEntry(String sessionEntry) {
		return sessionTagMapping.containsKey(sessionEntry);
	}

	/**
	 * Gets all key-value pairs for a given session ID.
	 * 
	 * @param sessionEntry
	 *            session ID
	 * @return a list of key-value pairs if the session exists, null otherwise
	 */
	public List<Pair<String, String>> getTags(String sessionEntry) {
		if (hasEntry(sessionEntry)) {
			return sessionTagMapping.get(sessionEntry);
		} else {
			return null;
		}
	}

	/**
	 * Generates a new unique session id.
	 *
	 * @return generated session id
	 */
	private String createSessionIdEntry() {
		return UUID.randomUUID().toString();
	}

}
