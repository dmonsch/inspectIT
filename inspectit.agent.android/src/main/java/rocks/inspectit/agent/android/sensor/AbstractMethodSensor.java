package rocks.inspectit.agent.android.sensor;

import rocks.inspectit.agent.android.core.AndroidMonitoringComponent;

/**
 * @author David Monschein
 *
 */
abstract public class AbstractMethodSensor extends AndroidMonitoringComponent {

	abstract public void beforeBody(long methodId, String methoSignature, Object object);

	/**
	 * Gets executed after the original method was executed.
	 */
	abstract public void firstAfterBody(long methodId, String methodSignature, Object object);

}
