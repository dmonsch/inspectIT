package rocks.inspectit.android.instrument.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;
import rocks.inspectit.android.instrument.dex.DexInstrumenter;
import rocks.inspectit.android.instrument.util.AndroidManifestParser;
import rocks.inspectit.shared.all.util.Pair;

/**
 * Main class which schedules and performs the instrumentation of an Android application.
 *
 * @author David Monschein
 *
 */
public class APKInstrumenter {
	/** Temporary file for building the current agent version. */
	private static final File AGENT_BUILD = new File("dxbuild/agent.dex");

	/** Temporary output folder for rezipping the application. */
	private static final File OUTPUT_TEMP = new File("temp");

	/** Temporary output folder for unzipping the application. */
	private static final File OUTPUT_TEMPO = new File("tempo");

	private static final File DEX_FILES_PATH = new File("temp-dexs");

	// _________________________________________________ //

	/** Logger. */
	private static final Logger LOG = Logger.getLogger(APKInstrumenter.class.getName());

	/** Path which is used to save a modified manifest file. */
	private static final File MODIFIED_MANIFEST = new File("modified_manifest.xml");

	/** Flags if we override an existing output or not. */
	private boolean override;

	/** Path to the keystore used to resign the application. */
	private File keystore;

	/** Alias of the keystore. */
	private String alias;

	/** Password for the keystore. */
	private String pass;

	/** Permissions which are needed by the agent. */
	private List<String> neededRights;

	/** Flags whether to cleanup before instrumentation. */
	private boolean cleanBefore = true;

	/** Flags whether to cleanup after instrumentation. */
	private boolean cleanAfter = true;

	/** Flags whether to adjust the manifest or not. (recommended) */
	private boolean adjustManifest = true;

	private boolean buildAgent = true;

	private DxJarProxy dxProxy;

	/**
	 * Creates a new instance for instrumenting Android applications.
	 *
	 * @param override
	 *            whether to override existing files or not
	 * @param keystore
	 *            the path to the keystore
	 * @param alias
	 *            the alias of the keystore
	 * @param pass
	 *            the password of the keystore
	 */
	public APKInstrumenter(boolean override, File keystore, String alias, String pass) {
		this.override = override;
		this.keystore = keystore;
		this.alias = alias;
		this.pass = pass;

		dxProxy = new DxJarProxy(new File("lib/dx.jar"));
	}

	/**
	 * Instruments an android application.
	 *
	 * @param input
	 *            the input application
	 * @param output
	 *            the path where to save the instrumented application
	 * @return true if success - false otherwise
	 * @throws IOException
	 *             if there is an I/O problem
	 * @throws ZipException
	 *             if zip4j can't zip/unzip your application files
	 * @throws KeyStoreException
	 *             if there is a problem with the used keystore
	 * @throws NoSuchAlgorithmException
	 *             algorithm not found
	 * @throws CertificateException
	 *             certificate error
	 * @throws URISyntaxException
	 *             URI syntax problem
	 */
	public boolean instrumentAPK(File input, File output, File agentJar)
			throws IOException, ZipException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		// CHECK IF INPUT EXISTS AND OUTPUT DOESNT
		if (!input.exists() || (output.exists() && !override)) {
			return false;
		}

		// LOAD CONFIGURATION
		final InstrumentationConfiguration instrConfig = new InstrumentationConfiguration();
		try {
			instrConfig.parseConfigFile(APKInstrumenter.class.getResourceAsStream("/config/default.xml"));
		} catch (JAXBException e) {
			LOG.severe("Failed to load configuration.");
			return false;
		}

		// INSERT RIGHTS NEEDED
		this.neededRights = instrConfig.getXmlConfiguration().getManifestTransformer().getPermissions();

		// CLEAN
		if (cleanBefore) {
			cleanUpAll();
		}

		// IF OVERRIDE DELETE IT
		if (output.exists()) {
			if (!output.delete()) {
				return false;
			}
		}

		// BUILD OUR AGENT AS DEX
		if (!AGENT_BUILD.exists()) {
			if (!buildAgent) {
				return false;
			} else {
				buildDexAgent(agentJar, instrConfig);
			}
		} else {
			if (buildAgent) {
				AGENT_BUILD.delete();
				buildDexAgent(agentJar, instrConfig);
			}
		}

		// COPY INPUT TO OUTPUT
		final File tempOutputFolder = OUTPUT_TEMPO;
		tempOutputFolder.mkdir();
		final ZipFile tempInputZip = new ZipFile(input);
		tempInputZip.extractAll(tempOutputFolder.getAbsolutePath() + File.separator);

		LOG.info("Created copy of original APK file.");

