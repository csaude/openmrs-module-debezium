package org.openmrs.module.debezium;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;

/**
 * Wrapper class around a {@link io.debezium.engine.DebeziumEngine} that watches for events in an
 * OpenMRS database i.e. row inserts, updates and deletes
 */
final public class OpenmrsDebeziumEngine {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsDebeziumEngine.class);
	
	private static DebeziumEngine<ChangeEvent<String, String>> debeziumEngine = null;
	
	private OpenmrsDebeziumEngine() {
	}
	
	// Ensures lazy loading since inner classes are not loaded until they are first referenced
	private static class OpenmrsDebeziumEngineHolder {
		
		private static OpenmrsDebeziumEngine INSTANCE = new OpenmrsDebeziumEngine();
		
	}
	
	public static OpenmrsDebeziumEngine getInstance() {
		return OpenmrsDebeziumEngineHolder.INSTANCE;
	}
	
	/**
	 * Starts the debezium engine
	 */
	protected synchronized void start(DebeziumConfig config) {
		
		if (debeziumEngine != null) {
			log.info("OpenMRS debezium engine is already started");
			return;
		}
		
		log.info("Starting OpenMRS debezium engine...");
		
		//Engine properties
		final Properties props = new Properties();
		props.setProperty("name", "OpenMRS Debezium Engine");
		//TODO Add postgres support
		props.setProperty("connector.class", config.getConnectorClass().getName());
		props.setProperty("offset.storage", config.getOffsetStorageClass().getName());
		props.setProperty("offset.storage.file.filename", config.getOffsetStorageFilename());
		props.setProperty("offset.flush.interval.ms", "0");
		
		//Common connector properties
		props.setProperty("database.hostname", config.getHost());
		props.setProperty("database.port", config.getPort().toString());
		props.setProperty("database.user", config.getUsername());
		props.setProperty("database.password", config.getPassword());
		props.setProperty("database.history", config.getHistoryClass().getName());
		props.setProperty("database.history.file.filename", config.getHistoryFilename());
		props.setProperty("snapshot.mode", config.getSnapshotMode().getConnectorValue());
		//props.setProperty("snapshot.fetch.size", "10240");
		props.setProperty("table.include.list", "");
		
		//Mysql connector properties
		props.setProperty("database.include.list", "");
		props.setProperty("snapshot.locking.mode", "extended");
		props.setProperty("database.ssl.mode", "preferred");
		props.setProperty("include.schema.changes", "false");
		
		debeziumEngine = DebeziumEngine.create(Json.class).using(props).notifying(record -> {
			System.out.println("\n\nReceived DB event -> " + record);
		}).build();
		
		// Run the engine asynchronously ...
		//TODO Make thread pool size configurable
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(debeziumEngine);
		//Thread.sleep(30000);
		// Do something else or wait for a signal or an event
	}
	
	/**
	 * Stops the debezium engine
	 */
	protected synchronized void stop() throws IOException {
		if (debeziumEngine != null) {
			log.info("Stopping OpenMRS debezium engine...");
			
			debeziumEngine.close();
			debeziumEngine = null;
		}
	}
	
}
