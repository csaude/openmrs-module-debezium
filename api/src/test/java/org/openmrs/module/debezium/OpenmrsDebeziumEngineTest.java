package org.openmrs.module.debezium;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

public class OpenmrsDebeziumEngineTest {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsDebeziumEngineTest.class);
	
	protected static MySQLContainer mysqlContainer = new MySQLContainer(DockerImageName.parse("mysql:5.6"));
	
	protected static Integer MYSQL_PORT;
	
	private OpenmrsDebeziumEngine engine;
	
	@BeforeClass
	public static void beforeDebeziumTestClass() throws Exception {
		log.info("\n\nStarting MySQL container");
		mysqlContainer.withCopyFileToContainer(MountableFile.forClasspathResource("my.cnf"), "/etc/mysql/my.cnf");
		mysqlContainer.withEnv("MYSQL_ROOT_PASSWORD", "test");
		mysqlContainer.withDatabaseName("openmrs");
		Startables.deepStart(Stream.of(mysqlContainer)).join();
		MYSQL_PORT = mysqlContainer.getMappedPort(3306);
	}
	
	@Before
	public void beforeDebeziumTest() throws Exception {
		log.info("\n\nStarting OpenMRS test debezium engine");
		engine = OpenmrsDebeziumEngine.getInstance();
		engine.start(new MySqlDebeziumConfig());
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
	public void shouldProcessAnInsert() {
		
	}
	
}