		// ADD MANIFEST ADJUSTMENTS
		if (adjustManifest) {
			LOG.info("Decoding applications manifest.");

			// get input
			File maniInput = new File(tempOutputFolder.getAbsolutePath() + File.separator + "AndroidManifest.xml");

			// process output
			byte[] nManifest = AndroidManifestParser.adjustXml(FileUtils.readFileToByteArray(maniInput), neededRights);

			// write output
			FileUtils.writeByteArrayToFile(MODIFIED_MANIFEST, nManifest);

			LOG.info("Finished adjustment of the manifest.");
		}

		// TEMP WRITE DEX
		List<File> dexsToInstrument = unzipDexs(input, DEX_FILES_PATH);

		LOG.info("Searched and extracted dex files.");

		// INSTRUMENTATION OF DEX
		LOG.info("Started instrumentation of dex files.");
		DexInstrumenter dexInstrumenter = new DexInstrumenter(instrConfig);
		List<Pair<String, File>> instrumentedDexs = new ArrayList<>();
		for (File tempDex : dexsToInstrument) {
			File instrumentedTempDex = new File(tempDex.getAbsolutePath().substring(0, tempDex.getAbsolutePath().length() - 4) + "-instrumented.dex");
			dexInstrumenter.instrument(tempDex, instrumentedTempDex);
			instrumentedDexs.add(new Pair<>(tempDex.getName(), instrumentedTempDex));
		}
		LOG.info("Successfully instrumented dex files.");

		// MERGE DEXS
		LOG.info("Merging agent and app dex's.");
		// merge agent with instrumented dex
		boolean injected = false;
		int nCount = 1;

		for (Pair<String, File> instrDex : instrumentedDexs) {
			File tempFile = File.createTempFile("classes-and-agent", ".dex");
			try {
				dxProxy.mergeDexFiles(instrDex.getSecond(), AGENT_BUILD, tempFile);
				Files.move(tempFile.toPath(), instrDex.getSecond().toPath(), StandardCopyOption.REPLACE_EXISTING);
				injected = true;
				break;
			} catch (InvocationTargetException e) {
				LOG.warning("Merging with agent failed (Multidex problem).");
			} finally {
				tempFile.delete();
			}
			nCount++;
		}

		if (!injected) {
			// NOT TESTED YET
			LOG.warning("Created an additional dex file. Please make sure that mutlidex is enabled.");
			String fName = "classes" + nCount + ".dex";
			File newDex = new File(DEX_FILES_PATH + File.separator + fName);
			Files.move(AGENT_BUILD.toPath(), newDex.toPath(), StandardCopyOption.REPLACE_EXISTING);

			instrumentedDexs.add(new Pair<>(fName, newDex));
		}

		// dxProxy.mergeDexFiles(AGENT_BUILD, INSTRUMENTED_DEX, TEMP_DEX_NEW);
		LOG.info("Finished merging the dex's.");

		// EDIT OLD classes.dex and remove META INF
		LOG.info("Removing old signature.");
		FileUtils.deleteDirectory(new File(tempOutputFolder.getAbsolutePath() + File.separator + "META-INF"));

		// MOVE NEW DEX TO FOLDER
		String oldDexBasePath = tempOutputFolder.getAbsolutePath() + File.separator;

