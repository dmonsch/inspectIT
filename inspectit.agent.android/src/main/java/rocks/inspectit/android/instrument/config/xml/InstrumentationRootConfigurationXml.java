package rocks.inspectit.android.instrument.config.xml;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * XML Mapping class for the main instrumentation configuration.
 * @author David Monschein
 *
 */
public class InstrumentationRootConfigurationXml {

	/**
	 * List of instrumentation point configurations.
	 */
	@XmlElementWrapper(name = "mapping")
	@XmlElement(name = "point")
	private List<InstrumentationPointConfigurationXml> instrPointConfigs;

	/**
	 * Creates a new instance.
	 */
	public InstrumentationRootConfigurationXml() {
	}

	/**
	 * Gets the list of instrumentation point configurations.
	 *
	 * @return the list of instrumentation point configurations
	 */
	public List<InstrumentationPointConfigurationXml> getInstrPointConfigs() {
		return instrPointConfigs;
	}

}
