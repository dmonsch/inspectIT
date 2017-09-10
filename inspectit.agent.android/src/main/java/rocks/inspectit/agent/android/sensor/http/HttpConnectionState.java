package rocks.inspectit.agent.android.sensor.http;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * @author David Monschein
 *
 */
public class HttpConnectionState {

	private Map<HttpConnectionPoint, Long> reachedPointMap;

	public HttpConnectionState() {
		reachedPointMap = new EnumMap<>(HttpConnectionPoint.class);
		reachedPointMap.put(HttpConnectionPoint.BEFORE, System.currentTimeMillis());
	}

	public void update(HttpConnectionPoint point) {
		if (!reachedPointMap.containsKey(point)) {
			reachedPointMap.put(point, System.currentTimeMillis());
		}
	}

	public boolean finished() {
		Set<HttpConnectionPoint> reachedPoints = reachedPointMap.keySet();

		return reachedPoints.contains(HttpConnectionPoint.INPUT) || reachedPoints.contains(HttpConnectionPoint.RESPONSECODE);
	}

	public long responseTime() {
		if (!finished()) {
			return 0L;
		} else {
			HttpConnectionPoint beginPoint;
			HttpConnectionPoint endPoint;

			if (reachedPointMap.containsKey(HttpConnectionPoint.CONNECT)) {
				beginPoint = HttpConnectionPoint.CONNECT;
			} else if (reachedPointMap.containsKey(HttpConnectionPoint.OUTPUT)) {
				beginPoint = HttpConnectionPoint.OUTPUT;
			} else {
				beginPoint = HttpConnectionPoint.BEFORE;
			}

			if (reachedPointMap.containsKey(HttpConnectionPoint.RESPONSECODE)) {
				endPoint = HttpConnectionPoint.RESPONSECODE;
			} else {
				endPoint = HttpConnectionPoint.INPUT;
			}

			return reachedPointMap.get(endPoint) - reachedPointMap.get(beginPoint);
		}
	}

}
