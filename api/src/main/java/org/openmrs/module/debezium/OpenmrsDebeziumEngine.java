package org.openmrs.module.debezium;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;

/**
 * Wrapper class around a {@link io.debezium.engine.DebeziumEngine} that watches for events in an
 * OpenMRS database i.e. row inserts, updates and deletes
 */
final public class OpenmrsDebeziumEngine {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsDebeziumEngine.class);
	
	private static DebeziumEngine<ChangeEvent<String, String>> debeziumEngine;
	
	private static ExecutorService executor;
	
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
		
		debeziumEngine = DebeziumEngine.create(Connect.class).using(config.getProperties()).using(config.getCallback())
		        .notifying(config.getConsumer()).build();
		
		// Run the engine asynchronously ...
		//TODO Possibly set the thread pool size and add a global property for it configurable
		executor = Executors.newCachedThreadPool();
		executor.execute(debeziumEngine);
	}
	
	/**
	 * Stops the debezium engine
	 */
	protected synchronized void stop() {
		if (debeziumEngine != null) {
			log.info("Closing OpenMRS debezium engine...");
			
			try {
				debeziumEngine.close();
			}
			catch (IOException e) {
				log.warn("An error occurred while closing the debezium engine", e);
			}
			finally {
				debeziumEngine = null;
			}
		}
		
		if (executor != null) {
			log.info("Waiting another 5 seconds for the embedded engine to shut down");
			
			try {
				executor.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				log.warn("Interrupt occurred while waiting for the debezium engine to shut down", e);
			}
			finally {
				executor = null;
			}
		}
	}
	
}
