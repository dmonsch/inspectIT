package rocks.inspectit.android.instrument.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.android.instrument.DexInstrumenter;
import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;

/**
 * Main class which schedules and performs the instrumentation of an Android
 * application.
 *
 * @author David Monschein
 *
 */
public class APKInstrumenter {
	// DOWNLOAD LINKS
	private static final String DX_RELEASE = "https://github.com/pxb1988/dex2jar/releases/download/2.0/dex-tools-2.0.zip";
	private static final String APKTOOL_RELEASE_2_2_2 = "https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.2.2.jar";

	// TEMPORARY USED FILES
	private static final File INSTRUMENTED_DEX = new File("temp-new-instr.dex");

	private static final File AGENT_BUILD_JAVA = new File("../inspectit.agent.android/build/release/inspectit.agent.android-all.jar");

	/** Temporary file for building the current agent version. */
	private static final File AGENT_BUILD = new File("dxbuild/agent.dex");

	/** Temporary output folder for rezipping the application. */
	private static final File OUTPUT_TEMP = new File("temp");

	/** Temporary output folder for unzipping the application. */
	private static final File OUTPUT_TEMPO = new File("tempo");

	/** New created dex file. */
	private static final File TEMP_DEX_NEW = new File("temp-new.dex");

	/** Temporary extracted dex file. */
	private static final File TEMP_DEX_OLD = new File("temp-dex.dex");

	// _________________________________________________ //

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(APKInstrumenter.class);

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

	private boolean downloadLibs = true;

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
	public APKInstrumenter(final boolean override, final File keystore, final String alias, final String pass) {
		this.setOverride(override);
		this.setKeystore(keystore);
		this.setAlias(alias);
		this.setPass(pass);

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
	public boolean instrumentAPK(final File input, final File output) throws IOException, ZipException,
	KeyStoreException, NoSuchAlgorithmException, CertificateException, URISyntaxException {
		// CHECK IF INPUT EXISTS AND OUTPUT DOESNT
		if (!input.exists() || (output.exists() && !override)) {
			return false;
		}

		// LOAD CONFIGURATION
		final InstrumentationConfiguration instrConfig = new InstrumentationConfiguration();
		final File instrConfigFile = new File(APKInstrumenter.class.getResource("/config/default.xml").toURI());
		try {
			instrConfig.parseConfigFile(instrConfigFile);
		} catch (JAXBException e) {
			LOG.error("Failed to load configuration.");
			return false;
		}
		instrConfig.loadInstrumentationPoints();

		LOG.info("Successfully loaded instrumentation config '" + instrConfigFile.getAbsolutePath() + "'.");

		// DOWNLOAD LIBS
		if (downloadLibs) {
			final File DXTOOL = new File("lib/dx.jar");
			final File APKTOOL = new File("lib/apktool.jar");

			if (!DXTOOL.exists() || !APKTOOL.exists()) {
				LOG.info("Downloading belonging libraries.");
				downloadLibraries();
				LOG.info("Finished downloading libraries.");
			}
		}

		// INSERT RIGHTS NEEDED
		this.neededRights = instrConfig.getXmlConfiguration().getManifestTransformer().getPermissions();

		// CLEAN
		if (cleanBefore) {
			cleanUpAll();
		}

		// ADD MANIFEST ADJUSTMENTS
		if (adjustManifest) {
			LOG.info("Decoding application with APKTool.");

			final APKToolProxy apkTool = new APKToolProxy(input);
			final boolean b1 = apkTool.decodeAPK("intermediate");
			final boolean b2 = apkTool.adjustManifest(neededRights, MODIFIED_MANIFEST);

			if (!b1 || !b2) {
				adjustManifest = false;
			} else {
				instrConfig.setApplicationPackage(apkTool.getPackageName());
			}
			apkTool.cleanup();

			LOG.info("APKTool finished decoding the application.");
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
				buildDexAgent();
			}
		} else {
			if (buildAgent) {
				AGENT_BUILD.delete();
				buildDexAgent();
			}
		}

		// COPY INPUT TO OUTPUT
		final File tempOutputFolder = OUTPUT_TEMPO;
		tempOutputFolder.mkdir();
		final ZipFile tempInputZip = new ZipFile(input);
		tempInputZip.extractAll(tempOutputFolder.getAbsolutePath() + "/");

		LOG.info("Created copy of original APK file.");

		// TEMP WRITE DEX
		final File tempDex = TEMP_DEX_OLD;
		unzipDex(input, tempDex);

		LOG.info("Searched and extracted dex files.");

		// INSTRUMENTATION OF DEX
		LOG.info("Started instrumentation of dex files.");
		DexInstrumenter dexInstrumenter = new DexInstrumenter(instrConfig);
		dexInstrumenter.instrument(tempDex, INSTRUMENTED_DEX);
		LOG.info("Successfully instrumented dex files.");

		// MERGE DEXS
		LOG.info("Merging agent and app dex's.");
		// merge agent with instrumented dex
		dxProxy.mergeDexFiles(AGENT_BUILD, INSTRUMENTED_DEX, TEMP_DEX_NEW);
		LOG.info("Finished merging the dex's.");

		// EDIT OLD classes.dex and remove META INF
		LOG.info("Removing old signature.");
		FileUtils.deleteDirectory(new File(tempOutputFolder.getAbsolutePath() + File.separator + "META-INF"));

		// MOVE NEW DEX TO FOLDER
		File oldDex = new File(tempOutputFolder.getAbsolutePath() + File.separator + "classes.dex");
		oldDex.delete();
		Files.move(TEMP_DEX_NEW.toPath(), oldDex.toPath(), StandardCopyOption.REPLACE_EXISTING);

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
		signer.signJar(output, getKeystore(), getAlias(), getPass());

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
		return n;
	}

