package rocks.inspectit.android.instrument.core;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * Proxy for the APK tool which allows us to modify the original manifest file.
 *
 * @see https://ibotpeaches.github.io/Apktool/
 * @author David Monschein
 *
 */
public class APKToolProxy {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(APKToolProxy.class);

	/** Path to the library. */
	private static final String LIB_PATH = "lib\\apktool.jar";

	/** Name of the Android manifest file. */
	private static final String MANIFEST_FILE = "AndroidManifest.xml";

	/** Input Android application. */
	private File input;

	/** Name of the folder to where the application can be unzipped. */
	private String folderName;

	/** Name of the package provided by the application. */
	private String packageName;

	/**
	 * Creates a proxy for the APKTool.
	 *
	 * @param inputAPK
	 *            input application
	 */
	public APKToolProxy(final File inputAPK) {
		this.input = inputAPK;
		this.folderName = null;
		this.packageName = null;
	}

	/**
	 * Decodes the application to a specified folder.
	 *
	 * @param fName
	 *            the folder where the application files can be stored
	 *            temporarily.
	 * @return true if success - false otherwise
	 */
	public boolean decodeAPK(final String fName) {
		this.folderName = fName;
		// apktool d bar.apk -o baz
		final ProcessBuilder pb = new ProcessBuilder("java", "-jar", LIB_PATH, "d", input.getAbsolutePath(), "-o",
				folderName);

		try {
			pb.start().waitFor();
			return true;
		} catch (InterruptedException | IOException e) {
			LOG.error("Failed to execute APKTool to decode application.");
			return false;
		}
	}

	/**
	 * Adjust the manifest file of the application.
	 *
	 * @param rights
	 *            the rights which are mandatory
	 * @param modifiedManifest
	 *            the file where the adjusted manifest can be stored
	 * @return true if success - false otherwise
	 */
	public boolean adjustManifest(final List<String> rights, final File modifiedManifest) {
		if (folderName == null) {
			return false;
		}

		// read all rights
		final File manifestFile = new File(folderName + "/" + MANIFEST_FILE);
		if (!manifestFile.exists()) {
			return false;
		}

		final Set<String> exRights = new HashSet<String>();

		// PARSING XML
		final Document dom;
		// Make an instance of the DocumentBuilderFactory
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			final DocumentBuilder db = dbf.newDocumentBuilder();

			dom = db.parse(manifestFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			return false;
		}

		final Node manifestNode = dom.getElementsByTagName("manifest").item(0);
		this.packageName = manifestNode.getAttributes().getNamedItem("package").getTextContent();
		final NodeList permissionNodes = manifestNode.getChildNodes();

		for (int k = 0; k < permissionNodes.getLength(); k++) {
			final Node permNode = permissionNodes.item(k);
			if (permNode.getNodeName().equals("uses-permission")) {
				exRights.add(permNode.getAttributes().getNamedItem("android:name").getTextContent());
			}
		}

		// determine which to add
		for (String right : rights) {
			if (!exRights.contains(right)) {
				final Element nNode = dom.createElement("uses-permission");
				nNode.setAttribute("android:name", right);
				manifestNode.appendChild(nNode);
			}
		}

		// write content back to file
		try {
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(dom);
			final StreamResult result = new StreamResult(manifestFile);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			return false;
		}

		// RECOMPILE
		// apktool b bar -o new_bar.apk
		final File rebuildFile = new File(folderName + "/rebuild.apk");
		final ProcessBuilder pb = new ProcessBuilder("java", "-jar", LIB_PATH, "b", folderName, "-o",
				rebuildFile.getAbsolutePath());

		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);

		try {
			pb.start().waitFor();
		} catch (InterruptedException | IOException e) {
			LOG.error("Failed to rebuild apk with APKTool.");
			return false;
		}

		// UNZIP IT
		try {
			final ZipFile rebuildZip = new ZipFile(rebuildFile);
			rebuildZip.extractFile(MANIFEST_FILE, folderName + "/" + "manifest_new");
			Files.copy(new File(folderName + "/" + "manifest_new" + "/" + MANIFEST_FILE).toPath(),
					modifiedManifest.toPath());
		} catch (ZipException | IOException e) {
			LOG.error("Failed to extract the manifest from the rebuilt application.");
			return false;
		}

		return true;
	}

	/**
	 * Cleans temporary files.
	 *
	 * @throws IOException
	 *             if not all temp files could be deleted.
	 */
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(new File(folderName));
	}

	/**
	 * Gets the package name for the application.
	 *
	 * @return the package name for the application.
	 */
	public String getPackageName() {
		return packageName;
	}

}
