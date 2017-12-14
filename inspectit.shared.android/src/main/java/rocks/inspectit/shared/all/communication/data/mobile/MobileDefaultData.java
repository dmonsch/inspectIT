package rocks.inspectit.shared.all.communication.data.mobile;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author David Monschein
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = SessionCreation.class),
	@JsonSubTypes.Type(value = AbstractMobileSpanDetails.class),
	@JsonSubTypes.Type(value = AppCrash.class),
	@JsonSubTypes.Type(value = SystemResourceUsage.class),
	@JsonSubTypes.Type(value = BatteryConsumption.class),
	@JsonSubTypes.Type(value = HttpNetworkRequest.class)})
public class MobileDefaultData extends DefaultData {
	public MobileDefaultData() {
		this.setTimeStamp(new Timestamp(System.currentTimeMillis()));
	}
}
