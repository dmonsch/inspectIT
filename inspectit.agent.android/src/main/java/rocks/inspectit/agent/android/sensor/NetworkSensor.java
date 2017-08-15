package rocks.inspectit.agent.android.sensor;

/**
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 1L)
public class NetworkSensor implements ISensor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, String methoSignature, Object[] parameters) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionThrown(long methodId, String methodSignature, Object[] parameters, String clazz) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, String methodSignature, Object[] parameters) {
		// TODO Auto-generated method stub

	}

}
