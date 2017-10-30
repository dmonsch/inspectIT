package rocks.inspectit.server.influx.builder.mobile;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.mobile.AppCrash;

/**
 * @author David Monschein
 *
 */
@Component
public class AppCrashPointBuilder extends AbstractMobilePointBuilder<AppCrash> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends AppCrash>> getDataClasses() {
		return Collections.singleton(AppCrash.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.MobileAppCrash.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void completeFields(AppCrash data, Builder builder) {
		builder.addField(Series.MobileAppCrash.EXCEPTION_CLASS, data.getExceptionClass());
		builder.addField(Series.MobileAppCrash.EXCEPTION_MESSAGE, data.getExceptionMessage());
	}

}
