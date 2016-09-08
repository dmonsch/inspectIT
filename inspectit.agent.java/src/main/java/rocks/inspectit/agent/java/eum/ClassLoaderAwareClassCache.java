package rocks.inspectit.agent.java.eum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jonas Kunz
 *
 */
public class ClassLoaderAwareClassCache {


	private static ConcurrentHashMap<ClassLoader, ConcurrentHashMap<String, Class<?>>> cache = new ConcurrentHashMap<ClassLoader, ConcurrentHashMap<String, Class<?>>>();

	public static Class<?> lookupClass(String fqn, ClassLoader cl) {
		ConcurrentHashMap<String, Class<?>> clCache = cache.get(cl);
		if (clCache == null) {
			cache.putIfAbsent(cl, new ConcurrentHashMap<String, Class<?>>());
			clCache = cache.get(cl);
		}
		if (!clCache.containsKey(fqn)) {
			try {
				clCache.putIfAbsent(fqn, Class.forName(fqn, false, cl));
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
		return clCache.get(fqn);
	}

	public static Class<?> lookupClassRelative(String fqn, Object classLoaderSource) {
		return lookupClass(fqn, classLoaderSource.getClass().getClassLoader());
	}

	public static boolean isInstance(Object objToCheck, String fqn) {
		Class<?> clazz = lookupClass(fqn, objToCheck.getClass().getClassLoader());
		if (clazz == null) {
			return false;
		} else {
			return clazz.isInstance(objToCheck);
		}
	}

}
