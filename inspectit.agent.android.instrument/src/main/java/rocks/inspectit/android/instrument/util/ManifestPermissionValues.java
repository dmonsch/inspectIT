package rocks.inspectit.android.instrument.util;

/**
 * @author David Monschein
 *
 */
public class ManifestPermissionValues {

	private int injectionPoint;

	private int lineNumber;

	private int attrNs;

	/**
	 * Gets {@link #injectionPoint}.
	 *
	 * @return {@link #injectionPoint}
	 */
	public int getInjectionPoint() {
		return injectionPoint;
	}

	/**
	 * Sets {@link #injectionPoint}.
	 *
	 * @param injectionPoint
	 *            New value for {@link #injectionPoint}
	 */
	public void setInjectionPoint(int injectionPoint) {
		this.injectionPoint = injectionPoint;
	}

	/**
	 * Gets {@link #lineNumber}.
	 *
	 * @return {@link #lineNumber}
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets {@link #lineNumber}.
	 *
	 * @param lineNumber
	 *            New value for {@link #lineNumber}
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Gets {@link #attrNs}.
	 *   
	 * @return {@link #attrNs}  
	 */ 
	public int getAttrNs() {
		return attrNs;
	}

	/**  
	 * Sets {@link #attrNs}.  
	 *   
	 * @param attrNs  
	 *            New value for {@link #attrNs}  
	 */
	public void setAttrNs(int attrNs) {
		this.attrNs = attrNs;
	}

}
