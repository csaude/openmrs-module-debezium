package org.openmrs.module.debezium.mysql;

import static java.util.Arrays.stream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.debezium.mysql.MySqlSnapshotMode.SCHEMA_ONLY;
import static org.openmrs.module.debezium.mysql.MysqlConfigPropertyConstants.CONNECTOR_PROP_TABLE_EXCLUDE_LIST;
import static org.openmrs.module.debezium.mysql.MysqlConfigPropertyConstants.CONNECTOR_PROP_TABLE_INCLUDE_LIST;

import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.Test;
import org.openmrs.module.debezium.config.BaseDebeziumConfigTest;

import io.debezium.storage.file.history.FileSchemaHistory;

public class MySqlDebeziumConfigTest extends BaseDebeziumConfigTest {
	
	private static final String HISTORY_FILE = "./history.txt";
	
	private static final String TABLE_LOCATION = "location";
	
	private static final String TABLE_PERSON = "person";
	
	private static final String TABLE_ENC_TYPE = "encounter_type";
	
	private static final String tablesToInclude = TABLE_LOCATION + "," + TABLE_PERSON;
	
	private static final String tablesToExclude = TABLE_ENC_TYPE + "," + TABLE_PERSON;
	
	private final String database = "testDb";
	
	@Test
	public void getProperties_shouldReturnTheMySqlProperties() {
		MySqlDebeziumConfig config = new MySqlDebeziumConfig(SCHEMA_ONLY,
		        stream(tablesToInclude.split(",")).collect(Collectors.toSet()), null);
		setCoreProperties(config);
		config.setDatabaseName(database);
		config.setSnapshotLockMode(MySqlSnapshotLockMode.EXTENDED);
		config.setSslMode(MySqlSslMode.DISABLED);
		config.setHistoryClass(FileSchemaHistory.class);
		config.setHistoryFilename(HISTORY_FILE);
		
		Properties props = config.getProperties();
		
		assertCoreProperties(props, 21);
		assertEquals(database, props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_INCLUDE_LIST));
		assertEquals(MySqlSnapshotLockMode.EXTENDED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SNAPSHOT_LOCKING_MODE));
		assertEquals(MySqlSslMode.DISABLED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SSL_MODE));
		assertEquals("false", props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_INCLUDE_SCHEMA_CHANGES));
		assertEquals(FileSchemaHistory.class.getName(),
		    props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_CLASS));
		assertEquals(HISTORY_FILE, props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_FILE));
		assertEquals(2, props.getProperty(CONNECTOR_PROP_TABLE_INCLUDE_LIST).split(",").length);
		assertTrue(props.getProperty(CONNECTOR_PROP_TABLE_INCLUDE_LIST).contains(TABLE_LOCATION));
		assertTrue(props.getProperty(CONNECTOR_PROP_TABLE_INCLUDE_LIST).contains(TABLE_PERSON));
	}
	
	@Test
	public void getProperties_shouldReturnTheMySqlPropertiesWithTablesToExclude() {
		MySqlDebeziumConfig config = new MySqlDebeziumConfig(SCHEMA_ONLY, null,
		        stream(tablesToExclude.split(",")).collect(Collectors.toSet()));
		setCoreProperties(config);
		config.setDatabaseName(database);
		config.setSnapshotLockMode(MySqlSnapshotLockMode.EXTENDED);
		config.setSslMode(MySqlSslMode.DISABLED);
		config.setHistoryClass(FileSchemaHistory.class);
		config.setHistoryFilename(HISTORY_FILE);
		
		Properties props = config.getProperties();
		
		assertCoreProperties(props, 21);
		assertEquals(database, props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_INCLUDE_LIST));
		assertEquals(MySqlSnapshotLockMode.EXTENDED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SNAPSHOT_LOCKING_MODE));
		assertEquals(MySqlSslMode.DISABLED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SSL_MODE));
		assertEquals("false", props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_INCLUDE_SCHEMA_CHANGES));
		assertEquals(FileSchemaHistory.class.getName(),
		    props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_CLASS));
		assertEquals(HISTORY_FILE, props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_FILE));
		assertEquals(2, props.getProperty(CONNECTOR_PROP_TABLE_EXCLUDE_LIST).split(",").length);
		assertTrue(props.getProperty(CONNECTOR_PROP_TABLE_EXCLUDE_LIST).contains(TABLE_ENC_TYPE));
		assertTrue(props.getProperty(CONNECTOR_PROP_TABLE_EXCLUDE_LIST).contains(TABLE_PERSON));
	}
	
}
