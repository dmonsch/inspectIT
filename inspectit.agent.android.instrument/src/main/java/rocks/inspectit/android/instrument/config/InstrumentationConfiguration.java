package rocks.inspectit.android.instrument.config;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import rocks.inspectit.android.instrument.config.xml.RootConfigurationXml;

/**
 * Configuration class for the instrumentation process of Android applications.
 *
 * @author David Monschein
 *
 */
public class InstrumentationConfiguration {

	/**
	 * Configuration as XML tree.
	 */
	private RootConfigurationXml xmlConfiguration;

	/**
	 * Creates a new instrumentation configuration with default values.
	 */
	public InstrumentationConfiguration() {
	}

	/**
	 * Parses a XML configuration file.
	 *
	 * @param config
	 *            configuration file
	 * @throws JAXBException
	 *             if the configuration couldn't be parsed
	 */
	public void parseConfigFile(final File config) throws JAXBException {
		if (config.exists()) {
			final JAXBContext jc = JAXBContext.newInstance(RootConfigurationXml.class);
			final Unmarshaller unmarshaller = jc.createUnmarshaller();

			final RootConfigurationXml sc = (RootConfigurationXml) unmarshaller.unmarshal(config);
			this.xmlConfiguration = sc;
		}
	}

	/**
	 * Gets the XML configuration tree.
	 *
	 * @return XML configuration
	 */
	public RootConfigurationXml getXmlConfiguration() {
		return xmlConfiguration;
	}

}