	private void buildDexAgent() {
		LOG.info("Building the agent.");

		// compile the java files
		if (!AGENT_BUILD_JAVA.exists()) {
			LOG.error("Please build the agent before running the instrumentation!");
			cleanUpAll();
			System.exit(1);
		}

		File temporaryFolder = new File("agent-build/");
		temporaryFolder.mkdir();

		try {
			ZipFile agentZip = new ZipFile(AGENT_BUILD_JAVA);
			agentZip.extractAll(temporaryFolder.getAbsolutePath() + "/");

		} catch (ZipException e) {
			e.printStackTrace();
			LOG.error("Couldn't unzip the agent.");
		}

		// delete all except of rocks folder
		for (File toDel : temporaryFolder.listFiles()) {
			if (toDel.isDirectory()) {
				if (!toDel.getName().equals("rocks") && !toDel.getName().equals("io")) {
					try {
						FileUtils.deleteDirectory(toDel);
					} catch (IOException e) {
						LOG.error("Couldn't delete temporary folder.");
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
			LOG.error("Couldn't remove temporary folder.");
		}

		LOG.info("Finished building the agent.");
	}

	/**
	 * @return the override
	 */
	public boolean isOverride() {
		return override;
	}

	/**
	 * @param override
	 *            the override to set
	 */
	public void setOverride(final boolean override) {
		this.override = override;
	}

	/**
	 * @return the keystore
	 */
	public File getKeystore() {
		return keystore;
	}

	/**
	 * @param keystore
	 *            the keystore to set
	 */
	public void setKeystore(final File keystore) {
		this.keystore = keystore;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(final String alias) {
		this.alias = alias;
	}

	/**
	 * @return the pass
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @param pass
	 *            the pass to set
	 */
	public void setPass(final String pass) {
		this.pass = pass;
	}

	/**
	 * @return the cleanBefore
	 */
	public boolean isCleanBefore() {
		return cleanBefore;
	}

	/**
	 * @param cleanBefore
	 *            the cleanBefore to set
	 */
	public void setCleanBefore(final boolean cleanBefore) {
		this.cleanBefore = cleanBefore;
	}

	/**
	 * @return the cleanAfter
	 */
	public boolean isCleanAfter() {
		return cleanAfter;
	}

	/**
	 * @param cleanAfter
	 *            the cleanAfter to set
	 */
	public void setCleanAfter(final boolean cleanAfter) {
		this.cleanAfter = cleanAfter;
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
		} catch (IOException e) {
			LOG.error("Couldn't remove the temporary folders.");
		}

		TEMP_DEX_NEW.delete();
		TEMP_DEX_OLD.delete();
		MODIFIED_MANIFEST.delete();
		INSTRUMENTED_DEX.delete();
	}

	private void downloadLibraries() {
		File apkToolDestination = new File("lib/apktool.jar");
		File dxToolDestination = new File("lib/dx.jar");

		apkToolDestination.getParentFile().mkdirs();
		dxToolDestination.getParentFile().mkdirs();

		try {
			FileUtils.copyURLToFile(new URL(APKTOOL_RELEASE_2_2_2), apkToolDestination);
			FileUtils.copyURLToFile(new URL(DX_RELEASE), dxToolDestination);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOG.warn("Couldn't download all libraries successfully.");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			LOG.warn("Couldn't download all libraries successfully.");
			return;
		}
	}

	/**
	 * Extracts only a the classes.dex file from an Android application.
	 *
	 * @param apk
	 *            the Android application
	 * @param dex
	 *            the path where the dex should be saved
	 * @throws ZipException
	 *             if zip4j fails
	 * @throws IOException
	 *             if there is an I/O problem
	 */
	private void unzipDex(final File apk, final File dex) throws ZipException, IOException {
		if (apk.exists() && !dex.exists()) {
			final ZipFile parent = new ZipFile(apk);

			@SuppressWarnings("unchecked")
			final List<FileHeader> headerList = parent.getFileHeaders();

			for (FileHeader header : headerList) {
				if (header.getFileName().equals("classes.dex")) {
					final ZipInputStream in = parent.getInputStream(header);
					final FileOutputStream os = new FileOutputStream(dex);
					int readLen = -1;
					final byte[] buff = new byte[4096];
					while ((readLen = in.read(buff)) != -1) {
						os.write(buff, 0, readLen);
					}
					closeStreams(in, os);
					break;
				}
			}
		}
	}

	/**
	 * Closes two input streams.
	 *
	 * @param a
	 *            a zip input stream
	 * @param b
	 *            a file output stream
	 * @throws IOException
	 *             if one of the streams can't be closed
	 */
	private void closeStreams(final ZipInputStream a, final FileOutputStream b) throws IOException {
		if (a != null) {
			a.close();
		}

		if (b != null) {
			b.close();
		}
	}
}
