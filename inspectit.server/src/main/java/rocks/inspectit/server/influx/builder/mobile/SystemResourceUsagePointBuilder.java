package rocks.inspectit.server.influx.builder.mobile;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.mobile.SystemResourceUsage;

/**
 * Class which handles the creation of Influx points for {@link SystemResourceUsage} records.
 *
 * @author David Monschein
 *
 */
@Component
public class SystemResourceUsagePointBuilder extends AbstractMobilePointBuilder<SystemResourceUsage> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends SystemResourceUsage>> getDataClasses() {
		return Collections.singleton(SystemResourceUsage.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.MobileResourceUsage.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void completeFields(SystemResourceUsage data, Builder builder) {
		builder.addField(Series.MobileResourceUsage.CPU_USAGE, data.getCpuUsage());
		builder.addField(Series.MobileResourceUsage.MEMORY_USAGE, data.getMemoryUsage());
	}

}
