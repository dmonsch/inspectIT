package rocks.inspectit.agent.android.module;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import rocks.inspectit.shared.all.communication.data.mobile.CrashResponse;

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
	 * Function which handles an uncaught exception and sends a message to the CMR.
	 *
	 * @param thread
	 *            the thread which caused the exception
	 * @param e
	 *            the uncaught exception
	 */
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