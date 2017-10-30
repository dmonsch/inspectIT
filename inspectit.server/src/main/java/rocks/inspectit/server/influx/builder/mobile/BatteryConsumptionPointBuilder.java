package rocks.inspectit.server.influx.builder.mobile;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.mobile.BatteryConsumption;

/**
 * @author David Monschein
 *
 */
@Component
public class BatteryConsumptionPointBuilder extends AbstractMobilePointBuilder<BatteryConsumption> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends BatteryConsumption>> getDataClasses() {
		return Collections.singleton(BatteryConsumption.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.MobileBatteryConsumption.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void completeFields(BatteryConsumption data, Builder builder) {
		builder.addField(Series.MobileBatteryConsumption.INTERVAL, data.getTimeInterval());
		builder.addField(Series.MobileBatteryConsumption.PERCENTS, data.getConsumptionPercent());
	}

}
