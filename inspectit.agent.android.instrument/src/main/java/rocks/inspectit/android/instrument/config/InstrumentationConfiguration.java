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
	 * Pattern which is replaced with the application package for the
	 * instrumentation rules.
	 */
	private static final String PATTERN_APPLICATION_PACKAGE = "{application}";

	/** Name of the main package from the application. */
	private String applicationPackage;

	/**
	 * Configuration as XML tree.
	 */
	private RootConfigurationXml xmlConfiguration;

	/**
	 * Creates a new instrumentation configuration with default values.
	 */
	public InstrumentationConfiguration() {
		this.applicationPackage = null;
	}

	public void loadInstrumentationPoints() {
	}

	/**
	 * Checks whether within a single class traces should be collected or not.
	 *
	 * @param clazzFull
	 *            the name of the class
	 * @return if the traces should get collected
	 */
	public boolean isTraceRelevantClass(final String clazzFull) {
		for (String rule : xmlConfiguration.getTraceCollectionList().getPackages()) {
			String ruleAdjust = rule;
			if (rule.contains(PATTERN_APPLICATION_PACKAGE)) {
				ruleAdjust = rule.replace(PATTERN_APPLICATION_PACKAGE, getApplicationPackage());
			}

			// CHECK IF RULE MATCHES
			final String[] packageSplit1 = clazzFull.replaceAll("/", ".").split("\\.");
			final String[] packageSplit2 = ruleAdjust.replaceAll("/", ".").split("\\.");

			boolean match = true;
			for (int i = 0; i < packageSplit1.length; i++) {
				if (packageSplit2[i].equals("**") && match) {
					return true;
				} else if (!packageSplit2[i].equals("*")) {
					if (!packageSplit1[i].equals(packageSplit2[i])) {
						match = false;
						break;
					}
				}
			}

			if (match) {
				return true;
			}
		}
		return false;
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

	/**
	 * Gets the application package name.
	 *
	 * @return application package name
	 */
	public String getApplicationPackage() {
		return applicationPackage;
	}

	/**
	 * Sets the application package name.
	 *
	 * @param applicationPackage
	 *            application package name
	 */
	public void setApplicationPackage(final String applicationPackage) {
		this.applicationPackage = applicationPackage.replaceAll("\\.", "/");
	}

}
