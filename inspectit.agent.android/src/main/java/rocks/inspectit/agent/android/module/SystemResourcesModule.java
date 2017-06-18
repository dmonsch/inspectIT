package rocks.inspectit.agent.android.module;

import android.content.Context;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;

/**
 * @author David Monschein
 *
 */
public class SystemResourcesModule extends AbstractMonitoringModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(Context ctx) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdownModule() {
	}

	@ExecutionProperty(interval = 30000L)
	public void collectMeasurements() {
		collectCpuUsage();
		collectMemoryUsage();
	}

	/**
	 *
	 */
	private void collectMemoryUsage() {
	}

	/**
	 *
	 */
	private void collectCpuUsage() {
	}

}
