package rocks.inspectit.agent.java.util;

import java.util.HashMap;

public class AutoboxingHelper {

	private static final HashMap<String, Class<?>> primitiveTypeClasses = new HashMap<String, Class<?>>();
	private static final HashMap<String, Class<?>> primitiveTypeWrappers = new HashMap<String, Class<?>>();

	static {
		primitiveTypeClasses.put(byte.class.getName(), byte.class);
		primitiveTypeClasses.put(short.class.getName(), short.class);
		primitiveTypeClasses.put(int.class.getName(), int.class);
		primitiveTypeClasses.put(long.class.getName(), long.class);
		primitiveTypeClasses.put(float.class.getName(), float.class);
		primitiveTypeClasses.put(double.class.getName(), double.class);
		primitiveTypeClasses.put(boolean.class.getName(), boolean.class);
		primitiveTypeClasses.put(char.class.getName(), char.class);

		primitiveTypeWrappers.put(byte.class.getName(), Byte.class);
		primitiveTypeWrappers.put(short.class.getName(), Short.class);
		primitiveTypeWrappers.put(int.class.getName(), Integer.class);
		primitiveTypeWrappers.put(long.class.getName(), Long.class);
		primitiveTypeWrappers.put(float.class.getName(), Float.class);
		primitiveTypeWrappers.put(double.class.getName(), Double.class);
		primitiveTypeWrappers.put(boolean.class.getName(), Boolean.class);
		primitiveTypeWrappers.put(char.class.getName(), Character.class);
	}

	/**
	 * Returns the class object for normal types. Return the primitive type for int,long,etc (the
	 * Integer.TYPE and so on)
	 * 
	 * @param name
	 * @param initialize
	 * @param classloader
	 * @throws ClassNotFoundException
	 */
	public static Class<?> findClass(String name, boolean initialize, ClassLoader classloader) throws ClassNotFoundException {
		if (isPrimitiveType(name)) {
			return primitiveTypeClasses.get(name);
		} else {
			return Class.forName(name, initialize, classloader);
		}
	}

	public static boolean isPrimitiveType(String name) {
		return primitiveTypeClasses.containsKey(name);
	}

	/**
	 * Returns true if the name equals the name of the primtive "void" return type (NOT THE
	 * Void.class)"
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isVoid(String name) {
		return void.class.getName().equals(name);
	}

	public static Class<?> getWrapperClass(String primName) {
		return primitiveTypeWrappers.get(primName);
	}

	public static Class<?> getWrapperClass(Class<?> returnType) {
		return getWrapperClass(returnType.getName());
	}

}
