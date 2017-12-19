package rocks.inspectit.agent.android.module;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.shared.all.communication.data.mobile.UncaughtException;

/**
 * Module which captures crashes of the application and sends them back to the CMR.
 *
 * @author David Monschein
 *
 */
public class UncaughtExceptionModule extends AbstractMonitoringModule {

	/**
	 * Link to the default uncaught exception handler to be able to call it.
	 */
	private UncaughtExceptionHandler backup;

	private UncaughtExceptionHandler intern = new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable e) {
			handleUncaughtException(thread, e);
		}
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(Context ctx) {
		updateCrashHandler();
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

	@ExecutionProperty(interval = 60000L)
	public void manageProperties() {
		updateCrashHandler();
	}

	private void updateCrashHandler() {
		if (Thread.getDefaultUncaughtExceptionHandler() != intern) {
			// someone did overwrite or we did not set yet
			backup = Thread.getDefaultUncaughtExceptionHandler();
			// this needs to run on main thread
			Thread.setDefaultUncaughtExceptionHandler(intern);
		}
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
		UncaughtException resp = new UncaughtException(e.getClass().getName(), e.getMessage());
		this.pushData(resp);
		this.forceSend();

		// call old handler
		if (backup != null) {
			backup.uncaughtException(thread, e);
		}
	}


}
