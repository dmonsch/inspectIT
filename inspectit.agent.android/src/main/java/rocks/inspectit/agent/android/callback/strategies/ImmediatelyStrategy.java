package rocks.inspectit.agent.android.callback.strategies;

import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;

/**
 * Strategy which immediately sends all data objects to the server. This means
 * every data object is sent alone to the server and therefore this strategy
 * produces a high overhead and is only suggested for debug purposes.
 *
 * @author David Monschein
 *
 */
public class ImmediatelyStrategy extends AbstractCallbackStrategy {

	/**
	 * Creates a new instance.
	 */
	public ImmediatelyStrategy() {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addData(MobileDefaultData data) {
		this.data.addChildData(data);

		// DIRECTLY SEND
		super.sendBeacon();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addData(MobileSpan dat) {
		this.data.addChildSpan(dat);
		super.sendBeacon();
	}
}
