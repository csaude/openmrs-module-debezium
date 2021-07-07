package org.openmrs.module.debezium;

import java.io.IOException;
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
	protected synchronized void start(BaseDebeziumConfig config) {
		
		if (debeziumEngine != null) {
			log.info("OpenMRS debezium engine is already started");
			return;
		}
		
		log.info("Starting OpenMRS debezium engine...");
		
		debeziumEngine = DebeziumEngine.create(Json.class).using(config.getProperties()).notifying(record -> {
			System.out.println("\n\nReceived DB event -> " + record);
		}).build();
		
		// Run the engine asynchronously ...
		//TODO Possibly set the thread pool size and add a global property for it configurable
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(debeziumEngine);
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
