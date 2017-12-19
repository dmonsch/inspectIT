package rocks.inspectit.server.cassandra;

/**
 * @author Jonas Kunz
 *
 */
public interface CassandraSchema {

	public interface MobileTable {
		String SESSION_ID = "session_id";

		String TIME = "time";
		String DAY = "day";

		String DEVICE_LANG = "device_lang";
		String DEVICE_NAME = "device_name";
		String APP_VERSION = "app_version";
		String ANDROID_VERSION = "android_version";
		String APP_NAME = "app_name";

		String POS_LON = "device_lon";
		String POS_LAT = "device_lat";
	}

	public interface AppCrash extends MobileTable {
		String TABLE_NAME = "app_crashes";

		String EXCEPTION_CLASS = "exception_class";
		String EXCEPTION_MESSAGE = "exception_message";
	}

	public interface BatteryConsumption extends MobileTable {
		String TABLE_NAME = "battery_consumption";

		String CONSUMPTION_VALUE = "consumption_value";
		String CONSUMPTION_INTERVAL = "consumption_interval";
	}

	public interface ActivityStart extends MobileTable {
		String TABLE_NAME = "activity_start";

		String ACTIVITY_NAME = "activity_name";
		String ACTIVITY_TIMESTAMP = "activity_timestamp";
		String ACTIVITY_CLASS = "activity_class";
	}

	public interface HttpNetworkRequest extends MobileTable {
		String TABLE_NAME = "httprequests";

		String URL = "url";
		String METHOD = "method";

		String RESPONSE_CODE = "response_code";
		String DURATION = "duration";
		String CONTENT_TYPE = "content_type";
	}

	public interface ResourceUsage extends MobileTable {
		String TABLE_NAME = "resource_usage";

		String CPU_USAGE = "cpu_usage";
		String MEMROY_USAGE = "memory_usage";
	}

	public interface EumTable {

		String DAY = "day";
		String TIME = "time";
		String TRACE_ID = "trace_id";
		String SPAN_ID = "span_id";
		String SESSION_ID = "session_id";
		String TAB_ID = "tab_id";
		String BROWSER = "browser";
		String DEVICE = "device";
		String LANGUAGE = "language";

	}

	public interface AjaxRequests extends EumTable {
		String TABLE_NAME = "ajax_requests";

		String DURATION = "duration";
		String URL = "url";
		String BASE_URL = "base_url";
		String STATUS = "status";
	}

	public interface ResourceLoadRequests extends EumTable {
		String TABLE_NAME = "resourceload_requests";

		String DURATION = "duration";
		String URL = "url";
		String BASE_URL = "base_url";

		String INITIATOR_TYPE = "initiator_type";
		String TRANSFER_SIZE = "transfer_size";
	}

	public interface RootDomEvents extends EumTable {
		String TABLE_NAME = "root_dom_events";

		String RELEVANT_THROUGH_SELECTOR = "relevant_through_selector";
		String BASE_URL = "base_url";
		String ACTION_TYPE = "action_type";
		String ELEMENT_INFO = "element_info";
	}

	public interface PageLoadRequests extends EumTable {
		String TABLE_NAME = "pageload_requests";

		String URL = "url";
		String DURATION = "duration";
		String RESOURCE_COUNT = "resource_count";

		String SPEEDINDEX = "speedindex";
		String FIRST_PAINT = "first_paint";

		/// navtimings, navigation start and loadEventEnd are repsented by time and duration
		String UNLOAD_EVENT_START = "unload_event_start";
		String UNLOAD_EVENT_END = "unload_event_end";
		String REDIRECT_START = "redirect_start";
		String REDIRECT_END = "redirect_end";
		String FETCH_START = "fetch_start";
		String DOMAIN_LOOKUP_START = "domain_lookup_start";
		String DOMAIN_LOOKUP_END = "domain_lookup_end";
		String CONNECT_START = "connect_start";
		String CONNECT_END = "connect_end";
		String SECURE_CONNECTION_START = "secure_connection_start";
		String REQUEST_START = "request_start";
		String RESPONSE_START = "response_start";
		String RESPONSE_END = "response_end";
		String DOM_LOADING = "dom_loading";
		String DOM_INTERACTIVE = "dom_interactive";
		String DOM_CONTENT_LOADED_EVENT_START = "dom_content_loaded_event_start";
		String DOM_CONTENT_LOADED_EVENT_END = "dom_content_loaded_event_end";
		String DOM_COMPLETE = "dom_complete";
		String LOAD_EVENT_START = "load_event_start";
		String LOAD_EVENT_END = "load_event_end";
	}

}