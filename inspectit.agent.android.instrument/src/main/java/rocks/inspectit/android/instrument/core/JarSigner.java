package rocks.inspectit.android.instrument.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

/**
 * Proxy class for the Java Jarsigner. This needs the jarsigner tooling
 * installed on the machine.
 *
 * @see http://docs.oracle.com/javase/6/docs/technotes/tools/windows/jarsigner.html
 * @author David Monschein
 *
 */
public class JarSigner {

	/** Logger. */
	private static final Logger LOG = Logger.getLogger(JarSigner.class.getName());

	/** Creates a new Jarsigner instance. */
	public JarSigner() {
	}

	/**
	 * Signs a given jar file.
	 *
	 * @param jarFile
	 *            the jar file
	 * @param keyStore
	 *            the keystore file
	 * @param alias
	 *            the alias for the keystore
	 * @param pass
	 *            the password for the keystore
	 * @throws IOException
	 *             if there occured a problem while executing the jarsigner tool
	 */
	public void signJar(final File jarFile, final String keyStore, final String alias, final String pass)
			throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder("jarsigner", "-sigalg", "SHA1withRSA", "-digestalg",
				"SHA1", "-keystore", keyStore, jarFile.getAbsolutePath(), alias);

		final Process signJarProcess = processBuilder.start();

		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(signJarProcess.getOutputStream()));

		writer.write(pass, 0, pass.length());
		writer.newLine();
		writer.close();

		try {
			signJarProcess.waitFor();
		} catch (InterruptedException e) {
			LOG.severe("JarSigner failed to resign file '" + jarFile.getAbsolutePath() + "' with keystore '" + keyStore
					+ "'.");
			return;
		}

	}

	/**
	 * Signs a given jar file.
	 *
	 * @param jarFile
	 *            the jar file
	 * @param keyStore
	 *            the keystore file
	 * @param alias
	 *            the alias for the keystore
	 * @param pass
	 *            the password for the keystore
	 * @throws IOException
	 *             if there occured a problem while executing the jarsigner tool
	 */
	public void signJar(final File jarFile, final File keyStore, final String alias, final String pass)
			throws IOException {
		signJar(jarFile, keyStore.getAbsolutePath(), alias, pass);
	}

}
