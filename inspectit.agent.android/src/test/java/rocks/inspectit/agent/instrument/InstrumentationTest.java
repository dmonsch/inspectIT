package rocks.inspectit.agent.instrument;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import net.lingala.zip4j.exception.ZipException;
import rocks.inspectit.android.instrument.core.APKInstrumenter;

/**
 * @author David Monschein
 *
 */
public class InstrumentationTest {

	/**
	 * @param args
	 * @throws URISyntaxException
	 * @throws ZipException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, ZipException, URISyntaxException {
		final File toInstrument;
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("C:\\Users\\DMO\\Desktop\\Sonstiges\\apks"));
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public boolean accept(final File f) {
				if (f.isDirectory()) {
					return true;
				}

				if (FilenameUtils.getExtension(f.getAbsolutePath()).equals("apk")) {
					return true;
				}
				return false;
			}
		});
		fc.setAcceptAllFileFilterUsed(false);
		final int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			toInstrument = fc.getSelectedFile();
		} else {
			return;
		}

		APKInstrumenter instrumenter = new APKInstrumenter(true, new File("lib/release.keystore"), "androiddebugkey", "android");
		instrumenter.instrumentAPK(toInstrument, new File("app-instr.apk"));

	}

}
