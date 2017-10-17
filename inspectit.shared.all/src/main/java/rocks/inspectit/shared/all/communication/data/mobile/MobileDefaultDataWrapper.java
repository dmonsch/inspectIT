package rocks.inspectit.shared.all.communication.data.mobile;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.android.mobile.BatteryConsumptionResponse;
import rocks.inspectit.shared.android.mobile.CrashResponse;
import rocks.inspectit.shared.android.mobile.MobileDefaultData;
import rocks.inspectit.shared.android.mobile.NetRequestResponse;
import rocks.inspectit.shared.android.mobile.SessionCreationRequest;
import rocks.inspectit.shared.android.mobile.SystemResourceUsageResponse;

/**
 * @author David Monschein
 *
 */
public class MobileDefaultDataWrapper {

	public static DefaultData wrap(MobileDefaultData data) {
		if (data instanceof SystemResourceUsageResponse) {
			return new SystemResourceUsage((SystemResourceUsageResponse) data);
		} else if (data instanceof SessionCreationRequest) {
			return new SessionCreation((SessionCreationRequest) data);
		} else if (data instanceof BatteryConsumptionResponse) {
			return new BatteryConsumption((BatteryConsumptionResponse) data);
		} else if (data instanceof NetRequestResponse) {
			return new HttpNetworkRequest((NetRequestResponse) data);
		} else if (data instanceof CrashResponse) {
			return new AppCrash((CrashResponse) data);
		} else {
			return null;
		}
	}

}
