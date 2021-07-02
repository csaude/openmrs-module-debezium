package org.openmrs.module.debezium;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

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
		config.connectorClass(MySqlConnector.class);
		config.offsetStorageClass(FileOffsetBackingStore.class);
		config.offsetStorageFilename(storageFilename);
		config.host(host);
		config.port(port);
		config.username(username);
		config.password(password);
		config.historyClass(FileDatabaseHistory.class);
		config.historyFilename(historyFilename);
		config.snapshotMode(snapshotMode);
		config.tablesToWatch(Arrays.stream(tablesToWatch.split(",")).collect(Collectors.toSet()));
	}
	
	protected void assertCoreProperties(Properties props) {
		assertEquals(13, props.size());
		assertEquals(ConfigPropertyConstants.ENGINE_DEFAULT_NAME, props.get(ConfigPropertyConstants.ENGINE_PROP_NAME));
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
		assertEquals(tablesToWatch, props.get(ConfigPropertyConstants.CONNECTOR_PROP_TABLES_INCLUDE_LIST));
	}
	
	@Test
	public void getProperties_shouldReturnTheProperties() {
		TestDebeziumConfig config = new TestDebeziumConfig();
		setCoreProperties(config);
		assertCoreProperties(config.getProperties());
	}
	
}
