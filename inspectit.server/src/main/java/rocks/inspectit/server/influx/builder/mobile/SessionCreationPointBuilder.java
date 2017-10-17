package rocks.inspectit.server.influx.builder.mobile;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.builder.SinglePointBuilder;
import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;

/**
 * @author David Monschein
 *
 */
@Component
public class SessionCreationPointBuilder extends SinglePointBuilder<SessionCreation> {

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
	protected void addFields(SessionCreation data, Builder builder) {
		builder.tag(Series.MobileSessionCreation.APP_NAME, data.getAppName()).tag(Series.MobileSessionCreation.DEVICE_ID, data.getDeviceId());

		for (String key : data.getAdditionalInformation().keySet()) {
			builder.addField(key, data.getAdditionalInformation().get(key));
		}
	}

}
