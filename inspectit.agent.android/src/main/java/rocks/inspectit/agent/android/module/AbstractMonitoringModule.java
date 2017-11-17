package rocks.inspectit.agent.android.module;

import android.content.Context;
import rocks.inspectit.agent.android.core.AndroidMonitoringComponent;

/**
 * Abstract Android module which has an init and an exit point.
 *
 * @author David Monschein
 *
 */
public abstract class AbstractMonitoringModule extends AndroidMonitoringComponent {

	/**
	 * Initializes the module with a given context.
	 *
	 * @param ctx
	 *            context of the application
	 */
	public abstract void initModule(Context ctx);

	/**
	 * Shuts the module down.
	 */
	public abstract void shutdownModule();

	public abstract void onStartActivity(Object obj);

	public abstract void onStopActivity(Object obj);
}
