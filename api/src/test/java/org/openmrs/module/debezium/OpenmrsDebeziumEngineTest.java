package org.openmrs.module.debezium;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.debezium.mysql.MySqlDebeziumConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.debezium.relational.history.MemoryDatabaseHistory;

public class OpenmrsDebeziumEngineTest {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsDebeziumEngineTest.class);
	
	private static final String PASSWORD = "test";
	
	protected static MySQLContainer mysqlContainer = new MySQLContainer(DockerImageName.parse("mysql:5.6"));
	
	protected static Integer MYSQL_PORT;
	
	private OpenmrsDebeziumEngine engine;
	
	@BeforeClass
	public static void beforeDebeziumTestClass() throws Exception {
		log.info("\n\nStarting MySQL container");
		mysqlContainer.withCopyFileToContainer(MountableFile.forClasspathResource("my.cnf"), "/etc/mysql/my.cnf");
		mysqlContainer.withCopyFileToContainer(MountableFile.forClasspathResource("initialData.sql"),
		    "/docker-entrypoint-initdb.d/initialData.sql");
		mysqlContainer.withEnv("MYSQL_ROOT_PASSWORD", PASSWORD);
		mysqlContainer.withDatabaseName("openmrs");
		Startables.deepStart(Stream.of(mysqlContainer)).join();
		MYSQL_PORT = mysqlContainer.getMappedPort(3306);
	}
	
	@Before
	public void beforeDebeziumTest() throws Exception {
		log.info("\n\nStarting OpenMRS test debezium engine");
		engine = OpenmrsDebeziumEngine.getInstance();
		MySqlDebeziumConfig config = new MySqlDebeziumConfig();
		config.setOffsetStorageClass(MemoryOffsetBackingStore.class);
		config.setHistoryClass(MemoryDatabaseHistory.class);
		config.setHost(mysqlContainer.getHost());
		config.setPort(MYSQL_PORT);
		config.setUsername("root");
		config.setPassword(PASSWORD);
		engine.start(config);
	}
	
	@After
	public void afterDebeziumTest() throws IOException {
		log.info("\n\nStopping OpenMRS test debezium engine");
		engine.stop();
	}
	
	@AfterClass
	public static void afterDebeziumTestClass() {
		log.info("\n\nStopping MySQL container");
		mysqlContainer.close();
	}
	
	@Test
	public void shouldProcessAnInsert() throws Exception {
		//Thread.sleep(30000);
	}
	
}
