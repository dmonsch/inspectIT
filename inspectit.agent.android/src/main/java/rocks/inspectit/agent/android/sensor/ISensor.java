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
	void beforeBody(long methodId, String methoSignature, Object object);

	/**
	 * Gets executed after the original method was executed.
	 */
	void firstAfterBody(long methodId, String methodSignature, Object object);
}
