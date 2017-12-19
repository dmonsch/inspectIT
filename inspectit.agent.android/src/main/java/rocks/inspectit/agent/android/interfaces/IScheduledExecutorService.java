package rocks.inspectit.agent.android.interfaces;

/**
 * @author David Monschein
 *
 */
public interface IScheduledExecutorService {

	public void post(Runnable r);

	public void postDelayed(Runnable r, long delay);

}
