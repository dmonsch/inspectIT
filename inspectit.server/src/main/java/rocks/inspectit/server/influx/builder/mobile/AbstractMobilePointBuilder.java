package rocks.inspectit.server.influx.builder.mobile;

import org.influxdb.dto.Point.Builder;

import rocks.inspectit.server.influx.builder.SinglePointBuilder;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.util.Pair;

/**
 * Abstract point builder for all mobile monitoring records.
 *
 * @author David Monschein
 * @param <T>
 *            the concrete type of records to process
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
			builder.addField("field_" + tag.getFirst(), tag.getSecond());
		}

		this.completeFields(data, builder);
	}

	/**
	 * This method should complete the information for a concrete Influx point.
	 * 
	 * @param data
	 *            the monitoring record
	 * @param builder
	 *            the builder for the point
	 */
	protected abstract void completeFields(T data, Builder builder);

}
