package rocks.inspectit.agent.android.instrument;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * @author David Monschein
 *
 */
public class InstrumentationTask extends DefaultTask {

	private static final String INSPECTIT_MOBILE_BUNDLE = "";

	@TaskAction
	public void action() {
		// TODO install dependencies

		// load plugin settings
		InstrumentationExtension extension = getProject().getExtensions().findByType(InstrumentationExtension.class);
		if (extension == null) {
			extension = new InstrumentationExtension(); // => default values
		}

		// get apk file path
		Path buildDir = Paths.get(this.getProject().getBuildDir().toURI());

		Path targetApk = Paths.get(buildDir.toString(), extension.getInputApk());

		String usedPath = null;
		if (targetApk.toFile().exists()) {
			usedPath = targetApk.toString();
		}

		if (usedPath != null) {
			File instrumenter = new File("inspectit-android-instrument-all.jar");

			ProcessBuilder pb;
			if (extension.isOverride()) {
				pb = new ProcessBuilder("java", "-jar", instrumenter.getAbsolutePath(), "-w", "-k", extension.getKeystorePath(), "-a",
						extension.getKeystoreAlias(), "-p", extension.getKeystorePassword(), "-o", extension.getInstrumentedApk(), "-f", usedPath, "-t", extension.getAgentPath());
			} else {
				pb = new ProcessBuilder("java", "-jar", instrumenter.getAbsolutePath(), "-k", extension.getKeystorePath(), "-a",
						extension.getKeystoreAlias(), "-p", extension.getKeystorePassword(), "-o", extension.getInstrumentedApk(), "-f", usedPath, "-t", extension.getAgentPath());
			}

			try {
				Process proc = pb.start();
				proc.waitFor();
			} catch (InterruptedException | IOException e) {
				this.setDidWork(false);
			}
		} else {
			this.setDidWork(false);
		}
	}

	private boolean downloadDependencies() {
		try {
			FileUtils.copyURLToFile(new URL(INSPECTIT_MOBILE_BUNDLE), new File("temp.zip"));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
