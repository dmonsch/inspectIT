package rocks.inspectit.agent.instrument.util;

import java.io.File;
import java.io.IOException;

import rocks.inspectit.android.instrument.util.UnzipUtility;

/**
 * @author David Monschein
 *
 */
public class UnzipTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File f = new File("C:\\Users\\DMO\\Downloads\\BURGER KING® App_v3.7.1_apkpure.com.apk");

		UnzipUtility.unzip(f.getAbsolutePath(), "testapk");

	}

}
