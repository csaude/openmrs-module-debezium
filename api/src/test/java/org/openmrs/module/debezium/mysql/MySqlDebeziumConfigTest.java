package org.openmrs.module.debezium.mysql;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.BaseDebeziumConfigTest;
import org.openmrs.module.debezium.DebeziumConstants;

import io.debezium.relational.history.FileDatabaseHistory;

public class MySqlDebeziumConfigTest extends BaseDebeziumConfigTest {
	
	private final String historyFilename = "./history.txt";
	
	private final String tablesToInclude = "location,person";
	
	private final String tablesToExclude = "encounter_type,person";
	
	private static Properties originalProps;
	
	final String database = "testDb";
	
	final String url = "jdbc:mysql://" + host + ":" + port + "/" + database
	        + "?autoReconnect=true&sessionVariables=storage_engine";
	
	@After
	public void tearDown() {
		if (originalProps != null) {
			originalProps = Context.getRuntimeProperties();
		}
	}
	
	@Test
	public void getProperties_shouldReturnTheMySqlProperties() {
		Properties testProps = new Properties();
		testProps.setProperty(DebeziumConstants.DB_URL_PROP, url);
		testProps.setProperty(DebeziumConstants.DB_USERNAME_PROP, username);
		testProps.setProperty(DebeziumConstants.DB_PASSWORD_PROP, password);
		Context.setRuntimeProperties(testProps);
		MySqlDebeziumConfig config = new MySqlDebeziumConfig();
		setCoreProperties(config);
		config.setSnapshotLockMode(MySqlSnapshotLockMode.EXTENDED);
		config.setSslMode(MySqlSslMode.DISABLED);
		config.setIncludeSchemaChanges(true);
		config.setHistoryClass(FileDatabaseHistory.class);
		config.setHistoryFilename(historyFilename);
		config.setTablesToInclude(Arrays.stream(tablesToInclude.split(",")).collect(Collectors.toSet()));
		
		Properties props = config.getProperties();
		
		assertCoreProperties(props, 18);
		assertEquals(database, props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_INCLUDE_LIST));
		assertEquals(MySqlSnapshotLockMode.EXTENDED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SNAPSHOT_LOCKING_MODE));
		assertEquals("true", props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_INCLUDE_SCHEMA_CHANGES));
		assertEquals(MySqlSslMode.DISABLED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SSL_MODE));
		assertEquals(tablesToExclude, props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_TABLE_INCLUDE_LIST));
		assertEquals(FileDatabaseHistory.class.getName(),
		    props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_CLASS));
		assertEquals(historyFilename, props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_FILE));
	}
	
	@Test
	public void getProperties_shouldReturnTheMySqlPropertiesWithTablesToExclude() {
		Properties testProps = new Properties();
		testProps.setProperty(DebeziumConstants.DB_URL_PROP, url);
		testProps.setProperty(DebeziumConstants.DB_USERNAME_PROP, username);
		testProps.setProperty(DebeziumConstants.DB_PASSWORD_PROP, password);
		MySqlDebeziumConfig config = new MySqlDebeziumConfig();
		setCoreProperties(config);
		config.setSnapshotLockMode(MySqlSnapshotLockMode.EXTENDED);
		config.setSslMode(MySqlSslMode.DISABLED);
		config.setIncludeSchemaChanges(true);
		config.setTablesToExclude(Arrays.stream(tablesToExclude.split(",")).collect(Collectors.toSet()));
		
		Properties props = config.getProperties();
		
		assertCoreProperties(props, 18);
		assertEquals(database, props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_INCLUDE_LIST));
		assertEquals(MySqlSnapshotLockMode.EXTENDED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SNAPSHOT_LOCKING_MODE));
		assertEquals("true", props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_INCLUDE_SCHEMA_CHANGES));
		assertEquals(MySqlSslMode.DISABLED.getPropertyValue(),
		    props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_DB_SSL_MODE));
		assertEquals(tablesToExclude, props.getProperty(MysqlConfigPropertyConstants.CONNECTOR_PROP_TABLE_INCLUDE_LIST));
		assertEquals(FileDatabaseHistory.class.getName(),
		    props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_CLASS));
		assertEquals(historyFilename, props.get(MysqlConfigPropertyConstants.CONNECTOR_PROP_HISTORY_FILE));
	}
	
}
