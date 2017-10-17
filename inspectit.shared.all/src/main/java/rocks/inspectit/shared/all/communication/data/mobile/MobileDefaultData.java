package rocks.inspectit.shared.all.communication.data.mobile;

import java.sql.Timestamp;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * @author David Monschein
 *
 */
public class MobileDefaultData extends DefaultData {

	/**
	 * Serial UID.
	 */
	private static final long serialVersionUID = -685466883035297259L;

	public MobileDefaultData(rocks.inspectit.shared.android.mobile.MobileDefaultData analog) {
		this.setTimeStamp(new Timestamp(analog.getCreationTimestamp()));
	}

}
