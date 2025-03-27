package org.openmrs.module.debezium.mysql;

/**
 * Enumeration for {@link SnapshotMode} supported by MySQL, for more details please refer to
 * https://debezium.io/documentation/reference/1.6/connectors/mysql.html#mysql-property-snapshot-mode
 */
public enum MySqlSnapshotMode implements SnapshotMode {
	
	/**
	 * The connector runs a snapshot only when no offsets have been recorded for the logical server
	 * name.
	 */
	INITIAL("initial"),
	
	/**
	 * The connector runs a snapshot only when no offsets have been recorded for the logical server name
	 * and then stops; i.e. it will not read change events from the binlog
	 */
	INITIAL_ONLY("initial_only"),
	
	/**
	 * The connector runs a snapshot of the schemas and not the data. This setting is useful when you do
	 * not need the topics to contain a consistent snapshot of the data but need them to have only the
	 * changes since the connector was started.
	 */
	SCHEMA_ONLY("schema_only");
	
	private String propertyValue;
	
	MySqlSnapshotMode(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public String getPropertyValue() {
		return propertyValue;
	}
	
}
