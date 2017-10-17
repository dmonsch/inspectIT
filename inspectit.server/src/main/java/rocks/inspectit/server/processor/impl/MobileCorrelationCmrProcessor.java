package rocks.inspectit.server.processor.impl;

import java.util.concurrent.ScheduledExecutorService;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import rocks.inspectit.server.dao.impl.BufferSpanDaoImpl;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.AbstractMobileSpanDetails;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;

/**
 * @author David Monschein
 *
 */
public class MobileCorrelationCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link BufferSpanDaoImpl}.
	 */
	@Autowired
	private BufferSpanDaoImpl spanDao;

	/**
	 * Scheduled executor used for scheduling the Span correlation due to asynchronous indexing and
	 * adding data to the buffer.
	 */
	@Qualifier("scheduledExecutorService")
	@Autowired
	private ScheduledExecutorService scheduledExecutor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		HttpNetworkRequest req = (HttpNetworkRequest) defaultData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		if (defaultData instanceof MobileSpan) {
			AbstractMobileSpanDetails details = ((MobileSpan) defaultData).getDetails();
			if (details instanceof HttpNetworkRequest) {
				return true;
			}
		}
		return false;
	}

}
