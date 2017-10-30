package rocks.inspectit.server.influx.builder.mobile;

import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.influx.builder.SinglePointBuilder;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
public abstract class AbstractMobilePointBuilder<T extends MobileDefaultData> extends SinglePointBuilder<T> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(T data, Builder builder) {
		for (Pair<String, String> tag : data.getSessionTags()) {
			builder.tag(tag.getFirst(), tag.getSecond());
		}

		this.completeFields(data, builder);
	}

	protected abstract void completeFields(T data, Builder builder);

}
