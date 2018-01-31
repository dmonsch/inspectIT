package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.cassandra.SchemaAwareCassandraDao;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.ActivityStart;
import rocks.inspectit.shared.all.communication.data.mobile.UncaughtException;
import rocks.inspectit.shared.all.communication.data.mobile.BatteryConsumption;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.SystemResourceUsage;

/**
 * @author David Monschein
 *
 */
public class MobileCassandraWriterProcessor extends AbstractCmrDataProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(MobileCassandraWriterProcessor.class);

	@Autowired
	SchemaAwareCassandraDao cassandra;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		if (defaultData instanceof MobileDefaultData) {
			MobileDefaultData data = (MobileDefaultData) defaultData;
			if (data instanceof HttpNetworkRequest) {
				cassandra.insertHttpRequest((HttpNetworkRequest) data);
			} else if (data instanceof SystemResourceUsage) {
				cassandra.insertResourceUsage((SystemResourceUsage) data);
			} else if (data instanceof BatteryConsumption) {
				cassandra.insertBatteryConsumption((BatteryConsumption) data);
			} else if (data instanceof ActivityStart) {
				cassandra.insertActivityStart((ActivityStart) data);
			} else if (data instanceof UncaughtException) {
				cassandra.insertAppCrash((UncaughtException) data);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof MobileDefaultData;
	}

}
