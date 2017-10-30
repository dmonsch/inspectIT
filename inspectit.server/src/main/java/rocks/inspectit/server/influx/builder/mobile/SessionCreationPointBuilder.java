package rocks.inspectit.server.influx.builder.mobile;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;

/**
 * @author David Monschein
 *
 */
@Component
public class SessionCreationPointBuilder extends AbstractMobilePointBuilder<SessionCreation> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends SessionCreation>> getDataClasses() {
		return Collections.singleton(SessionCreation.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.MobileSessionCreation.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void completeFields(SessionCreation data, Builder builder) {
		builder.addField(Series.MobileSessionCreation.APP_NAME, data.getAppName()).addField(Series.MobileSessionCreation.DEVICE_ID, data.getDeviceId());
	}

}
