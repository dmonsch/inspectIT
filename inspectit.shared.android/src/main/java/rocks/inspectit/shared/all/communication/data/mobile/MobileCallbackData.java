package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains multiple monitoring records and the session id. This is the
 * data-type which is sent to the monitoring server.
 *
 * @author David Monschein
 *
 */
public class MobileCallbackData {

	/**
	 * Container for monitoring records.
	 */
	@JsonProperty
	private List<MobileDefaultData> childData;

	/**
	 * Container for span records.
	 */
	@JsonProperty
	private List<MobileSpan> childSpans;

	/**
	 * The Session id.
	 */
	@JsonProperty
	private String sessionId;

	/**
	 * Creates an empty container.
	 */
	public MobileCallbackData() {
		this.childData = new ArrayList<MobileDefaultData>();
		this.childSpans = new ArrayList<MobileSpan>();
	}

	/**
	 * Gets the monitoring records in this container.
	 *
	 * @return monitoring records in this container
	 */
	public List<MobileDefaultData> getChildData() {
		return childData;
	}

	/**
	 * Sets the monitoring records in this container.
	 *
	 * @param childData
	 *            monitoring records to set
	 */
	public void setChildData(List<MobileDefaultData> childData) {
		this.childData = childData;
	}

	/**
	 * Adds a monitoring record to the container.
	 *
	 * @param data
	 *            monitoring record to add
	 */
	public void addChildData(MobileDefaultData data) {
		this.childData.add(data);
	}

	/**
	 * Clears the container and removes all current monitoring records.
	 */
	public void clear() {
		this.childData.clear();
		this.childSpans.clear();
	}

	/**
	 * Gets the session id.
	 *
	 * @return session id
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the session id.
	 *
	 * @param sessionId
	 *            session id to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Gets {@link #childSpans}.
	 *
	 * @return {@link #childSpans}
	 */
	public List<MobileSpan> getChildSpans() {
		return this.childSpans;
	}

	/**
	 * Adds a span to {@link #childSpans}.
	 *
	 * @param child
	 *            span to add
	 */
	public void addChildSpan(MobileSpan child) {
		this.childSpans.add(child);
	}
}
