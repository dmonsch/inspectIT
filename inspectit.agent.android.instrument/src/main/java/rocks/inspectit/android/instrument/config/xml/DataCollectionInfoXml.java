package rocks.inspectit.android.instrument.config.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author David Monschein
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DataCollectionInfoXml {

	@XmlElement(name = "collectLocation")
	private boolean collectLocation;

	@XmlElement(name = "collectBatteryConsumption")
	private boolean collectBatteryConsumption;

	@XmlElement(name = "shutdownOnDestroy")
	private boolean shutdownOnDestroy;

	/**
	 * Gets {@link #collectLocation}.
	 *
	 * @return {@link #collectLocation}
	 */
	public boolean isCollectLocation() {
		return collectLocation;
	}

	/**
	 * Gets {@link #collectBatteryConsumption}.
	 *
	 * @return {@link #collectBatteryConsumption}
	 */
	public boolean isCollectBatteryConsumption() {
		return collectBatteryConsumption;
	}

	/**
	 * Gets {@link #shutdownOnDestroy}.
	 *
	 * @return {@link #shutdownOnDestroy}
	 */
	public boolean isShutdownOnDestroy() {
		return shutdownOnDestroy;
	}

}
