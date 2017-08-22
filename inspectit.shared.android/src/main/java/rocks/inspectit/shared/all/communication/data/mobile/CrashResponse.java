package rocks.inspectit.shared.all.communication.data.mobile;

/**
 * @author David Monschein
 *
 */
@InfluxCompatibleAnnotation(measurement = "crashes")
public class CrashResponse extends MobileDefaultData {

	/**
	 * @param exceptionClass
	 * @param exceptionMessage
	 */
	public CrashResponse(String exceptionClass, String exceptionMessage) {
		super();
		this.exceptionClass = exceptionClass;
		this.exceptionMessage = exceptionMessage;
	}

	@InfluxCompatibleAnnotation(key = "class", tag = false)
	private String exceptionClass;

	@InfluxCompatibleAnnotation(key = "message", tag = false)
	private String exceptionMessage;

	/**
	 * Gets {@link #exceptionClass}.
	 *
	 * @return {@link #exceptionClass}
	 */
	public String getExceptionClass() {
		return this.exceptionClass;
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
		return this.exceptionMessage;
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
