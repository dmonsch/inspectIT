package rocks.inspectit.agent.java.eum;


import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class W_ServletOutputStream {

	private static final String CLAZZ = "javax.servlet.ServletOutputStream";
	private static final ConcurrentHashMap<ClassLoader, Class<?>> clazzCache = new ConcurrentHashMap<ClassLoader, Class<?>>();

	private static final CachedMethod<Void> print_bool = new CachedMethod<Void>(CLAZZ,"print",boolean.class);
	private static final CachedMethod<Void> print_char = new CachedMethod<Void>(CLAZZ,"print",char.class);
	private static final CachedMethod<Void> print_double = new CachedMethod<Void>(CLAZZ,"print",double.class);
	private static final CachedMethod<Void> print_float = new CachedMethod<Void>(CLAZZ,"print",float.class);
	private static final CachedMethod<Void> print_int = new CachedMethod<Void>(CLAZZ,"print",int.class);
	private static final CachedMethod<Void> print_long = new CachedMethod<Void>(CLAZZ,"print",long.class);
	private static final CachedMethod<Void> print_string = new CachedMethod<Void>(CLAZZ,"print",String.class);

	private static final CachedMethod<Void> println = new CachedMethod<Void>(CLAZZ,"println");
	private static final CachedMethod<Void> println_bool = new CachedMethod<Void>(CLAZZ,"println",boolean.class);
	private static final CachedMethod<Void> println_char = new CachedMethod<Void>(CLAZZ,"println",char.class);
	private static final CachedMethod<Void> println_double = new CachedMethod<Void>(CLAZZ,"println",double.class);
	private static final CachedMethod<Void> println_float = new CachedMethod<Void>(CLAZZ,"println",float.class);
	private static final CachedMethod<Void> println_int = new CachedMethod<Void>(CLAZZ,"println",int.class);
	private static final CachedMethod<Void> println_long = new CachedMethod<Void>(CLAZZ,"println",long.class);
	private static final CachedMethod<Void> println_string = new CachedMethod<Void>(CLAZZ,"println",String.class);

	private static final CachedMethod<Boolean> isReady = new CachedMethod<Boolean>(CLAZZ,"isReady");
	private static final CachedMethod<Void> setWriteListener = new CachedMethod<Void>(CLAZZ,"setWriteListener", "javax.servlet.WriteListener");


	private Object instance;

	private W_ServletOutputStream(Object inst) {
		this.instance = inst;
	}

	public static W_ServletOutputStream wrap(Object request) {
		return new W_ServletOutputStream(request);
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

	public void print(boolean val) {
		print_bool.callSafe(instance, val);
	}

	public void print(char val) {
		print_char.callSafe(instance, val);
	}

	public void print(double val) {
		print_double.callSafe(instance, val);
	}

	public void print(float val) {
		print_float.callSafe(instance, val);
	}

	public void print(int val) {
		print_int.callSafe(instance, val);
	}

	public void print(long val) {
		print_long.callSafe(instance, val);
	}

	public void print(String val) {
		print_string.callSafe(instance, val);
	}

	public void println() {
		println.callSafe(instance);
	}

	public void println(boolean val) {
		println_bool.callSafe(instance, val);
	}

	public void println(char val) {
		println_char.callSafe(instance, val);
	}

	public void println(double val) {
		println_double.callSafe(instance, val);
	}

	public void println(float val) {
		println_float.callSafe(instance, val);
	}

	public void println(int val) {
		println_int.callSafe(instance, val);
	}

	public void println(long val) {
		println_long.callSafe(instance, val);
	}

	public void println(String val) {
		println_string.callSafe(instance, val);
	}

	public void close() throws IOException {
		((OutputStream)instance).close();
	}

	public void flush() throws IOException{
		((OutputStream)instance).flush();
	}

	public void write(byte[] b) throws IOException{
		((OutputStream)instance).write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException{
		((OutputStream)instance).write(b,off,len);
	}

	public void write(int b) throws IOException{
		((OutputStream)instance).write(b);
	}

	public boolean isReady() {
		return isReady.callSafe(instance);
	}

	public void setWriteListener(Object listener) {
		setWriteListener.callSafe(instance, listener);
	}

}
