package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

public class W_HttpServletRequest {

	private static final String CLAZZ = "javax.servlet.http.HttpServletRequest";
	private static final ConcurrentHashMap<ClassLoader, Class<?>> clazzCache = new ConcurrentHashMap<ClassLoader, Class<?>>();

	private static final CachedMethod<String> getRequestURI = new CachedMethod<String>(CLAZZ, "getRequestURI");
	private static final CachedMethod<Map<java.lang.String, java.lang.String[]>> getParameterMap = new CachedMethod<Map<java.lang.String, java.lang.String[]>>(CLAZZ, "getParameterMap");
	private static final CachedMethod<BufferedReader> getReader = new CachedMethod<BufferedReader>(CLAZZ, "getReader");
	private static final CachedMethod<Cookie[]> getCookies = new CachedMethod<Cookie[]>(CLAZZ, "getCookies");
	private static final CachedMethod<HttpSession> getSession = new CachedMethod<HttpSession>(CLAZZ, "getSession");

	private final Object instance;

	private W_HttpServletRequest(Object inst) {
		this.instance = inst;
	}

	public static W_HttpServletRequest wrap(Object request) {
		return new W_HttpServletRequest(request);
	}

	public static boolean isInstance(Object instance) {
		ClassLoader cl = instance.getClass().getClassLoader();
		if (!clazzCache.containsKey(cl)) {
			try {
				clazzCache.putIfAbsent(cl, Class.forName(CLAZZ, false, cl));
			} catch (ClassNotFoundException e) {
				return false;
			}
		}
		return clazzCache.get(cl).isInstance(instance);
	}

	public String getRequestURI() {
		return getRequestURI.callSafe(instance);
	}

	public Map<java.lang.String, java.lang.String[]> getParameterMap() {
		return getParameterMap.callSafe(instance);
	}

	public BufferedReader getReader() {
		return getReader.callSafe(instance);
	}

	public Cookie[] getCookies() {
		return getCookies.callSafe(instance);
	}

	public HttpSession getSession() {
		return getSession.callSafe(instance);
	}
}
