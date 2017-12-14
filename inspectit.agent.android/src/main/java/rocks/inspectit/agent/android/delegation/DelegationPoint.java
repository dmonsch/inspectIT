package rocks.inspectit.agent.android.delegation;

/**
 * Used by the {@link DelegationAnnotation} to annotate methods which handle method call events.
 *
 * @author David Monschein
 *
 */
public enum DelegationPoint {
	/**
	 * Method which handles activity onStart calls.
	 */
	ON_START,
	/**
	 * Method which handles activity onStop calls.
	 */
	ON_STOP,
	/**
	 * Method which handles method enter events.
	 */
	ON_METHOD_ENTER,
	/**
	 * Method which handles method exit events.
	 */
	ON_METHOD_EXIT
}
