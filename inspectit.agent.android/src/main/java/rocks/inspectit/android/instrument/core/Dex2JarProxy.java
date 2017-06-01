package rocks.inspectit.android.instrument.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Proxy class for using the dex2jar tooling.
 *
 * @author David Monschein
 *
 */
public final class Dex2JarProxy {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(Dex2JarProxy.class);

	/** Path to the dex2jar library folder. */
	private static final String DEX2JAR_BASEPATH = "lib/dex2jar-2.0/";

	/** Path to the jar2dex batch tool. */
	private static final String JAR2DEX_BATCH = "d2j-jar2dex.bat";

	/** Path to the jar2dex sh tool. */
	private static final String JAR2DEX_SH = "d2j-jar2dex.sh";

	/** Path to the dex2jar sh tool. */
	private static final String DEX2JAR_SH = "d2j-dex2jar.sh";

	/** Path to the dex2jar batch tool. */
	private static final String DEX2JAR_BATCH = "d2j-dex2jar.bat";

	/**
	 * No instance creation allowed because static tooling class.
	 */
	private Dex2JarProxy() {
	}

	/**
	 * Creates a jar file from a dex file.
	 *
	 * @param dex
	 *            the dex file
	 * @param output
	 *            the path where to store the jar
	 * @return true if success - false otherwise
	 */
	public static boolean createJarFromDex(final File dex, final File output) {
		if (!dex.exists() || output.exists()) {
			return false;
		}

		return executeTool(DEX2JAR_BATCH, DEX2JAR_SH, dex, output);
	}

	/**
	 * Creates a dex file from a given jar file.
	 *
	 * @param jar
	 *            the jar file
	 * @param dex
	 *            the path where to store the dex
	 * @return true if success - false otherwise
	 */
	public static boolean createDexFromJar(final File jar, final File dex) {
		if (!jar.exists() || dex.exists()) {
			return false;
		}

		return executeTool(JAR2DEX_BATCH, JAR2DEX_SH, jar, dex);
	}

	/**
	 * Executes a given dex2jar sub tool.
	 *
	 * @param windows
	 *            path to the windows tool
	 * @param unix
	 *            path to the unix tool
	 * @param input
	 *            input file
	 * @param output
	 *            output file
	 * @return true if sucess - false otherwise
	 */
	private static boolean executeTool(final String windows, final String unix, final File input, final File output) {
		ProcessBuilder pb = null;
		if (SystemUtils.IS_OS_WINDOWS) {
			// use the batch
			pb = new ProcessBuilder(DEX2JAR_BASEPATH + windows, input.getAbsolutePath(), "-o",
					output.getAbsolutePath());
		} else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
			// use the sh
			pb = new ProcessBuilder(DEX2JAR_BASEPATH + unix, input.getAbsolutePath(), "-o", output.getAbsolutePath());
		}

		if (pb != null) {
			try {
				pb.start().waitFor();
			} catch (IOException | InterruptedException e) {
				LOG.error("Failed to execute dex2jar or other similar subtooling.");
				return false;
			}

			return true;
		} else {
			return false;
		}
	}

}
