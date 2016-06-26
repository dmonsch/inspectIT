package rocks.inspectit.agent.java.eum;


import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class W_HttpServletResponse {

	private static final String CLAZZ = "javax.servlet.http.HttpServletResponse";
	private static final ConcurrentHashMap<ClassLoader, Class<?>> clazzCache = new ConcurrentHashMap<ClassLoader, Class<?>>();

	private static final CachedMethod<Void> setStatus = new CachedMethod<Void>(CLAZZ,"setStatus",int.class);
	private static final CachedMethod<Void> setContentType = new CachedMethod<Void>(CLAZZ,"setContentType",String.class);
	private static final CachedMethod<String> getContentType = new CachedMethod<String>(CLAZZ,"getContentType");
	private static final CachedMethod<Void> setContentLength = new CachedMethod<Void>(CLAZZ,"setContentLength",int.class);
	private static final CachedMethod<Void> setCharacterEncoding = new CachedMethod<Void>(CLAZZ,"setCharacterEncoding",String.class);
	private static final CachedMethod<String> getCharacterEncoding = new CachedMethod<String>(CLAZZ,"getCharacterEncoding");
	private static final CachedMethod<Void> setLocale = new CachedMethod<Void>(CLAZZ,"setLocale",Locale.class);
	private static final CachedMethod<PrintWriter> getWriter = new CachedMethod<PrintWriter>(CLAZZ,"getWriter");
	private static final CachedMethod<OutputStream> getOutputStream = new CachedMethod<OutputStream>(CLAZZ,"getOutputStream");

	private Object instance;

	private W_HttpServletResponse(Object inst) {
		this.instance = inst;
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

	public static W_HttpServletResponse wrap(Object request) {
		return new W_HttpServletResponse(request);
	}

	public void setStatus(int status) {
		setStatus.callSafe(instance, status);
	}

	public void setContentType(String conentType) {
		setContentType.callSafe(instance, conentType);

	}

	public String getContentType() {
		return getContentType.callSafe(instance);
	}

	public void setContentLength(int length) {
		setContentLength.callSafe(instance, length);
	}

	public void setCharacterEncoding(String encoding) {
		setCharacterEncoding.callSafe(instance, encoding);
	}

	public PrintWriter getWriter() {
		return getWriter.callSafe(instance);
	}


	public OutputStream getOutputStream() {
		return getOutputStream.callSafe(instance);
	}

	public void setLocale(Locale locale) {
		setLocale.callSafe(instance, locale);
	}


	public String getCharacterEncoding() {
		return getCharacterEncoding.callSafe(instance);
	}



	public Object getWrappedElement() {
		return instance;
	}



}
