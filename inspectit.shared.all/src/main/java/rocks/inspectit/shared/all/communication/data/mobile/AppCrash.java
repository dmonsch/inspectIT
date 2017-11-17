package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * Monitoring record which indicates a crash of the monitored application.
 *
 * @author David Monschein
 *
 */
public class AppCrash extends MobileDefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = 8049333482421879947L;

	/**
	 * The type of the occurred exception.
	 */
	private String exceptionClass;

	/**
	 * The message of the occurred exception.
	 */
	private String exceptionMessage;

	/**
	 * @param name
	 *            value for {@link AppCrash#exceptionClass}
	 * @param message
	 *            value for {@link AppCrash#exceptionMessage}
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
