package org.openmrs.module.debezium.mysql;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.openmrs.module.debezium.BaseDebeziumConfig;

import io.debezium.relational.history.DatabaseHistory;
import io.debezium.relational.history.FileDatabaseHistory;

/**
 * Debezium configuration for the MySQL connector
 */
public class MySqlDebeziumConfig extends BaseDebeziumConfig {
	
	private MySqlSnapshotLockMode snapshotLockMode = MySqlSnapshotLockMode.EXTENDED;
	
	private MySqlSslMode sslMode = MySqlSslMode.PREFERRED;
	
	private Class<? extends DatabaseHistory> historyClass = FileDatabaseHistory.class;
	
	private String historyFilename;
	
	private Set<String> tablesToInclude;
	
	private Set<String> tablesToExclude;
	
	@Override
	public Properties getProperties() {
		Properties props = super.getProperties();
		props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_INCLUDE_LIST, getDatabaseName());
		props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SNAPSHOT_LOCKING_MODE,
		    getSnapshotLockMode().getPropertyValue());
		props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SSL_MODE, getSslMode().getPropertyValue());
		props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_INCLUDE_SCHEMA_CHANGES, "false");
		//props.setProperty("max.batch.size", "1");
		props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_CLASS, getHistoryClass().getName());
		if (FileDatabaseHistory.class.equals(getHistoryClass())) {
			props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_FILE, getHistoryFilename());
		}
		
		if (getTablesToInclude() != null) {
			props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_TABLE_INCLUDE_LIST, String.join(",",
			    getTablesToInclude().stream().map(t -> getDatabaseName() + "." + t).collect(Collectors.toSet())));
		}
		
		if (getTablesToExclude() != null) {
			props.setProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_TABLE_EXCLUDE_LIST, String.join(",",
			    getTablesToExclude().stream().map(t -> getDatabaseName() + "." + t).collect(Collectors.toSet())));
		}
		
		return props;
	}
	
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
	 * Gets the historyClass
	 *
	 * @return the historyClass
	 */
	public Class<? extends DatabaseHistory> getHistoryClass() {
		return historyClass;
	}
	
	/**
	 * Sets the historyClass
	 *
	 * @param historyClass the historyClass to set
	 */
	public void setHistoryClass(Class<? extends DatabaseHistory> historyClass) {
		this.historyClass = historyClass;
	}
	
	/**
	 * Gets the historyFilename
	 *
	 * @return the historyFilename
	 */
	public String getHistoryFilename() {
		return historyFilename;
	}
	
	/**
	 * Sets the historyFilename
	 *
	 * @param historyFilename the historyFilename to set
	 */
	public void setHistoryFilename(String historyFilename) {
		this.historyFilename = historyFilename;
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
