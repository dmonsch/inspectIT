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

/**
 * Class holding information about a device which wants to connect to the
 * server.
 * 
 * @author David Monschein
 * 
 */
public class SessionCreationRequest extends MobileDefaultData {

	/**
	 * The name of the mobile application.
	 */
	private String appName;

	/**
	 * The device id.
	 */
	private String deviceId;

	/**
	 * Creates a new instance.
	 */
	public SessionCreationRequest() {
	}

	/**
	 * @return the appName
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * @param appName
	 *            the appName to set
	 */
	public void setAppName(final String appName) {
		this.appName = appName;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId
	 *            the deviceId to set
	 */
	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}
}
