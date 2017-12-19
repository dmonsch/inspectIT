package rocks.inspectit.shared.all.externalservice;

/**
 * Represents the external services which are available on the CMR.
 *
 * @author Marius Oehler
 *
 */
public enum ExternalServiceType {
	/**
	 * Influx database.
	 */
	INFLUXDB("InfluxDB"),

	/**
	 * An SMTP server.
	 */
	MAIL_SENDER("eMail Service"),

	/**
	 * Apache Cassandra Database.
	 */
	CASSANDRA("Cassandra"),

	/**
	 * ElasticSearch database.
	 */
	ELASTICSEARCH("ElasticSearch");

	/**
	 * The service name.
	 */
	private String name;

	/**
	 * Constructor.
	 *
	 * @param name
	 *            the name of the service
	 */
	ExternalServiceType(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #name}.
	 *
	 * @return {@link #name}
	 */
	public String getName() {
		return this.name;
	}
}