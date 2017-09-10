package rocks.inspectit.agent.android.sensor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import rocks.inspectit.agent.android.sensor.http.HttpConnectionPoint;
import rocks.inspectit.agent.android.sensor.http.HttpConnectionState;
import rocks.inspectit.shared.all.communication.data.mobile.NetRequestResponse;

/**
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 1)
public class NetworkSensor extends AbstractMethodSensor {

	private Map<HttpURLConnection, HttpConnectionState> connectionStateMap;

	public NetworkSensor() {
		this.connectionStateMap = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, String methodSignature, Object object) {
		if ((object != null) && (object instanceof HttpURLConnection)) {
			HttpURLConnection casted = (HttpURLConnection) object;

			if (!connectionStateMap.containsKey(casted)) {
				connectionStateMap.put(casted, new HttpConnectionState());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void firstAfterBody(long methodId, String methodSignature, Object object) {
		String methodName = SensorUtil.getMethodName(methodSignature);
		if ((object != null) && (object instanceof HttpURLConnection)) {

			HttpURLConnection casted = (HttpURLConnection) object;
			HttpConnectionPoint belonging = HttpConnectionPoint.getCorrespondingPoint(methodName);

			if ((belonging != null) && connectionStateMap.containsKey(casted)) {
				HttpConnectionState state = connectionStateMap.get(casted);
				state.update(belonging);

				if (state.finished()) {
					NetRequestResponse reqResp = new NetRequestResponse();
					reqResp.setContentType(casted.getContentType());
					reqResp.setDuration(state.responseTime());
					reqResp.setMethod(casted.getRequestMethod());
					reqResp.setUrl(casted.getURL().toString());
					try {
						reqResp.setResponseCode(casted.getResponseCode());
					} catch (IOException e) {
						// nothing to do here -> this is no problem for us
					}

					this.pushData(reqResp);
				}
			}
		}
	}

}
