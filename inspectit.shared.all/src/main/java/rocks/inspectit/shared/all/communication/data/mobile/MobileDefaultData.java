package rocks.inspectit.shared.all.communication.data.mobile;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.util.Pair;

/**
 * Base monitoring record type which is used by all specific monitoring records.
 *
 * @author David Monschein
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = SessionCreation.class),
	@JsonSubTypes.Type(value = AbstractMobileSpanDetails.class),
	@JsonSubTypes.Type(value = UncaughtException.class),
	@JsonSubTypes.Type(value = SystemResourceUsage.class),
	@JsonSubTypes.Type(value = BatteryConsumption.class),
	@JsonSubTypes.Type(value = HttpNetworkRequest.class),
	@JsonSubTypes.Type(value = ActivityStart.class)})
public class MobileDefaultData extends DefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -685466883035297259L;

	/**
	 * List which contains tags belonging to this session. Is null till the CMR resolved belonging
	 * tags from a session storage.
	 */
	@JsonIgnore
	private List<Pair<String, String>> sessionTags;

	@JsonIgnore
	private String sessionId;

	/**
	 * Creates and initializes a basic mobile monitoring record.
	 */
	public MobileDefaultData() {
		this.setTimeStamp(new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * Gets {@link #sessionTags}.
	 *
	 * @return {@link #sessionTags}
	 */
	@JsonIgnore
	public List<Pair<String, String>> getSessionTags() {
		return sessionTags;
	}

	/**
	 * Sets {@link #sessionTags}.
	 *
	 * @param sessionTags
	 *            New value for {@link #sessionTags}
	 */
	@JsonIgnore
	public void setSessionTags(List<Pair<String, String>> sessionTags) {
		this.sessionTags = sessionTags;
	}

	/**
	 * Gets {@link #sessionId}.
	 * 
	 * @return {@link #sessionId}
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets {@link #sessionId}.
	 * 
	 * @param sessionId
	 *            New value for {@link #sessionId}
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
