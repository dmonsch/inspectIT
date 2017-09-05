package rocks.inspectit.android.instrument.config.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author David Monschein
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TraceCollectionRule {

	@XmlElement(name = "package")
	private String pattern;

	@XmlElement(name = "sensor")
	private List<String> sensor;

	/**
	 * Gets {@link #pattern}.
	 *
	 * @return {@link #pattern}
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets {@link #pattern}.
	 *
	 * @param pattern
	 *            New value for {@link #pattern}
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Gets {@link #sensor}.
	 *
	 * @return {@link #sensor}
	 */
	public List<String> getSensor() {
		return sensor;
	}

	/**
	 * Sets {@link #sensor}.
	 *
	 * @param sensor
	 *            New value for {@link #sensor}
	 */
	public void setSensor(List<String> sensor) {
		this.sensor = sensor;
	}

}
