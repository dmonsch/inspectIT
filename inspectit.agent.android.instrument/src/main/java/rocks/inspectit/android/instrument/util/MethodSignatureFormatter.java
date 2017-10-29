package rocks.inspectit.android.instrument.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Monschein
 *
 */
public class MethodSignatureFormatter {

	private static final Pattern METHOD_SIG_PATTERN = Pattern.compile("(L.*;)(.*)\\((.*?)\\)(.*)");

	public String formatSignature(String sig) {
		Matcher matcher = METHOD_SIG_PATTERN.matcher(sig);
		if (matcher.find()) {
			// format class type
			String clazzFormat = formatClassName(matcher.group(1), false);

			// format method name
			String methodName = matcher.group(2);
			if (methodName.contains("$")) {
				methodName = "<anonymous>";
			}

			// format method paramters
			String[] methodParas = matcher.group(3).split(",");
			for (int i = 0; i < methodParas.length; i++) {
				methodParas[i] = formatClassName(methodParas[i], true);
			}

			// format result type
			String resultType = formatClassName(matcher.group(4), false);

			return methodName + "(" + implode(methodParas, ", ", 0) + ") - " + clazzFormat;
		}

		return sig;
	}

	private String implode(String[] sp, String del, int from) {
		StringBuilder res = new StringBuilder(sp[from]);
		for (int k = from + 1; k < sp.length; k++) {
			res.append(del);
			res.append(sp[k]);
		}
		return res.toString();
	}

	private String formatClassName(String className, boolean simple) {
		if (className.equals("")) {
			return "";
		}
		if (className.length() == 1) {
			return getPrimitiveTypeStringRepresentation(className.charAt(0));
		}

		String clazzFormat = className.substring(1, className.length() - 1);
		String[] clazzFormatSplit = clazzFormat.split("\\$");
		if (clazzFormatSplit.length >= 2) {
			StringBuilder temp = new StringBuilder(simple ? simpleType(clazzFormatSplit[0]) : clazzFormatSplit[0]);
			for (int k = 1; k < clazzFormatSplit.length; k++) {
				if (!isNumeric(clazzFormatSplit[k])) {
					temp.append(".");
					temp.append(clazzFormatSplit[k]);
				}
			}
			return temp.toString().replaceAll("/", ".");
		}
		if (simple) {
			clazzFormat = simpleType(clazzFormat);
		}
		return clazzFormat.replaceAll("/", ".");
	}

	private String simpleType(String full) {
		String[] sp = full.split("/");
		return sp[sp.length - 1];
	}

	private String getPrimitiveTypeStringRepresentation(char cz) {
		switch (cz) {
		case 'I':
			return "int";
		case 'Z':
			return "boolean";
		case 'D':
			return "double";
		case 'F':
			return "float";
		case 'J':
			return "long";
		case 'S':
			return "short";
		case 'C':
			return "char";
		case 'V':
			return "void";
		default:
			return "unknown";
		}
	}

	private boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

}
