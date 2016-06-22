/**
 *
 */
package rocks.inspectit.agent.java.eum.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Class for processing beacons which the javascript agent sends back to the agent.
 *
 * @author David Monschein
 */
public class DataHandler {

	// JSON OBJ CONFIG CONSTANTS (STRUCTURE OF THE JSON OBJ)
	private static final String JSON_SESSIONID_ATTRIBUTE = "sessionId";
	private static final String JSON_TYPE_ATTRIBUTE = "type";
	private static final String JSON_TYPE_SESSION = "userSession";
	private static final String JSON_TYPE_ACTION = "userAction";
	private static final String JSON_ACTION_CONTENTS = "contents";
	private static final String JSON_ACTION_SPECTYPE = "specialType";

	private static final String JSON_ACTION_TYPE_PAGELOAD = "pageLoad";
	private static final String JSON_ACTION_TYPE_CLICK = "click";
	// __________________________ //

	/**
	 * Maps the sessionIds to the UserSession objects.
	 */
	private final Map<String, UserSession> sessionMap;

	/**
	 * A list containing all user actions which need to get send to the CMR.
	 */
	private final List<UserAction> actions;

	/**
	 * Needed for parsing json beacon data.
	 */
	private final ObjectMapper jsonMapper = new ObjectMapper();
	/**
	 * Creates a new instance which handles sessions and user actions.
	 */
	public DataHandler() {
		this.actions = new ArrayList<UserAction>();
		this.sessionMap = new HashMap<String, UserSession>();
	}

	/**
	 * Parses the incoming beacon and decides whether it is a session creation or a user action and
	 * then adds it to the session map or to the user action list.
	 *
	 * @param data
	 *            the beacon which should get parsed and processed
	 */
	public void insertBeacon(String data) {
		// either a useraction or sessioncreation
		JsonNode jsonObj = null;
		try {
			jsonObj = jsonMapper.readTree(data);
		} catch (IOException e) {
			return;
		}

		if (jsonObj != null) {
			if (jsonObj.has(JSON_TYPE_ATTRIBUTE)) {
				String type = jsonObj.get(JSON_TYPE_ATTRIBUTE).asText();
				if (type.equals(JSON_TYPE_SESSION)) {
					createSession(jsonObj);
				} else if (type.equals(JSON_TYPE_ACTION)) {
					createUserAction(jsonObj);
				}
			}
		}
	}

	/**
	 * Creates a session from a json object using jackson.
	 *
	 * @param obj
	 *            the json object representing the user session.
	 */
	private void createSession(JsonNode obj) {
		try {
			UserSession newSession = jsonMapper.readValue(obj, UserSession.class);
			sessionMap.put(newSession.getSessionId(), newSession);
		} catch (JsonParseException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Creates a new user action from a json object which represents a user action.
	 *
	 * @param obj
	 *            json object representing a user action
	 */
	private void createUserAction(JsonNode obj) {
		if (obj.has(JSON_SESSIONID_ATTRIBUTE) && obj.has(JSON_ACTION_CONTENTS) && obj.has(JSON_ACTION_SPECTYPE)) {
			String specType = obj.get(JSON_ACTION_SPECTYPE).asText();
			String sessionId = obj.get(JSON_SESSIONID_ATTRIBUTE).asText();

			// do we need this in reality?
			if (!sessionMap.containsKey(sessionId)) {
				createEmptySession(sessionId);
			}

			if (obj.get(JSON_ACTION_CONTENTS).isArray() && sessionMap.containsKey(sessionId)) {
				UserSession userSession = sessionMap.get(sessionId);
				UserAction parsedAction = null;
				if (specType.equals(JSON_ACTION_TYPE_PAGELOAD)) {
					parsedAction = parsePageLoadAction(obj.get(JSON_ACTION_CONTENTS));
				} else if (specType.equals(JSON_ACTION_TYPE_CLICK)) {
					parsedAction = parseClickAction(obj.get(JSON_ACTION_CONTENTS));
				}

				if (parsedAction != null) {
					parsedAction.setUserSession(userSession);
					actions.add(parsedAction);
				}
			}
		}
	}

	/**
	 * Creates a page load action from a json array which contains all belonging requests. (at least
	 * one pageloadrequest)
	 *
	 * @param contentArray
	 *            the array which contains all belonging requests as json objects
	 * @return the parsed pageloadaction
	 */
	private UserAction parsePageLoadAction(JsonNode contentArray) {
		PageLoadAction rootAction = new PageLoadAction();
		for (JsonNode req : contentArray) {
			try {
				Request childRequest = jsonMapper.readValue(req, Request.class);
				if (childRequest.isPageLoad()) {
					// no instanceof :)
					rootAction.setPageLoadRequest((PageLoadRequest) childRequest);
				} else {
					rootAction.addRequest(childRequest);
				}
			} catch (IOException e) {
			}
		}
		return rootAction;
	}

	/**
	 * Creates a click action from a json array which contains all belonging requests.
	 *
	 * @param contentArray
	 *            json array which contains all belonging requests as json objects.
	 * @return the parsed click action.
	 */
	private UserAction parseClickAction(JsonNode contentArray) {
		boolean root = true;
		ClickAction rootAction = null;
		for (JsonNode reqOrAction : contentArray) {
			if (root) {
				try {
					rootAction = jsonMapper.readValue(reqOrAction, ClickAction.class);
				} catch (IOException e) {
					return null;
				}
				root = false;
			} else {
				if (rootAction != null) {
					try {
						Request childRequest = jsonMapper.readValue(reqOrAction, Request.class);
						rootAction.addRequest(childRequest);
					} catch (IOException e) {
					}
				}
			}
		}
		return rootAction;
	}

	/**
	 * Creates a session without browser informations. Needed if we want to assign data to a session
	 * but there is none on server side otherwise we would lose data.
	 *
	 * @param id
	 *            the id of the session which should get created
	 */
	private void createEmptySession(String id) {
		// for creating a session when the client already has one but it disappeared on the server
		UserSession r = new UserSession();
		r.setSessionId(id);
		sessionMap.put(id, r);
	}

}
