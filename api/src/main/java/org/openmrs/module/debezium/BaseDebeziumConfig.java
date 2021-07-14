package org.openmrs.module.debezium;

import java.util.Properties;

import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.apache.kafka.connect.storage.OffsetBackingStore;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;

import io.debezium.connector.mysql.MySqlConnector;

/**
 * Base class for debezium configuration classes
 */
public abstract class BaseDebeziumConfig {
	
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
	
	private SnapshotMode snapshotMode = MySqlSnapshotMode.INITIAL;
	
	/**
	 * Returns a {@link Properties} instance with the keys as the actual debezium property names and the
	 * values as the form in which they should be passed to engine and the connector.
	 * 
	 * @return Properties instance
	 */
	public Properties getProperties() {
		final Properties props = new Properties();
		props.setProperty(ConfigPropertyConstants.ENGINE_PROP_NAME, ConfigPropertyConstants.ENGINE_DEFAULT_NAME);
		props.setProperty(ConfigPropertyConstants.ENGINE_PROP_DB_SERVER_NAME,
		    ConfigPropertyConstants.ENGINE_DEFAULT_DB_SERVER_NAME);
		props.setProperty(ConfigPropertyConstants.ENGINE_PROP_CONNECT_CLASS, getConnectorClass().getName());
		props.setProperty(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_STORAGE_CLASS, getOffsetStorageClass().getName());
		if (FileOffsetBackingStore.class.equals(getOffsetStorageClass())) {
			props.setProperty(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_STORAGE_FILE, getOffsetStorageFilename());
		}
		props.setProperty(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_FLUSH_INTERVAL_MS, "0");
		
		//Common connector properties
		props.setProperty(ConfigPropertyConstants.CONNECTOR_PROP_DB_HOST, getHost());
		props.setProperty(ConfigPropertyConstants.CONNECTOR_PROP_DB_PORT, getPort().toString());
		props.setProperty(ConfigPropertyConstants.CONNECTOR_PROP_DB_USERNAME, getUsername());
		props.setProperty(ConfigPropertyConstants.CONNECTOR_PROP_DB_PASSWORD, getPassword());
		props.setProperty(ConfigPropertyConstants.CONNECTOR_PROP_SNAPSHOT_MODE, getSnapshotMode().getPropertyValue());
		//props.setProperty("snapshot.fetch.size", "10240");
		props.setProperty(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_FLUSH_TIMEOUT_MS, "15000");
		
		return props;
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
	 * Sets the connectorClass
	 *
	 * @param connectorClass the connectorClass to set
	 */
	public void setConnectorClass(Class<? extends SourceConnector> connectorClass) {
		this.connectorClass = connectorClass;
	}
	
	/**
	 * Gets the offsetStorageClass
	 *
	 * @return the offsetStorageClass
	 */
	public Class<? extends OffsetBackingStore> getOffsetStorageClass() {
		return offsetStorageClass;
	}
	
	/**
	 * Sets the offsetStorageClass
	 *
	 * @param offsetStorageClass the offsetStorageClass to set
	 */
	public void setOffsetStorageClass(Class<? extends OffsetBackingStore> offsetStorageClass) {
		this.offsetStorageClass = offsetStorageClass;
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
	 * Sets the offsetStorageFilename
	 *
	 * @param offsetStorageFilename the offsetStorageFilename to set
	 */
	public void setOffsetStorageFilename(String offsetStorageFilename) {
		this.offsetStorageFilename = offsetStorageFilename;
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
	 * Sets the host
	 *
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
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
	 * Sets the port
	 *
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
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
	 * Sets the username
	 *
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
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
	 * Sets the password
	 *
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
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
	 * Sets the databaseName
	 *
	 * @param databaseName the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
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
	 * Sets the snapshotMode
	 *
	 * @param snapshotMode the snapshotMode to set
	 */
	public void setSnapshotMode(SnapshotMode snapshotMode) {
		this.snapshotMode = snapshotMode;
	}
}
