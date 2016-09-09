package rocks.inspectit.agent.java.eum;


import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

public class W_HttpServletResponse {

	private static final String CLAZZ = "javax.servlet.http.HttpServletResponse";

	private static final CachedMethod<Void> setStatus = new CachedMethod<Void>(CLAZZ,"setStatus",int.class);
	private static final CachedMethod<Void> setContentType = new CachedMethod<Void>(CLAZZ,"setContentType",String.class);
	private static final CachedMethod<String> getContentType = new CachedMethod<String>(CLAZZ,"getContentType");
	private static final CachedMethod<Void> setContentLength = new CachedMethod<Void>(CLAZZ,"setContentLength",int.class);
	private static final CachedMethod<Void> setCharacterEncoding = new CachedMethod<Void>(CLAZZ,"setCharacterEncoding",String.class);
	private static final CachedMethod<String> getCharacterEncoding = new CachedMethod<String>(CLAZZ,"getCharacterEncoding");
	private static final CachedMethod<Void> setLocale = new CachedMethod<Void>(CLAZZ,"setLocale",Locale.class);
	private static final CachedMethod<PrintWriter> getWriter = new CachedMethod<PrintWriter>(CLAZZ,"getWriter");
	private static final CachedMethod<OutputStream> getOutputStream = new CachedMethod<OutputStream>(CLAZZ,"getOutputStream");
	private static final CachedMethod<Void> addCookie = new CachedMethod<Void>(CLAZZ, "addCookie", "javax.servlet.http.Cookie");
	private static final CachedMethod<Void> addHeader = new CachedMethod<Void>(CLAZZ, "addHeader", String.class, String.class);

	private Object instance;

	private W_HttpServletResponse(Object inst) {
		this.instance = inst;
	}

	public static boolean isInstance(Object instance) {
		return ClassLoaderAwareClassCache.isInstance(instance, CLAZZ);
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

	/**
	 * @param cookieToSet
	 */
	public void addCookie(Object cookieToSet) {
		addCookie.callSafe(instance, cookieToSet);
	}

	/**
	 * @param headerName
	 * @param headerValue
	 */
	public void addHeader(String headerName, String headerValue) {
		addHeader.callSafe(instance, headerName, headerValue);
	}

}
