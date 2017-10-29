package rocks.inspectit.agent.android.sensor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.propagation.Format;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.module.CoreSpanReporter;
import rocks.inspectit.agent.android.sensor.http.HttpConnectionPoint;
import rocks.inspectit.agent.android.sensor.http.HttpConnectionState;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
@SensorAnnotation(id = 1)
public class NetworkSensor extends AbstractMethodSensor {

	private Map<HttpURLConnection, Pair<SpanImpl, HttpConnectionState>> connectionStateMap;

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

			if (!connectionStateMap.containsKey(casted)) {
				// build span for the network request
				SpanImpl currSp = tracerUtil.buildSpan(casted.getURL().toString());

				if (currSp.context() != null) {
					// we need to create an association
					tracerUtil.inject(currSp.context(), Format.Builtin.HTTP_HEADERS, new HttpURLConnectionAdapter(casted));
				}

				if (!connectionStateMap.containsKey(casted)) {
					connectionStateMap.put(casted, new Pair<SpanImpl, HttpConnectionState>(currSp, new HttpConnectionState()));
				}
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
				Pair<SpanImpl, HttpConnectionState> state = connectionStateMap.get(casted);
				state.getSecond().update(belonging);

				if (state.getSecond().finished()) {
					// create details
					HttpNetworkRequest reqResp = new HttpNetworkRequest();
					reqResp.setContentType(casted.getContentType());
					reqResp.setDuration(state.getSecond().responseTime());
					reqResp.setMethod(casted.getRequestMethod());
					reqResp.setUrl(casted.getURL().toString());
					try {
						reqResp.setResponseCode(casted.getResponseCode());
					} catch (IOException e) {
						// nothing to do here -> this is no problem for us
					}

					// SPAN things
					CoreSpanReporter.queueNetRequest(reqResp);
					// finish span
					state.getFirst().setBaggageItem("net", "");
					state.getFirst().finish();

					// this goes directly to influx
					this.pushData(reqResp);

					// remove this
					connectionStateMap.remove(casted);
				}
			}
		}
	}

}
