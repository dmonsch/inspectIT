package rocks.inspectit.agent.java.eum;

import java.io.BufferedReader;
import java.util.Map;


public class W_HttpServletRequest {

	private static final String CLAZZ = "javax.servlet.http.HttpServletRequest";

	private static final CachedMethod<String> getRequestURI = new CachedMethod<String>(CLAZZ,"getRequestURI");
	private static final CachedMethod<Map<java.lang.String,java.lang.String[]>> getParameterMap = new CachedMethod<Map<java.lang.String,java.lang.String[]>>(CLAZZ,"getParameterMap");
	private static final CachedMethod<BufferedReader> getReader = new CachedMethod<BufferedReader>(CLAZZ, "getReader");
	private static final CachedMethod<Object[]> getCookies = new CachedMethod<Object[]>(CLAZZ, "getCookies");

	private Object instance;

	private W_HttpServletRequest(Object inst) {
		this.instance = inst;
	}

	public static W_HttpServletRequest wrap(Object request) {
		return new W_HttpServletRequest(request);
	}

	public static boolean isInstance(Object instance) {
		return ClassLoaderAwareClassCache.isInstance(instance, CLAZZ);
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

	public Object[] getCookies() {
		return getCookies.callSafe(instance);
	}
}
