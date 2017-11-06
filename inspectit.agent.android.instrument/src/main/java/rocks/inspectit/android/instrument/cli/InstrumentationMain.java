package rocks.inspectit.android.instrument.cli;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BooleanConverter;

import net.lingala.zip4j.exception.ZipException;
import rocks.inspectit.android.instrument.core.APKInstrumenter;

/**
 * @author David Monschein
 *
 */
public class InstrumentationMain {

	@Parameter(names = "--help", help = true)
	private boolean help;

	@Parameter(names = { "-w", "--override" }, required = false, description = "If the application is permitted to override existing files.", converter = BooleanConverter.class)
	private boolean override = false;

	@Parameter(names = { "-k", "--keystore" }, required = true, description = "Path to the keystore.")
	private String keystore;

	@Parameter(names = { "-a", "--keystore-alias" }, required = true, description = "Alias for the keystore.")
	private String alias;

	@Parameter(names = { "-p", "--keystore-password" }, required = true, description = "Password for the keystore.")
	private String password;

	@Parameter(names = { "-f", "--apkfile" }, required = true, description = "Path to the APK file to be instrumented.")
	private String apk;

	@Parameter(names = { "-o", "--output" }, required = true, description = "Path where the instrumented APK should be saved.")
	private String output;

	@Parameter(names = { "-t", "--agent" }, required = true, description = "Path to the inspectIT Android Agent jar.")
	private String agent;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InstrumentationMain main = new InstrumentationMain();
		JCommander commander = new JCommander(main);
		try {
			commander.parse(args);
			main.execute(commander);
		} catch (ParameterException e) {
			System.err.println(e.getLocalizedMessage());
			commander.usage();
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());
			commander.usage();
		}

	}

	private void execute(final JCommander commander) throws IOException {
		if (this.help) {
			commander.usage();
			System.exit(0);
		} else {
			File keystoreFile = new File(keystore);
			File apkFile = new File(apk);
			File outputFile = new File(output);
			File dexAgent = new File(agent);

			if (keystoreFile.exists() && apkFile.exists() && dexAgent.exists()) {
				APKInstrumenter instrumenter = new APKInstrumenter(override, keystoreFile, alias, password);
				try {
					instrumenter.instrumentAPK(apkFile, outputFile, dexAgent);
					System.exit(0);
				} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | ZipException e) {
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				System.err.println("One or multiple file paths are incorrect.");
			}
		}
	}

}
