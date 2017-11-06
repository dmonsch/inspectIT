package rocks.inspectit.android.instrument.dex.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jf.dexlib2.iface.reference.MethodReference;

import rocks.inspectit.agent.android.sensor.SensorAnnotation;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;

/**
 * @author David Monschein
 *
 */
public class SensorCache {

	private static SensorCache currentInstance;

	public static SensorCache getCurrentInstance(TraceCollectionConfiguration config) {
		if (currentInstance == null) {
			currentInstance = new SensorCache(config);
		}
		return currentInstance;
	}

	private Map<String, Integer> sensorIdCache;
	private TraceCollectionConfiguration config;

	public SensorCache(TraceCollectionConfiguration config) {
		this.sensorIdCache = new HashMap<>();
		this.config = config;
	}

	public int[] resolveAllSensorIds(MethodReference meth) {
		Set<String> sens = config.isTracedMethod(meth.getDefiningClass(), meth.getName(), meth.getParameterTypes());
		int[] _return = new int[sens.size()];

		int k = 0;
		for (String se : sens) {
			_return[k++] = resolveSensorId(se);
		}

		return _return;
	}

	public int resolveSensorId(String sensorClazz) {
		if (sensorIdCache.containsKey(sensorClazz)) {
			return sensorIdCache.get(sensorClazz);
		}
		try {
			Class<?> sensorClass = Class.forName(sensorClazz);
			if (sensorClass.isAnnotationPresent(SensorAnnotation.class)) {
				int retId = sensorClass.getAnnotation(SensorAnnotation.class).id();
				sensorIdCache.put(sensorClazz, retId);
				return retId;
			}
		} catch (ClassNotFoundException e) {
			return -1;
		}
		return -1;
	}

}