		for (Pair<String, File> instrDex : instrumentedDexs) {
			File oldDex = new File(oldDexBasePath + instrDex.getFirst());
			oldDex.delete();

			Files.move(instrDex.getSecond().toPath(), oldDex.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		// MODIFY MANIFEST
		if (adjustManifest) {
			LOG.info("Adjusting Android manifest.");
			final File oManifest = new File(tempOutputFolder.getAbsolutePath() + "/AndroidManifest.xml");
			oManifest.delete();
			Files.copy(MODIFIED_MANIFEST.toPath(), oManifest.toPath());
		}

		// GENERATE XML CONFIG AND ADD IT
		File assetsFolder = new File(tempOutputFolder.getAbsolutePath() + File.separator + "assets");
		if (!assetsFolder.exists()) {
			assetsFolder.mkdir();
		}

		AgentConfiguration derivedAgentConfig = createAgentConfiguration(instrConfig);

		ObjectMapper tempMapper;
		tempMapper = new ObjectMapper();
		ObjectWriter tempWriter = tempMapper.writer(new DefaultPrettyPrinter());
		tempWriter.writeValue(new File(assetsFolder.getAbsolutePath() + File.separator + "inspectit_agent_config.json"), derivedAgentConfig);

		// REZIP
		LOG.info("Rezip all files to output APK.");
		rezipOutput(output, tempOutputFolder);

		// RESIGN
		LOG.info("Resigning the output APK.");
		final JarSigner signer = new JarSigner();
		signer.signJar(output, keystore, alias, pass);

		// REMOVE ALL TEMP FILES
		if (cleanAfter) {
			cleanUpAll();
		}

		LOG.info("Instrumentation finished.");
		LOG.info("Instrumented application is located at '" + output.getAbsolutePath() + "'.");

		return true;
	}

	private AgentConfiguration createAgentConfiguration(InstrumentationConfiguration config) {
		AgentConfiguration n = new AgentConfiguration();
		n.setBeaconUrl(config.getXmlConfiguration().getConnectionInfo().getBeaconUrl());
		n.setSessionUrl(config.getXmlConfiguration().getConnectionInfo().getHelloUrl());
		n.setLogTag("Android Agent");
		n.setCollectLocation(false);
		return n;
	}

	private void buildDexAgent(File jarPath, InstrumentationConfiguration instrConfig) {
		LOG.info("Building the agent.");

		// compile the java files
		if (!jarPath.exists()) {
			LOG.severe("Please build the agent before running the instrumentation!");
			cleanUpAll();
			System.exit(1);
		}

		File temporaryFolder = new File("agent-build/");
		temporaryFolder.mkdir();

		try {
			ZipFile agentZip = new ZipFile(jarPath);
			agentZip.extractAll(temporaryFolder.getAbsolutePath() + "/");

		} catch (ZipException e) {
			e.printStackTrace();
			LOG.severe("Couldn't unzip the agent.");
		}

		// delete all except of rocks folder
		List<String> agentFolders = instrConfig.getXmlConfiguration().getAgentBuildConfiguration().getLibraryFolders();
		for (File toDel : temporaryFolder.listFiles()) {
			if (toDel.isDirectory()) {
				if (!agentFolders.contains(toDel.getName())) {
					try {
						FileUtils.deleteDirectory(toDel);
					} catch (IOException e) {
						LOG.severe("Couldn't delete temporary folder.");
					}
				}
			} else {
				toDel.delete();
			}
		}

		// dx transformation
		AGENT_BUILD.getParentFile().mkdirs();
		dxProxy.createDexFromFolder(temporaryFolder, AGENT_BUILD);

		// remove it
		try {
			FileUtils.deleteDirectory(temporaryFolder);
		} catch (IOException e) {
			LOG.severe("Couldn't remove temporary folder.");
		}

		LOG.info("Finished building the agent.");
	}

	/**
	 * Rezips a folder to a zip file.
	 *
	 * @param output
	 *            the zip file to create
	 * @param tempOutputFolder
	 *            the folder which should be zipped
	 * @throws ZipException
	 *             if zip4j fails
	 */
	private void rezipOutput(final File output, final File tempOutputFolder) throws ZipException {
		final ZipFile zipOutput = new ZipFile(output);
		addFolderToZipNoParent(zipOutput, tempOutputFolder);
	}

	/**
	 * Adds a folder to a zip file without the parent folder.
	 *
	 * @param zip
	 *            the zip file
	 * @param folder
	 *            the folder to add
	 * @throws ZipException
	 *             if there is an I/O problem
	 */
	private void addFolderToZipNoParent(final ZipFile zip, final File folder) throws ZipException {
		final ZipParameters parameters = new ZipParameters();
		for (File fInner : folder.listFiles()) {
			if (fInner.isDirectory()) {
				zip.addFolder(fInner, parameters);
			} else {
				zip.addFile(fInner, parameters);
			}
		}
	}

	/**
	 * Cleans all folders and files temporary used for instrumentation.
	 *
	 * @throws IOException
	 *             if not all files and folders could be removed successfully
	 */
	private void cleanUpAll() {
		LOG.info("Cleaning up all folders.");

		try {
			FileUtils.deleteDirectory(OUTPUT_TEMP);
			FileUtils.deleteDirectory(OUTPUT_TEMPO);
			FileUtils.deleteDirectory(DEX_FILES_PATH);
			FileUtils.deleteDirectory(new File("dxbuild")); // agent build
			FileUtils.forceDelete(new File("AndroidManifest.xml"));
		} catch (IOException e) {
			LOG.warning("Couldn't remove the temporary folders.");
		}

		MODIFIED_MANIFEST.delete();
	}

	private List<File> unzipDexs(File apk, File baseDexPath) throws IOException {
		baseDexPath.mkdirs();

		List<File> dexs = new ArrayList<>();
		byte[] buffer = new byte[1024];

		java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(new FileInputStream(apk));

		Pattern dexPattern = Pattern.compile("^(classes[0-9]*?\\.dex)$");

		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null) {
			Matcher matcher = dexPattern.matcher(entry.getName());
			if (matcher.find()) {
				File file = new File(baseDexPath.getAbsolutePath() + "/" + matcher.group(1));
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);

				int len;
				while ((len = zip.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();

				dexs.add(file);
			}
		}

		zip.close();

		return dexs;
	}
}
