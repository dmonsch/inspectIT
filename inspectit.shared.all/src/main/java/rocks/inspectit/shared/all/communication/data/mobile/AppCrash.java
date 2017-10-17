package rocks.inspectit.shared.all.communication.data.mobile;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author David Monschein
 *
 */
public class AppCrash extends DefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 8049333482421879947L;

	private String exceptionClass;

	private String exceptionMessage;

	/**
	 * @param name
	 * @param message
	 */
	public AppCrash(String name, String message) {
		this.exceptionClass = name;
		this.exceptionMessage = message;
	}

	/**
	 * Gets {@link #exceptionClass}.
	 *
	 * @return {@link #exceptionClass}
	 */
	public String getExceptionClass() {
		return exceptionClass;
	}

	/**
	 * Sets {@link #exceptionClass}.
	 *
	 * @param exceptionClass
	 *            New value for {@link #exceptionClass}
	 */
	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	/**
	 * Gets {@link #exceptionMessage}.
	 *
	 * @return {@link #exceptionMessage}
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	/**
	 * Sets {@link #exceptionMessage}.
	 *
	 * @param exceptionMessage
	 *            New value for {@link #exceptionMessage}
	 */
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

}
