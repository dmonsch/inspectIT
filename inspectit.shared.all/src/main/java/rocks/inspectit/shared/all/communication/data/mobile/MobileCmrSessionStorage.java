package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
public class MobileCmrSessionStorage {

	private Map<String, List<Pair<String, String>>> sessionTagMapping;

	public MobileCmrSessionStorage() {
		sessionTagMapping = new HashMap<String, List<Pair<String, String>>>();
	}

	public String createEntry() {
		String nId = createSessionIdEntry();
		sessionTagMapping.put(nId, new ArrayList<Pair<String, String>>());

		return nId;
	}

	public void putTag(String sessionEntry, String tagKey, String tagValue) {
		if (hasEntry(sessionEntry)) {
			sessionTagMapping.get(sessionEntry).add(new Pair<String, String>(tagKey, tagValue));
		}
	}

	public boolean hasEntry(String sessionEntry) {
		return sessionTagMapping.containsKey(sessionEntry);
	}

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
