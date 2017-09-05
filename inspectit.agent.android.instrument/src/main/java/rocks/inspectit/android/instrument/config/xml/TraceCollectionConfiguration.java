package rocks.inspectit.android.instrument.config.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Class for configuring the packages which should be monitored with trace collection.
 *
 * @author David Monschein
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TraceCollectionConfiguration {

	/**
	 * Ruleset for packages which should be monitored with traces.
	 */
	@XmlElement(name = "rule")
	private List<TraceCollectionRule> packages;

	/**
	 * Creates an empty trace collection configuration.
	 */
	public TraceCollectionConfiguration() {
	}

	/**
	 * @return the packages
	 */
	public List<TraceCollectionRule> getPackages() {
		return packages;
	}

	/**
	 * @param packages
	 *            the packages to set
	 */
	public void setPackages(final List<TraceCollectionRule> packages) {
		this.packages = packages;
	}

	public Set<String> isTracedMethod(String clazz, String method, List<? extends CharSequence> parameters) {
		List<TraceCollectionRule> patterns = this.getPackages();

		Set<String> output = new HashSet<>();

		for (TraceCollectionRule pattern : patterns) {
			String[] patternSplit = pattern.getPattern().split("\\.");
			String[] matchSplit = (clazz.replaceAll("/", ".").substring(1, clazz.length() - 1) + "." + method).split("\\.");

			int k = 0;
			for (String part : patternSplit) {

				if (k >= matchSplit.length) {
					break;
				}

				if (!part.equals("*")) {
					if (part.equals("**")) {
						output.addAll(pattern.getSensor());
					} else {
						if (!part.equals(matchSplit[k])) {
							break;
						}
					}
				}

				++k;
			}
		}

		return output;
	}

}
