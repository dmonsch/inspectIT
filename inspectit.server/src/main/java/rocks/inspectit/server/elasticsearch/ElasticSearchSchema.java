package rocks.inspectit.server.elasticsearch;

/**
 * @author Jonas Kunz
 *
 */
public interface ElasticSearchSchema {

	public interface MobileIndex {
		String TIMESTAMP = "@timestamp";

		String SESSION_ID = "session_id";

		String DEVICE_LANG = "device_lang";
		String DEVICE_NAME = "device_name";
		String APP_VERSION = "app_version";
		String ANDROID_VERSION = "android_version";
		String APP_NAME = "app_name";

		String POS_LON = "device_lon";
		String POS_LAT = "device_lat";
	}

	public interface UncaughtException extends MobileIndex {
		String INDEX_NAME = "mobile-crashes";

		String EXCEPTION_CLASS = "exception_class";
		String EXCEPTION_MESSAGE = "exception_message";
	}

	public interface BatteryConsumption extends MobileIndex {
		String INDEX_NAME = "mobile-battery";

		String CONSUMPTION_VALUE = "consumption_value";
		String CONSUMPTION_INTERVAL = "consumption_interval";
	}

	public interface ActivityStart extends MobileIndex {
		String INDEX_NAME = "mobile-activities";

		String ACTIVITY_NAME = "activity_name";
		String ACTIVITY_TIMESTAMP = "activity_timestamp";
		String ACTIVITY_CLASS = "activity_class";
	}

	public interface ActivityTransition extends MobileIndex {
		String INDEX_NAME = "mobile-activity-transition";

		String ACTIVITY_FROM_NAME = "activity_name_from";
		String ACTIVITY_TO_NAME = "activity_name_to";
	}

	public interface HttpNetworkRequest extends MobileIndex {
		String INDEX_NAME = "mobile-httprequests";

		String URL = "urlname";
		String METHOD = "method";

		String RESPONSE_CODE = "response_code";
		String DURATION = "duration";
		String CONTENT_TYPE = "content_type";
	}

	public interface ResourceUsage extends MobileIndex {
		String INDEX_NAME = "mobile-resourceusage";

		String CPU_USAGE = "cpu_usage";
		String MEMROY_USAGE = "memory_usage";
	}

}
