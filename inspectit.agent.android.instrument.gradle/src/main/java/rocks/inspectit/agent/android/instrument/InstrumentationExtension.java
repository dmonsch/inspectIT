package rocks.inspectit.agent.android.instrument;

/**
 * @author David Monschein
 *
 */
public class InstrumentationExtension {

	private boolean override = true;

	private String keystorePath;

	private String keystoreAlias;

	private String keystorePassword;

	private String instrumentedApk;

	private String agentPath;

	private String inputApk;

	/**
	 * Gets {@link #override}.
	 *
	 * @return {@link #override}
	 */
	public boolean isOverride() {
		return override;
	}

	/**
	 * Sets {@link #override}.
	 *
	 * @param override
	 *            New value for {@link #override}
	 */
	public void setOverride(boolean override) {
		this.override = override;
	}

	/**
	 * Gets {@link #keystorePath}.
	 *
	 * @return {@link #keystorePath}
	 */
	public String getKeystorePath() {
		return keystorePath;
	}

	/**
	 * Sets {@link #keystorePath}.
	 *
	 * @param keystorePath
	 *            New value for {@link #keystorePath}
	 */
	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	/**
	 * Gets {@link #keystoreAlias}.
	 *
	 * @return {@link #keystoreAlias}
	 */
	public String getKeystoreAlias() {
		return keystoreAlias;
	}

	/**
	 * Sets {@link #keystoreAlias}.
	 *
	 * @param keystoreAlias
	 *            New value for {@link #keystoreAlias}
	 */
	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}

	/**
	 * Gets {@link #keystorePassword}.
	 *
	 * @return {@link #keystorePassword}
	 */
	public String getKeystorePassword() {
		return keystorePassword;
	}

	/**
	 * Sets {@link #keystorePassword}.
	 *
	 * @param keystorePassword
	 *            New value for {@link #keystorePassword}
	 */
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	/**
	 * Gets {@link #outputFile}.
	 *
	 * @return {@link #outputFile}
	 */
	public String getInstrumentedApk() {
		return instrumentedApk;
	}

	/**
	 * Sets {@link #outputFile}.
	 *
	 * @param outputFile
	 *            New value for {@link #outputFile}
	 */
	public void setInstrumentedApk(String instrumentedApk) {
		this.instrumentedApk = instrumentedApk;
	}

	/**
	 * Gets {@link #agentPath}.
	 *
	 * @return {@link #agentPath}
	 */
	public String getAgentPath() {
		return agentPath;
	}

	/**
	 * Sets {@link #agentPath}.
	 *
	 * @param agentPath
	 *            New value for {@link #agentPath}
	 */
	public void setAgentPath(String agentPath) {
		this.agentPath = agentPath;
	}

	/**
	 * Gets {@link #inputApk}.
	 *   
	 * @return {@link #inputApk}  
	 */ 
	public String getInputApk() {
		return inputApk;
	}

	/**  
	 * Sets {@link #inputApk}.  
	 *   
	 * @param inputApk  
	 *            New value for {@link #inputApk}  
	 */
	public void setInputApk(String inputApk) {
		this.inputApk = inputApk;
	}

}
