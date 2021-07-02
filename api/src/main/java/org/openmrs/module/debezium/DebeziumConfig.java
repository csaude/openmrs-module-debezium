package org.openmrs.module.debezium;

import java.util.Properties;
import java.util.Set;

import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.apache.kafka.connect.storage.OffsetBackingStore;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;

import io.debezium.connector.mysql.MySqlConnector;
import io.debezium.relational.history.DatabaseHistory;
import io.debezium.relational.history.FileDatabaseHistory;

/**
 * Base class for debezium configuration classes
 */
public abstract class DebeziumConfig {
	
	//Engine properties
	private Class<? extends SourceConnector> connectorClass = MySqlConnector.class;
	
	private Class<? extends OffsetBackingStore> offsetStorageClass = FileOffsetBackingStore.class;
	
	private String offsetStorageFilename;
	
	//Connector properties
	private String host;
	
	private Integer port;
	
	private String username;
	
	private String password;
	
	private String databaseName;
	
	private Class<? extends DatabaseHistory> historyClass = FileDatabaseHistory.class;
	
	private String historyFilename;
	
	private SnapshotMode snapshotMode = MySqlSnapshotMode.INITIAL;
	
	private Set<String> tablesToWatch;
	
	private void validate() {
		//validate
	}
	
	/**
	 * Returns a {@link Properties} instance with the keys as the actual debezium property names and the
	 * values as the form in which they should be passed to engine and the connector.
	 * 
	 * @return Properties instance
	 */
	protected Properties getProperties() {
		validate();
		return null;
	}
	
	/**
	 * Sets the class for the connector
	 * 
	 * @param connectorClass connector class to set
	 */
	protected DebeziumConfig connectorClass(Class<? extends SourceConnector> connectorClass) {
		this.connectorClass = connectorClass;
		return this;
	}
	
	/**
	 * Sets the class that is responsible for persistence of connector offsets minute.
	 * 
	 * @param offsetStorageClass off set storage class to set
	 */
	protected DebeziumConfig offsetStorageClass(Class<? extends OffsetBackingStore> offsetStorageClass) {
		this.offsetStorageClass = offsetStorageClass;
		return this;
	}
	
	/**
	 * Sets the path to file where offsets are to be stored. Required when offsetStorage is set to the
	 * 
	 * @param offsetStorageFilename path to filename to set
	 */
	protected DebeziumConfig offsetStorageFilename(String offsetStorageFilename) {
		this.offsetStorageFilename = offsetStorageFilename;
		return this;
	}
	
	/**
	 * Sets the host name for the OpenMRS database
	 * 
	 * @param host the host name to set
	 * @return
	 */
	protected DebeziumConfig host(String host) {
		this.host = host;
		return this;
	}
	
	/**
	 * Sets the port number for the OpenMRS database
	 * 
	 * @param port port number to set
	 * @return
	 */
	protected DebeziumConfig port(Integer port) {
		this.port = port;
		return this;
	}
	
	/**
	 * Sets the username for the OpenMRS database
	 * 
	 * @param username username to set
	 * @return
	 */
	protected DebeziumConfig username(String username) {
		this.username = username;
		return this;
	}
	
	/**
	 * Sets the password for the OpenMRS database
	 * 
	 * @param password password to set
	 * @return
	 */
	protected DebeziumConfig password(String password) {
		this.password = password;
		return this;
	}
	
	/**
	 * Sets the name for the OpenMRS database
	 * 
	 * @param databaseName database name to set
	 * @return
	 */
	protected DebeziumConfig databaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}
	
	/**
	 * Sets the history class
	 * 
	 * @param historyClass history class to set
	 * @return
	 */
	protected DebeziumConfig historyClass(Class<? extends DatabaseHistory> historyClass) {
		this.historyClass = historyClass;
		return this;
	}
	
	/**
	 * Sets the history filename
	 * 
	 * @param historyFilename history filename to set
	 * @return
	 */
	protected DebeziumConfig historyFilename(String historyFilename) {
		this.historyFilename = historyFilename;
		return this;
	}
	
	/**
	 * Sets the snapshot mode
	 * 
	 * @param snapshotMode snapshot mode to set
	 * @return
	 */
	protected DebeziumConfig snapshotMode(SnapshotMode snapshotMode) {
		this.snapshotMode = snapshotMode;
		return this;
	}
	
	/**
	 * Sets the set of names for the table to watch
	 * 
	 * @param tablesToWatch
	 * @return
	 */
	protected DebeziumConfig tablesToWatch(Set<String> tablesToWatch) {
		this.tablesToWatch = tablesToWatch;
		return this;
	}
	
	/**
	 * Gets the connectorClass
	 *
	 * @return the connectorClass
	 */
	public Class<? extends SourceConnector> getConnectorClass() {
		return connectorClass;
	}
	
	/**
	 * Gets the offsetStorage class
	 *
	 * @return the offsetStorage class
	 */
	public Class<? extends OffsetBackingStore> getOffsetStorageClass() {
		return offsetStorageClass;
	}
	
	/**
	 * Gets the offsetStorageFilename
	 *
	 * @return the offsetStorageFilename
	 */
	public String getOffsetStorageFilename() {
		return offsetStorageFilename;
	}
	
	/**
	 * Gets the host
	 *
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Gets the port
	 *
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}
	
	/**
	 * Gets the username
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets the password
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Gets the databaseName
	 *
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
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
	 * Gets the historyFilename
	 *
	 * @return the historyFilename
	 */
	public String getHistoryFilename() {
		return historyFilename;
	}
	
	/**
	 * Gets the snapshotMode
	 *
	 * @return the snapshotMode
	 */
	public SnapshotMode getSnapshotMode() {
		return snapshotMode;
	}
	
	/**
	 * Gets the tablesToWatch
	 *
	 * @return the tablesToWatch
	 */
	public Set<String> getTablesToWatch() {
		return tablesToWatch;
	}
	
}
