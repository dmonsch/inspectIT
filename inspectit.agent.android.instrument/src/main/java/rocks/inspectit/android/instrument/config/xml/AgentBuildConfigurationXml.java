package rocks.inspectit.android.instrument.config.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * XML Mapping class for the agent build options.
 *
 * @author David Monschein
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AgentBuildConfigurationXml {

	/**
	 * Folders which are included in the agent build.
	 */
	@XmlElement(name = "folder")
	@XmlElementWrapper(name = "libraryFolders")
	private List<String> libraryFolders;

	@XmlElement(name = "force")
	private boolean force;

	/**
	 * Creates a new instance.
	 */
	public AgentBuildConfigurationXml() {
	}

	/**
	 * Gets the list of folders which are included in the agent build.
	 *
	 * @return list of folders which are included in the agent build
	 */
	public List<String> getLibraryFolders() {
		return libraryFolders;
	}

	public boolean isForce() {
		return force;
	}

}
