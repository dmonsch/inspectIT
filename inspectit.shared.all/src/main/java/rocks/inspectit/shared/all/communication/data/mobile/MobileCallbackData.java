/***************************************************************************
 * Copyright (C) 2016 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

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
	 * The Session id.
	 */
	@JsonProperty
	private String sessionId;

	/**
	 * The timestamp when this container was created or reseted.
	 */
	@JsonProperty
	private long creationTimestamp;

	/**
	 * Creates an empty container.
	 */
	public MobileCallbackData() {
		this.creationTimestamp = System.currentTimeMillis();
		this.childData = new ArrayList<MobileDefaultData>();
	}

	/**
	 * Gets the creation timestamp.
	 *
	 * @return creation timestmap
	 */
	public long getCreationTimestamp() {
		return creationTimestamp;
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
	public void setChildData(final List<MobileDefaultData> childData) {
		this.childData = childData;
	}

	/**
	 * Adds a monitoring record to the container.
	 *
	 * @param data
	 *            monitoring record to add
	 */
	public void addChildData(final MobileDefaultData data) {
		this.childData.add(data);
	}

	/**
	 * Clears the container and removes all current monitoring records.
	 */
	public void clear() {
		creationTimestamp = System.currentTimeMillis();
		this.childData.clear();
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
	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}
}
