package rocks.inspectit.agent.android.interfaces.impl;

import android.os.Handler;
import rocks.inspectit.agent.android.interfaces.IScheduledExecutorService;

/**
 * @author David Monschein
 *
 */
public class ScheduledExecutorServiceImpl implements IScheduledExecutorService {

	private Handler intern;

	public ScheduledExecutorServiceImpl(Handler handler) {
		this.intern = handler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void post(Runnable r) {
		intern.post(r);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postDelayed(Runnable r, long delay) {
		intern.postDelayed(r, delay);
	}

}
