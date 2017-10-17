package rocks.inspectit.agent.android.sensor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.sensor.http.HttpConnectionPoint;
import rocks.inspectit.agent.android.sensor.http.HttpConnectionState;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;

/**
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 1)
public class NetworkSensor extends AbstractMethodSensor {

	private Map<HttpURLConnection, HttpConnectionState> connectionStateMap;

	/**
	 * Link to the tracer implementation.
	 */
	private TracerImplHandler tracerUtil;

	public NetworkSensor() {
		this.connectionStateMap = new HashMap<>();

		tracerUtil = DependencyManager.getTracerImplHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeBody(long methodId, String methodSignature, Object object) {
		if ((object != null) && (object instanceof HttpURLConnection)) {
			HttpURLConnection casted = (HttpURLConnection) object;
			// get current context
			SpanContext currCtx = tracerUtil.getCurrentContext();
			if (currCtx != null) {
				// we need to create an association
				tracerUtil.inject(currCtx, Format.Builtin.HTTP_HEADERS, new HttpURLConnectionAdapter(casted));
			}

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
					// TODO integrate into span

					HttpNetworkRequest reqResp = new HttpNetworkRequest();
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
