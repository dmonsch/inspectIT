package rocks.inspectit.agent.android.sensor;

/**
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 1)
public class NetworkSensor implements ISensor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(int sensorId, long methodId, String methoSignature, Object object) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionThrown(int sensorId, long methodId, String methodSignature, Object object, String clazz) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(int sensorId, long methodId, String methodSignature, Object object) {
		// TODO Auto-generated method stub

	}

}
