package rocks.inspectit.agent.android.module;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import rocks.inspectit.shared.all.communication.data.mobile.CrashResponse;

/**
 * @author David Monschein
 *
 */
public class CrashModule extends AbstractMonitoringModule {

	private UncaughtExceptionHandler backup;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(Context ctx) {
		backup = Thread.getDefaultUncaughtExceptionHandler();
		// this needs to run on main thread
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				handleUncaughtException(thread, e);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdownModule() {
	}

	private void handleUncaughtException(Thread thread, Throwable e) {
		// do our part
		CrashResponse resp = new CrashResponse(e.getClass().getName(), e.getMessage());
		this.pushData(resp);

		// call old handler
		if (backup != null) {
			backup.uncaughtException(thread, e);
		}
	}

}
