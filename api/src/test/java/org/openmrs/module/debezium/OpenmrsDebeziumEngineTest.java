package org.openmrs.module.debezium;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
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
		log.info("Starting OpenMRS test debezium engine");
		engine = OpenmrsDebeziumEngine.getInstance();
		MySqlDebeziumConfig config = new MySqlDebeziumConfig();
		config.setOffsetStorageClass(MemoryOffsetBackingStore.class);
		config.setHistoryClass(MemoryDatabaseHistory.class);
		config.setHost(mysqlContainer.getHost());
		config.setPort(MYSQL_PORT);
		config.setDatabaseName(DB_NAME);
		config.setUsername("root");
		config.setPassword(PASSWORD);
		config.setTablesToInclude(Collections.singleton("location"));
		events = new ArrayList();
		config.setConsumer(new TestDebeziumChangeConsumer(e -> events.add(e)));
		engine.start(config);
		firstEventLatch = new CountDownLatch(1);
		firstEventLatch.await(60, TimeUnit.SECONDS);
		if (firstEventLatch.getCount() > 0) {
			Assert.fail("Expected First event not received");
		}
	}
	
	@After
	public void tearDown() {
		log.info("Stopping OpenMRS test debezium engine");
		engine.stop();
		log.info("Stopping MySQL container");
		mysqlContainer.stop();
		mysqlContainer.close();
	}
	
	private void waitForEvents() throws InterruptedException {
		log.info("Waiting for events...");
		eventsLatch.await(30, TimeUnit.SECONDS);
	}
	
	@Test
	public void shouldProcessAnInsert() throws Exception {
		final int expectedCount = 2;
		eventsLatch = new CountDownLatch(expectedCount);
		try (Connection c = getConnection(); Statement s = c.createStatement()) {
			log.info("Inserting " + expectedCount + " row(s)");
			s.executeUpdate("INSERT INTO location(name) VALUES('Test 1')");
			s.executeUpdate("INSERT INTO location(name) VALUES('Test 2')");
		}
		waitForEvents();
		assertEquals(expectedCount, events.size());
	}
	
	@Test
	public void shouldProcessAnUpdate() throws Exception {
		final int expectedCount = 1;
		eventsLatch = new CountDownLatch(expectedCount);
		try (Connection c = getConnection(); Statement s = c.createStatement()) {
			log.info("Updating row");
			s.executeUpdate("UPDATE location SET name = 'New name'");
		}
		waitForEvents();
		assertEquals(expectedCount, events.size());
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
	}
	
}
