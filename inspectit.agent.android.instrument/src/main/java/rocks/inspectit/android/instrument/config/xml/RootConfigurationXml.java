package rocks.inspectit.android.instrument.config.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML root mapping class which holds the whole XML configuration.
 * @author David Monschein
 *
 */
@XmlRootElement(name = "instrumenter-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class RootConfigurationXml {

	/**
	 * Connection informations.
	 */
	@XmlElement(name = "cmr-bridge")
	private ConnectionInfoXml connectionInfo;

	/**
	 * Manifest transformation configuration.
	 */
	@XmlElement(name = "manifestTransformer")
	private ManifestTransformerConfigXml manifestTransformerConfig;

	/**
	 * Agent build configuration.
	 */
	@XmlElement(name = "agentBuild")
	private AgentBuildConfigurationXml agentBuildConfiguration;

	/**
	 * Configuration for packages where traces should get collected.
	 */
	@XmlElement(name = "traceCollection")
	private TraceCollectionConfiguration traceCollectionList;

	/**
	 * Creates a new instance.
	 */
	public RootConfigurationXml() {
	}

	/**
	 * @return the connectionInfo
	 */
	public ConnectionInfoXml getConnectionInfo() {
		return connectionInfo;
	}

	/**
	 * @return the manifestTransformerConfig
	 */
	public ManifestTransformerConfigXml getManifestTransformer() {
		return manifestTransformerConfig;
	}

	/**
	 * @return the agentBuildConfiguration
	 */
	public AgentBuildConfigurationXml getAgentBuildConfiguration() {
		return agentBuildConfiguration;
	}

	/**
	 * @return the traceCollectionList
	 */
	public TraceCollectionConfiguration getTraceCollectionList() {
		return traceCollectionList;
	}

	/**
	 * @param traceCollectionList
	 *            the traceCollectionList to set
	 */
	public void setTraceCollectionList(final TraceCollectionConfiguration traceCollectionList) {
		this.traceCollectionList = traceCollectionList;
	}

}
