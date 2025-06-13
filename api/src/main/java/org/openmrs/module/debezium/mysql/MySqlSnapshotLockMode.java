package org.openmrs.module.debezium.mysql;

/**
 * Enumeration for {@link SslMode} supported by MySQL, for more details please refer to
 * https://debezium.io/documentation/reference/1.6/connectors/mysql.html#mysql-property-snapshot-locking-mode
 */
public enum MySqlSnapshotLockMode implements SnapshotLockMode {
	
	/**
	 * Blocks all writes for the duration of the snapshot. Use this setting if there are clients that
	 * are submitting operations that MySQL excludes from REPEATABLE READ semantics.
	 */
	EXTENDED("extended"),
	
	/**
	 * The connector holds the global read lock for only the initial portion of the snapshot during
	 * which the connector reads the database schemas and other metadata. The remaining work in a
	 * snapshot involves selecting all rows from each table. The connector can do this in a consistent
	 * fashion by using a REPEATABLE READ transaction. This is the case even when the global read lock
	 * is no longer held and other MySQL clients are updating the database.
	 */
	MINIMAL("minimal");
	
	private String propertyValue;
	
	MySqlSnapshotLockMode(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public String getPropertyValue() {
		return propertyValue;
	}
	
}
