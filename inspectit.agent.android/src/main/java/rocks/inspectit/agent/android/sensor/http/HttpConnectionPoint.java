package rocks.inspectit.agent.android.sensor.http;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Monschein
 *
 */
public enum HttpConnectionPoint {
	BEFORE(""), CONNECT("connect"), OUTPUT("getOutputStream"), RESPONSECODE("getResponseCode"), INPUT("getInputStream");

	private static Map<String, HttpConnectionPoint> staticMapping = new HashMap<>();

	static {
		for (HttpConnectionPoint t : values()) {
			staticMapping.put(t.correspondingMethod, t);
		}
	}

	public static HttpConnectionPoint getCorrespondingPoint(String methName) {
		return staticMapping.get(methName);
	}

	private final String correspondingMethod;

	private HttpConnectionPoint(String methodMapping) {
		this.correspondingMethod = methodMapping;
	}

	@Override
	public String toString() {
		return correspondingMethod;
	}
}
