package rocks.inspectit.agent.android.sensor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Monschein
 *
 */
public class SensorUtil {

	private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("(.*?)\\(");

	public static String getMethodName(String methodSignature) {
		Matcher matcher = METHOD_NAME_PATTERN.matcher(methodSignature);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

}
