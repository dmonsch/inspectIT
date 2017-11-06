package rocks.inspectit.server.influx.builder.mobile;

import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;

/**
 * @author David Monschein
 *
 */
@Component
public class NetworkRequestPointBuilder extends AbstractMobilePointBuilder<HttpNetworkRequest> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<? extends Class<? extends HttpNetworkRequest>> getDataClasses() {
		return Collections.singleton(HttpNetworkRequest.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.MobileHttpNetworkRequest.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void completeFields(HttpNetworkRequest data, Builder builder) {
		builder.addField(Series.MobileHttpNetworkRequest.CONTENT_TYPE, data.getContentType() == null ? "null" : data.getContentType());
		builder.addField(Series.MobileHttpNetworkRequest.DURATION, data.getDuration());
		builder.addField(Series.MobileHttpNetworkRequest.METHOD, data.getMethod());
		builder.addField(Series.MobileHttpNetworkRequest.URL, data.getUrl());
		builder.addField(Series.MobileHttpNetworkRequest.RESPONSE_CODE, data.getResponseCode());

		builder.tag(Series.MobileHttpNetworkRequest.CONTENT_TYPE + "_tag", data.getContentType() == null ? "null" : data.getContentType());
		builder.tag(Series.MobileHttpNetworkRequest.URL + "_tag", data.getUrl());
		builder.tag(Series.MobileHttpNetworkRequest.RESPONSE_CODE + "_tag", String.valueOf(data.getResponseCode()));
	}

}
