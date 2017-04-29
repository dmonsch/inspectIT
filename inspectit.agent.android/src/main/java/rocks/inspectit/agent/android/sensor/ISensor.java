package rocks.inspectit.agent.android.sensor;

import rocks.inspectit.agent.android.callback.CallbackManager;

/**
 * Interface for the sensor concept of the agent.
 * 
 * @author David Monschein
 *
 */
public interface ISensor {
	/**
	 * Is executed before the original method gets executed.
	 */
	void beforeBody(long id);

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

	/**
	 * Gets executed after the original method and the
	 * {@link ISensor#firstAfterBody()} were executed.
	 */
	void secondAfterBody(long id);

	/**
	 * Sets the name of the class which owns the method.
	 * 
	 * @param owner
	 *            name of the class which owns the method
	 */
	void setOwner(long id, String owner);

	/**
	 * Sets the signature of the method for which this sensor is responsible.
	 * 
	 * @param methodSignature
	 *            the signature of the method
	 */
	void setSignature(long id, String methodSignature);

	/**
	 * Sets the callback manager for the sensor.
	 * 
	 * @param callbackManager
	 *            the {@link CallbackManager}
	 */
	void setCallbackManager(CallbackManager callbackManager);
}
