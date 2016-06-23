package rocks.inspectit.shared.cs.ci.eum;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "end-user-monitoring-config")
public class EndUserMonitoringConfig {

	@XmlAttribute(name = "eum-enabled", required = true)
	private boolean eumEnabled = false;

	@XmlAttribute(name = "eum-script-base-url", required = true)
	private String scriptBaseUrl = "/";

	/**
	 * Gets {@link #eumEnabled}.
	 *
	 * @return {@link #eumEnabled}
	 */
	public boolean isEumEnabled() {
		return eumEnabled;
	}

	/**
	 * Sets {@link #eumEnabled}.
	 *
	 * @param eumEnabled
	 *            New value for {@link #eumEnabled}
	 */
	public void setEumEnabled(boolean eumEnabled) {
		this.eumEnabled = eumEnabled;
	}

	/**
	 * Gets {@link #scriptBaseUrl}.
	 *
	 * @return {@link #scriptBaseUrl}
	 */
	public String getScriptBaseUrl() {
		return scriptBaseUrl;
	}

	/**
	 * Sets {@link #scriptBaseUrl}.
	 *
	 * @param scriptBaseUrl
	 *            New value for {@link #scriptBaseUrl}
	 */
	public void setScriptBaseUrl(String scriptBaseUrl) {
		this.scriptBaseUrl = scriptBaseUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (eumEnabled ? 1231 : 1237);
		result = (prime * result) + ((scriptBaseUrl == null) ? 0 : scriptBaseUrl.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EndUserMonitoringConfig other = (EndUserMonitoringConfig) obj;
		if (eumEnabled != other.eumEnabled) {
			return false;
		}
		if (scriptBaseUrl == null) {
			if (other.scriptBaseUrl != null) {
				return false;
			}
		} else if (!scriptBaseUrl.equals(other.scriptBaseUrl)) {
			return false;
		}
		return true;
	}

}