package rocks.inspectit.android.instrument.config.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.reference.MethodReference;

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

	@XmlTransient
	private Set<Method> directlyInstrumentedMethods;

	/**
	 * Creates an empty trace collection configuration.
	 */
	public TraceCollectionConfiguration() {
		this.directlyInstrumentedMethods = new HashSet<Method>();
	}

	/**
	 * @param meth
	 * @return
	 */
	public boolean isAlreadyInstrumented(MethodReference meth) {
		for (Method mt : directlyInstrumentedMethods) {
			if (mt.getName().equals(meth.getName()) && mt.getDefiningClass().equals(meth.getDefiningClass()) && meth.getReturnType().equals(mt.getReturnType())
					&& arrayEq(mt.getParameterTypes(), meth.getParameterTypes())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param parameterTypes
	 * @param parameterTypes2
	 * @return
	 */
	private boolean arrayEq(List<? extends CharSequence> parameterTypes, List<? extends CharSequence> parameterTypes2) {
		if (parameterTypes.size() != parameterTypes2.size()) {
			return false;
		}
		for (int i = 0; i < parameterTypes.size(); i++) {
			if (!parameterTypes.get(i).toString().equals(parameterTypes2.get(i).toString())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the packages
	 */
	public List<TraceCollectionRule> getPackages() {
		return packages;
	}

	public void markMethodAsInstrumented(Method meth) {
		directlyInstrumentedMethods.add(meth);
	}

	public boolean isAlreadyInstrumented(Method meth) {
		if (meth == null) {
			return false;
		}
		return directlyInstrumentedMethods.contains(meth);
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
