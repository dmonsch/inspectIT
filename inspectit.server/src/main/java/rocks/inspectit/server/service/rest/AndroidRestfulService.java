package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ClassUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.communication.data.mobile.InfluxCompatibleAnnotation;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCallbackData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreationRequest;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreationResponse;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
@Controller
@RequestMapping(value = "/mobile")
public class AndroidRestfulService {

	private ObjectMapper mapper;

	private Map<String, Pair<String, String>> sessionStorage;

	@Autowired
	InfluxDBDao influxDB;

	public AndroidRestfulService() {
		mapper = new ObjectMapper();
		sessionStorage = new HashMap<>();
	}

	@RequestMapping(method = POST, value = "session")
	@ResponseBody
	public String createSession(@RequestBody String json) {
		try {
			MobileCallbackData data = mapper.readValue(json, MobileCallbackData.class);
			if ((data.getChildData().size() == 1)) {
				MobileDefaultData singleElement = data.getChildData().get(0);
				if (singleElement instanceof SessionCreationRequest) {
					SessionCreationRequest request = (SessionCreationRequest) singleElement;

					String sessionId = createSessionIdEntry();
					sessionStorage.put(sessionId, new Pair<String, String>(request.getAppName(), request.getDeviceId()));

					// insert into influx
					if (influxDB.isConnected()) {
						Point.Builder builder = Point.measurement("session").tag("appname", request.getAppName()).tag("deviceid", request.getDeviceId());
						for (String key : request.getAdditionalInformation().keySet()) {
							builder.addField(key, request.getAdditionalInformation().get(key));
						}
						influxDB.insert(builder.build());
					}

					SessionCreationResponse resp = new SessionCreationResponse();
					resp.setSessionId(sessionId);
					return mapper.writeValueAsString(resp);
				}
			}

		} catch (IOException e) {
			return "";
		}

		return "";
	}

	@RequestMapping(method = POST, value = "beacon")
	@ResponseBody
	public String getBeacon(@RequestBody String jsonBeacon) {
		if (!influxDB.isConnected()) {
			return "";
		}

		try {
			MobileCallbackData data = mapper.readValue(jsonBeacon, MobileCallbackData.class);
			if (sessionStorage.containsKey(data.getSessionId())) {
				Pair<String, String> belongingSession = sessionStorage.get(data.getSessionId());
				for (MobileDefaultData defData : data.getChildData()) {
					// get points
					if (defData.getClass().isAnnotationPresent(InfluxCompatibleAnnotation.class)) {
						// we have some data to insert
						String measurement = defData.getClass().getAnnotation(InfluxCompatibleAnnotation.class).measurement();
						Point.Builder nPoint = buildPoint(measurement, belongingSession);
						if (!measurement.equals("")) {
							// valid
							for (Field field : defData.getClass().getDeclaredFields()) {
								if (field.isAnnotationPresent(InfluxCompatibleAnnotation.class)) {
									InfluxCompatibleAnnotation annot = field.getAnnotation(InfluxCompatibleAnnotation.class);
									if (!annot.key().equals("")) {
										try {
											updatePoint(nPoint, field, defData, annot.key(), annot.tag());
										} catch (IllegalArgumentException | IllegalAccessException e) {
											e.printStackTrace();
										}
									}
								}
							}

							// finally build it
							influxDB.insert(nPoint.build());
						}
					}
				}
			}
		} catch (IOException e) {
			return "";
		}
		return "";
	}

	private Point.Builder buildPoint(String measurement, Pair<String, String> sessionInfo) {
		Point.Builder builder = Point.measurement(measurement);

		builder.tag("appname", sessionInfo.getFirst());
		builder.tag("deviceid", sessionInfo.getSecond());

		return builder;
	}

	private void updatePoint(Point.Builder point, Field field, MobileDefaultData obj, String key, boolean tag) throws IllegalArgumentException, IllegalAccessException {
		boolean accessibility = field.isAccessible();
		if (!accessibility) {
			field.setAccessible(true);
		}

		if (field.getType().equals(String.class)) {
			String literal = (String) field.get(obj);
			if (tag) {
				point.tag(key, literal);
			} else {
				point.addField(key, literal);
			}
		} else if (field.getType().equals(boolean.class)) {
			boolean bool = (boolean) field.get(obj);
			if (tag) {
				point.tag(key, String.valueOf(bool));
			} else {
				point.addField(key, bool);
			}
		} else if (ClassUtils.isAssignable(field.getType(), Number.class, true)) {
			Number val = (Number) field.get(obj);
			if (tag) {
				point.tag(key, String.valueOf(val));
			} else {
				point.addField(key, val);
			}
		} else if (field.getType().equals(double.class)) {
			double val = (double) field.get(obj);
			if (tag) {
				point.tag(key, String.valueOf(val));
			} else {
				point.addField(key, val);
			}
		} else if (field.getType().equals(long.class)) {
			long val = (long) field.get(obj);
			if (tag) {
				point.tag(key, String.valueOf(val));
			} else {
				point.addField(key, val);
			}
		}

		field.setAccessible(accessibility);
	}

	private String createSessionIdEntry() {
		return UUID.randomUUID().toString();
	}
}

