package rocks.inspectit.agent.java.eum;

public class W_Cookie {

	private static final String CLAZZ = "javax.servlet.http.Cookie";

	private static final CachedConstructor constructor = new CachedConstructor(CLAZZ, String.class, String.class);

	private static final CachedMethod<String> getName = new CachedMethod<String>(CLAZZ, "getName");
	private static final CachedMethod<String> getValue = new CachedMethod<String>(CLAZZ, "getValue");
	private static final CachedMethod<Void> setMaxAge = new CachedMethod<Void>(CLAZZ, "setMaxAge", int.class);
	private static final CachedMethod<Void> setPath = new CachedMethod<Void>(CLAZZ, "setPath", String.class);

	private Object instance;

	private W_Cookie(Object inst) {
		this.instance = inst;
	}

	public static W_Cookie wrap(Object cookie) {
		return new W_Cookie(cookie);
	}


	public static boolean isInstance(Object instance) {
		return ClassLoaderAwareClassCache.isInstance(instance, CLAZZ);
	}

	public static Object newInstance(ClassLoader cl, String name, String value) {
		return constructor.newInstanceSafe(cl, name, value);
	}

	public String getName() {
		return getName.callSafe(instance);
	}

	public String getValue() {
		return getValue.callSafe(instance);
	}

	public void setMaxAge(int maxAgeSeconds) {
		setMaxAge.callSafe(instance, maxAgeSeconds);
	}

	public void setPath(String path) {
		setPath.callSafe(instance, path);
	}

}
