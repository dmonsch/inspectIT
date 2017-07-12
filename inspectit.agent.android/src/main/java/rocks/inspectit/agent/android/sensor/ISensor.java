package rocks.inspectit.agent.android.sensor;

/**
 * Interface for the sensor concept of the agent.
 *
 * @author David Monschein
 *
 */
public interface ISensor {
	/**
	 * Is executed before the original method gets executed.
	 * 
	 * @param methodSignature
	 */
	void beforeBody(long methodId, String methodSignature);

	/**
	 * Gets executed if the real method throws an exception.
	 *
	 * @param clazz
	 *            the class of the exception
	 */
	void exceptionThrown(long id, String clazz);

	/**
	 * Gets executed after the original method was executed.
	 */
	void firstAfterBody(long id);
}
