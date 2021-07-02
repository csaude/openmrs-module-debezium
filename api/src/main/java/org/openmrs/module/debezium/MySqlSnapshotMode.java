package org.openmrs.module.debezium;

/**
 * {@link SnapshotMode} for MySQL, for possible values and their descriptions see
 * https://debezium.io/documentation/reference/1.6/connectors/mysql.html#mysql-property-snapshot-include-collection-list
 */
public enum MySqlSnapshotMode implements SnapshotMode {
	
	/**
	 * The connector runs a snapshot only when no offsets have been recorded for the logical server
	 * name.
	 */
	INITIAL("initial"),
	
	/**
	 * The connector runs a snapshot of the schemas and not the data. This setting is useful when you do
	 * not need the topics to contain a consistent snapshot of the data but need them to have only the
	 * changes since the connector was started.
	 */
	SCHEMA_ONLY("schema_only");
	
	private String connectorValue;
	
	private MySqlSnapshotMode(String connectorValue) {
		this.connectorValue = connectorValue;
	}
	
	@Override
	public String getConnectorValue() {
		return connectorValue;
	}
	
}
