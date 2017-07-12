package rocks.inspectit.android.instrument.core;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author David Monschein
 *
 */
public class DxJarProxy {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(DxJarProxy.class);

	private File dxLocation;

	public DxJarProxy(File dxLocation) {
		this.dxLocation = dxLocation;
	}

	public void createDexFromFolder(File rootDirectory, File resultingDex) {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", dxLocation.getAbsolutePath(), "--dex", "--output", resultingDex.getAbsolutePath(), rootDirectory.getAbsolutePath());

		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);

		try {
			pb.start().waitFor();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			LOG.error("Failed to execute dx tool.");
		}
	}

	// TODO reformat
	public File mergeDexFiles(File input1, File input2, File output) {
		URLClassLoader loader = null;
		try {
			// DexMerger.main(new String[]{mergedDex.getAbsolutePath(), inputFile, ellaRuntime});
			loader = new URLClassLoader(new URL[] { dxLocation.toURI().toURL() });
			Class<?> dexMergerClass = loader.loadClass("com.android.dx.merge.DexMerger");
			java.lang.reflect.Method mainMethod = dexMergerClass.getDeclaredMethod("main", (new String[0]).getClass());

			mainMethod.invoke(null, (Object) new String[] { output.getAbsolutePath(), input1.getAbsolutePath(), input2.getAbsolutePath() });
			return output;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} finally {
			if (loader != null) {
				try {
					loader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

}
