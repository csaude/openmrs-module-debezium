package org.openmrs.module.debezium.mysql;

import java.util.Set;

import org.openmrs.module.debezium.DebeziumConfig;

/**
 * Debezium configuration for the MySQL connector
 */
public class MySqlDebeziumConfig extends DebeziumConfig {
	
	private MySqlSnapshotLockMode snapshotLockMode = MySqlSnapshotLockMode.EXTENDED;
	
	private MySqlSslMode sslMode = MySqlSslMode.PREFERRED;
	
	private boolean includeSchemaChanges = false;
	
	private Set<String> tablesToInclude;
	
	private Set<String> tablesToExclude;
	
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
	 */
	public void setSnapshotLockMode(MySqlSnapshotLockMode snapshotLockMode) {
		this.snapshotLockMode = snapshotLockMode;
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
	 */
	public void setSslMode(MySqlSslMode sslMode) {
		this.sslMode = sslMode;
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
	 */
	public void setIncludeSchemaChanges(boolean includeSchemaChanges) {
		this.includeSchemaChanges = includeSchemaChanges;
	}
	
	/**
	 * Gets the tablesToInclude
	 *
	 * @return the tablesToInclude
	 */
	public Set<String> getTablesToInclude() {
		return tablesToInclude;
	}
	
	/**
	 * Sets the tablesToInclude
	 *
	 * @param tablesToInclude the tablesToInclude to set
	 */
	public void setTablesToInclude(Set<String> tablesToInclude) {
		this.tablesToInclude = tablesToInclude;
	}
	
	/**
	 * Gets the tablesToExclude
	 *
	 * @return the tablesToExclude
	 */
	public Set<String> getTablesToExclude() {
		return tablesToExclude;
	}
	
	/**
	 * Sets the tablesToExclude
	 *
	 * @param tablesToExclude the tablesToExclude to set
	 */
	public void setTablesToExclude(Set<String> tablesToExclude) {
		this.tablesToExclude = tablesToExclude;
	}
}
