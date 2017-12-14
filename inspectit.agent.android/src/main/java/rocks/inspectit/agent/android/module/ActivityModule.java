package rocks.inspectit.agent.android.module;

import android.content.Context;
import rocks.inspectit.shared.all.communication.data.mobile.ActivityStart;

/**
 * @author David Monschein
 *
 */
public class ActivityModule extends AbstractMonitoringModule {

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStartActivity(Object obj) {
		System.out.println(obj.getClass().getName());
		ActivityStart startEv = new ActivityStart();
		startEv.setActivityClass(obj.getClass().getName());
		startEv.setActivityName(obj.getClass().getSimpleName());
		startEv.setStartTimestamp(System.currentTimeMillis());

		this.pushData(startEv);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStopActivity(Object obj) {
	}

}
