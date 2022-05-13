package org.openmrs.module.debezium;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.debezium.DatabaseEvent.Snapshot;
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
import org.openmrs.module.debezium.mysql.MySqlSnapshotMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.debezium.engine.ChangeEvent;
import io.debezium.relational.history.MemoryDatabaseHistory;

public class OpenmrsDebeziumEngineTest {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsDebeziumEngineTest.class);
	
	private static final String PASSWORD = "test";
	
	private static final String DB_NAME = "openmrs";
	
	private static final int INITIAL_LOCATION_ID = 1;
	
	private static final String INITIAL_LOCATION_NAME = "Demo";
	
	private static final String INITIAL_LOCATION_DESCR = "Unknown";
	
	private static final String INITIAL_LOCATION_UUID = "ab3b12d1-5c4f-415f-871b-b98a22137604";
	
	protected static MySQLContainer mysqlContainer = new MySQLContainer(DockerImageName.parse("mysql:5.6"));
	
	protected static Integer MYSQL_PORT;
	
	private OpenmrsDebeziumEngine engine;
	
	private CountDownLatch firstEventLatch;
	
	private CountDownLatch eventsLatch;
	
	private List<DatabaseEvent> events;
	
	public class TestDebeziumChangeConsumer extends DebeziumChangeConsumer {
		
		public TestDebeziumChangeConsumer(Consumer<DatabaseEvent> listener) {
			super(listener);
		}
		
		@Override
		public void accept(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
			log.info("Test consumer: Received database change -> " + changeEvent);
			if (firstEventLatch.getCount() > 0) {
				log.info("Ignoring first database change");
				firstEventLatch.countDown();
			} else {
				super.accept(changeEvent);
				eventsLatch.countDown();
			}
			
		}
		
	}
	
	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(mysqlContainer.getJdbcUrl(), mysqlContainer.getUsername(),
		    mysqlContainer.getPassword());
	}
	
	@Before
	public void setup() throws Exception {
		startMySql();
		startEngine();
	}
	
	private void startMySql() {
		log.info("Starting MySQL container");
		mysqlContainer.withCopyFileToContainer(MountableFile.forClasspathResource("my.cnf"), "/etc/mysql/my.cnf");
		mysqlContainer.withCopyFileToContainer(MountableFile.forClasspathResource("initialData.sql"),
		    "/docker-entrypoint-initdb.d/initialData.sql");
		mysqlContainer.withEnv("MYSQL_ROOT_PASSWORD", PASSWORD);
		mysqlContainer.withDatabaseName(DB_NAME);
		Startables.deepStart(Stream.of(mysqlContainer)).join();
		MYSQL_PORT = mysqlContainer.getMappedPort(3306);
	}
	
	private void startEngine() throws Exception {
		firstEventLatch = new CountDownLatch(1);
		engine = OpenmrsDebeziumEngine.getInstance();
		MySqlDebeziumConfig config = new MySqlDebeziumConfig(MySqlSnapshotMode.INITIAL, singleton("location"), null);
		config.setOffsetStorageClass(MemoryOffsetBackingStore.class);
		config.setHistoryClass(MemoryDatabaseHistory.class);
		config.setHost(mysqlContainer.getHost());
		config.setPort(MYSQL_PORT);
		config.setDatabaseName(DB_NAME);
		config.setUsername("root");
		config.setPassword(PASSWORD);
		events = new ArrayList();
		config.setConsumer(new TestDebeziumChangeConsumer(e -> events.add(e)));
		
		log.info("Starting OpenMRS test debezium engine");
		engine.start(config);
		firstEventLatch.await(45, TimeUnit.SECONDS);
		if (firstEventLatch.getCount() > 0) {
			Assert.fail("Expected First event not received");
		}
	}
	
	@After
	public void tearDown() {
		log.info("Stopping OpenMRS test debezium engine");
		engine.stop(true);
		log.info("Stopping MySQL container");
		mysqlContainer.stop();
		mysqlContainer.close();
	}
	
	private void waitForEvents() throws Exception {
		log.info("Waiting for events...");
		eventsLatch.await(15, TimeUnit.SECONDS);
	}
	
	@Test
	public void shouldProcessAnInsert() throws Exception {
		final int expectedCount = 2;
		final String name1 = "Test 1";
		final String name2 = "Test 2";
		final String uuid1 = "bb3b12d1-5c4f-415f-871b-b98a22137601";
		final String uuid2 = "cb3b12d1-5c4f-415f-871b-b98a22137602";
		final String description1 = "Description 1";
		final String description2 = "Description 2";
		eventsLatch = new CountDownLatch(expectedCount);
		try (Connection c = getConnection(); Statement s = c.createStatement()) {
			log.info("Inserting " + expectedCount + " row(s)");
			s.executeUpdate("INSERT INTO location(name,description,uuid) VALUES('" + name1 + "', '" + description1 + "', '"
			        + uuid1 + "')");
			s.executeUpdate("INSERT INTO location(name,description,uuid) VALUES('" + name2 + "', '" + description2 + "', '"
			        + uuid2 + "')");
		}
		waitForEvents();
		assertEquals(expectedCount, events.size());
		DatabaseEvent event = events.get(0);
		assertEquals("location", event.getTableName());
		assertEquals(2, event.getPrimaryKeyId());
		assertEquals(DatabaseOperation.CREATE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		assertEquals(4, event.getNewState().size());
		Map<String, Object> expectedNewState = new HashMap();
		expectedNewState.put("id", 2);
		expectedNewState.put("name", name1);
		expectedNewState.put("description", description1);
		expectedNewState.put("uuid", uuid1);
		assertEquals(expectedNewState, event.getNewState());
		assertNull(event.getPreviousState());
		
		event = events.get(1);
		assertEquals("location", event.getTableName());
		assertEquals(3, event.getPrimaryKeyId());
		assertEquals(DatabaseOperation.CREATE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		assertEquals(4, event.getNewState().size());
		expectedNewState = new HashMap();
		expectedNewState.put("id", 3);
		expectedNewState.put("name", name2);
		expectedNewState.put("description", description2);
		expectedNewState.put("uuid", uuid2);
		assertEquals(expectedNewState, event.getNewState());
		assertNull(event.getPreviousState());
	}
	
	@Test
	public void shouldProcessAnUpdate() throws Exception {
		final int expectedCount = 1;
		final String name = "New name";
		final String description = "New description";
		eventsLatch = new CountDownLatch(expectedCount);
		try (Connection c = getConnection(); Statement s = c.createStatement()) {
			log.info("Updating row");
			s.executeUpdate("UPDATE location SET name = '" + name + "', description = '" + description + "'");
		}
		waitForEvents();
		assertEquals(expectedCount, events.size());
		DatabaseEvent event = events.get(0);
		assertEquals("location", event.getTableName());
		assertEquals(INITIAL_LOCATION_ID, event.getPrimaryKeyId());
		assertEquals(DatabaseOperation.UPDATE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		assertEquals(4, event.getPreviousState().size());
		Map<String, Object> expectedPrevState = new HashMap();
		expectedPrevState.put("id", INITIAL_LOCATION_ID);
		expectedPrevState.put("name", INITIAL_LOCATION_NAME);
		expectedPrevState.put("description", INITIAL_LOCATION_DESCR);
		expectedPrevState.put("uuid", INITIAL_LOCATION_UUID);
		assertEquals(expectedPrevState, event.getPreviousState());
		assertEquals(4, event.getNewState().size());
		Map<String, Object> expectedNewState = new HashMap();
		expectedNewState.put("id", INITIAL_LOCATION_ID);
		expectedNewState.put("name", name);
		expectedNewState.put("description", description);
		expectedNewState.put("uuid", INITIAL_LOCATION_UUID);
		assertEquals(expectedNewState, event.getNewState());
	}
	
	@Test
	public void shouldProcessADelete() throws Exception {
		final int expectedCount = 1;
		eventsLatch = new CountDownLatch(expectedCount);
		try (Connection c = getConnection(); Statement s = c.createStatement()) {
			log.info("Deleting row");
			s.executeUpdate("DELETE FROM location WHERE name = 'Demo'");
		}
		waitForEvents();
		assertEquals(expectedCount, events.size());
		DatabaseEvent event = events.get(0);
		assertEquals("location", event.getTableName());
		assertEquals(1, event.getPrimaryKeyId());
		assertEquals(DatabaseOperation.DELETE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		assertEquals(4, event.getPreviousState().size());
		Map<String, Object> expectedPrevState = new HashMap();
		expectedPrevState.put("id", INITIAL_LOCATION_ID);
		expectedPrevState.put("name", INITIAL_LOCATION_NAME);
		expectedPrevState.put("description", INITIAL_LOCATION_DESCR);
		expectedPrevState.put("uuid", INITIAL_LOCATION_UUID);
		assertEquals(expectedPrevState, event.getPreviousState());
		assertNull(event.getNewState());
	}
	
}
