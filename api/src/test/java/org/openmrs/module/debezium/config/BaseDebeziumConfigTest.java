package org.openmrs.module.debezium.config;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;

import io.debezium.connector.mysql.MySqlConnector;
import org.openmrs.module.debezium.utils.CustomFileOffsetBackingStore;

public abstract class BaseDebeziumConfigTest {
	
	protected final String storageFilename = "./offset.txt";
	
	protected final Long serverId = 3L;
	
	protected final String host = "localhost";
	
	protected final Integer port = 3306;
	
	protected final String username = "root";
	
	protected final String password = "test";
	
	protected final MySqlSnapshotMode snapshotMode = MySqlSnapshotMode.SCHEMA_ONLY;
	
	protected void setCoreProperties(BaseDebeziumConfig config) {
		config.setOffsetStorageClass(CustomFileOffsetBackingStore.class);
		config.setOffsetStorageFilename(storageFilename);
		config.setServerId(serverId);
		config.setHost(host);
		config.setPort(port);
		config.setUsername(username);
		config.setPassword(password);
		config.setSnapshotMode(snapshotMode);
	}
	
	protected void assertCoreProperties(Properties props, int expectedCount) {
		assertEquals(expectedCount, props.size());
		assertEquals(ConfigPropertyConstants.ENGINE_DEFAULT_NAME, props.get(ConfigPropertyConstants.ENGINE_PROP_NAME));
		assertEquals(serverId.toString(), props.get(ConfigPropertyConstants.ENGINE_PROP_DB_SERVER_ID));
		assertEquals(MySqlConnector.class.getName(), props.get(ConfigPropertyConstants.ENGINE_PROP_CONNECT_CLASS));
		assertEquals(CustomFileOffsetBackingStore.class.getName(),
		    props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_STORAGE_CLASS));
		assertEquals(storageFilename, props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_STORAGE_FILE));
		assertEquals("0", props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_FLUSH_INTERVAL_MS));
		assertEquals("15000", props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_FLUSH_TIMEOUT_MS));
		assertEquals("false", props.get(ConfigPropertyConstants.CONNECTOR_PROP_TOMBSTONE_ON_DELETE));
		assertEquals(host, props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_HOST));
		assertEquals(port.toString(), props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_PORT));
		assertEquals(username, props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_USERNAME));
		assertEquals(password, props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_PASSWORD));
		assertEquals(snapshotMode.getPropertyValue(), props.get(ConfigPropertyConstants.CONNECTOR_PROP_SNAPSHOT_MODE));
	}
	
}
