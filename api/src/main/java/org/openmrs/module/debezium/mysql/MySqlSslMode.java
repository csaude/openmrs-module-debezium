package org.openmrs.module.debezium.mysql;

/**
 * Enumeration for {@link SslMode} supported by MySQL, for more details please refer to
 * https://debezium.io/documentation/reference/1.6/connectors/mysql.html#mysql-property-database-ssl-mode
 */
public enum MySqlSslMode implements SslMode {
	
	/**
	 * Establishes an encrypted connection if the server supports secure connections. If the server does
	 * not support secure connections, falls back to an unencrypted connection.
	 */
	PREFERRED("preferred"),
	
	/**
	 * Establishes an encrypted connection or fails if one cannot be made for any reason.
	 */
	REQUIRED("required"),
	
	/**
	 * Specifies the use of an unencrypted connection.
	 */
	DISABLED("disabled"),
	
	/**
	 * Behaves like required but additionally it verifies the server TLS certificate against the
	 * configured Certificate Authority (CA) certificates and fails if the server TLS certificate does
	 * not match any valid CA certificates.
	 */
	VERIFY_CA("verify_ca"),
	
	/**
	 * Behaves like verify_ca but additionally verifies that the server certificate matches the host of
	 * the remote connection.
	 */
	VERIFY_IDENTITY("verify_identity");
	
	private String propertyValue;
	
	MySqlSslMode(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public String getPropertyValue() {
		return propertyValue;
	}
	
}
