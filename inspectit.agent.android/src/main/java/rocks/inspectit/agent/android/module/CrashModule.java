package rocks.inspectit.agent.android.module;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import rocks.inspectit.shared.all.communication.data.mobile.AppCrash;

/**
 * Module which captures crashes of the application and sends them back to the CMR.
 *
 * @author David Monschein
 *
 */
public class CrashModule extends AbstractMonitoringModule {

	/**
	 * Link to the default uncaught exception handler to be able to call it.
	 */
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
		Thread.setDefaultUncaughtExceptionHandler(backup); // set the old one
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStartActivity(Object obj) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onStopActivity(Object obj) {
		// nothing to do here
	}

	/**
	 * Function which handles an uncaught exception and sends a message to the CMR.
	 *
	 * @param thread
	 *            the thread which caused the exception
	 * @param e
	 *            the uncaught exception
	 */
	private void handleUncaughtException(Thread thread, Throwable e) {
		// do our part
		AppCrash resp = new AppCrash(e.getClass().getName(), e.getMessage());
		this.pushData(resp);
		// TODO force send

		// call old handler
		if (backup != null) {
			backup.uncaughtException(thread, e);
		}
	}


}
