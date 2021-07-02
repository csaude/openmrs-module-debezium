package org.openmrs.module.debezium.mysql;

import org.openmrs.module.debezium.DebeziumConfig;

/**
 * Debezium configuration for the MySQL connector
 */
public class MySqlDebeziumConfig extends DebeziumConfig {
	
	private MySqlSnapshotLockMode snapshotLockMode = MySqlSnapshotLockMode.EXTENDED;
	
	private MySqlSslMode sslMode = MySqlSslMode.PREFERRED;
	
	private boolean includeSchemaChanges = false;
	
	/**
	 * Gets the snapshotLockMode
	 *
	 * @return the snapshotLockMode
	 */
	public MySqlSnapshotLockMode getSnapshotLockMode() {
		return snapshotLockMode;
	}
	
	/**
	 * Sets the snapshotLockMode
	 *
	 * @param snapshotLockMode the snapshotLockMode to set
	 * @return this instance
	 */
	public MySqlDebeziumConfig snapshotLockMode(MySqlSnapshotLockMode snapshotLockMode) {
		this.snapshotLockMode = snapshotLockMode;
		return this;
	}
	
	/**
	 * Gets the sslMode
	 *
	 * @return the sslMode
	 */
	public MySqlSslMode getSslMode() {
		return sslMode;
	}
	
	/**
	 * Sets the sslMode
	 *
	 * @param sslMode the sslMode to set
	 * @return this instance
	 */
	public MySqlDebeziumConfig sslMode(MySqlSslMode sslMode) {
		this.sslMode = sslMode;
		return this;
	}
	
	/**
	 * Gets the includeSchemaChanges
	 *
	 * @return the includeSchemaChanges
	 */
	public boolean isIncludeSchemaChanges() {
		return includeSchemaChanges;
	}
	
	/**
	 * Sets the includeSchemaChanges
	 *
	 * @param includeSchemaChanges the includeSchemaChanges to set
	 * @return this instance
	 */
	public MySqlDebeziumConfig includeSchemaChanges(boolean includeSchemaChanges) {
		this.includeSchemaChanges = includeSchemaChanges;
		return this;
	}
	
}
