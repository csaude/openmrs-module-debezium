package org.openmrs.module.debezium;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.junit.Test;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;

import io.debezium.connector.mysql.MySqlConnector;
import io.debezium.relational.history.FileDatabaseHistory;

public class DebeziumConfigTest {
	
	private class TestDebeziumConfig extends DebeziumConfig {}
	
	final String storageFilename = "./offset.txt";
	
	final String host = "localhost";
	
	final Integer port = 3306;
	
	final String username = "root";
	
	final String password = "test";
	
	final String historyFilename = "./history.txt";
	
	final MySqlSnapshotMode snapshotMode = MySqlSnapshotMode.SCHEMA_ONLY;
	
	String tablesToWatch = "patient,person,visit";
	
	protected void setCoreProperties(DebeziumConfig config) {
		String tablesToWatch = "patient,person,visit";
		config.setConnectorClass(MySqlConnector.class);
		config.setOffsetStorageClass(FileOffsetBackingStore.class);
		config.setOffsetStorageFilename(storageFilename);
		config.setHost(host);
		config.setPort(port);
		config.setUsername(username);
		config.setPassword(password);
		config.setHistoryClass(FileDatabaseHistory.class);
		config.setHistoryFilename(historyFilename);
		config.setSnapshotMode(snapshotMode);
	}
	
	protected void assertCoreProperties(Properties props) {
		assertEquals(13, props.size());
		assertEquals(ConfigPropertyConstants.ENGINE_DEFAULT_NAME, props.get(ConfigPropertyConstants.ENGINE_PROP_NAME));
		assertEquals(ConfigPropertyConstants.ENGINE_DEFAULT_DB_SERVER_NAME,
		    props.get(ConfigPropertyConstants.ENGINE_PROP_DB_SERVER_NAME));
		assertEquals(MySqlConnector.class.getName(), props.get(ConfigPropertyConstants.ENGINE_PROP_CONNECT_CLASS));
		assertEquals(FileOffsetBackingStore.class.getName(),
		    props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_STORAGE_CLASS));
		assertEquals(storageFilename, props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_STORAGE_FILE));
		assertEquals("0", props.get(ConfigPropertyConstants.ENGINE_PROP_OFF_SET_FLUSH_INTERVAL_MS));
		assertEquals(host, props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_HOST));
		assertEquals(port.toString(), props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_PORT));
		assertEquals(username, props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_USERNAME));
		assertEquals(password, props.get(ConfigPropertyConstants.CONNECTOR_PROP_DB_PASSWORD));
		assertEquals(FileDatabaseHistory.class.getName(), props.get(ConfigPropertyConstants.CONNECTOR_PROP_HISTORY_CLASS));
		assertEquals(historyFilename, props.get(ConfigPropertyConstants.CONNECTOR_PROP_HISTORY_FILE));
		assertEquals(snapshotMode.getPropertyValue(), props.get(ConfigPropertyConstants.CONNECTOR_PROP_SNAPSHOT_MODE));
	}
	
	@Test
	public void getProperties_shouldReturnTheProperties() {
		TestDebeziumConfig config = new TestDebeziumConfig();
		setCoreProperties(config);
		assertCoreProperties(config.getProperties());
	}
	
}
