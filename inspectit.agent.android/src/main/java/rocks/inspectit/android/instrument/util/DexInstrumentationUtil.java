package rocks.inspectit.android.instrument.util;

/**
 * @author David Monschein
 *
 */
public class DexInstrumentationUtil {

	// UTILS
	public static String getType(Class<?> clz) {
		return "L" + clz.getName().replaceAll("\\.", "/") + ";";
	}

}
