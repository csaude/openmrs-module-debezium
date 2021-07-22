package org.openmrs.module.debezium;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.embedded.Connect;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;

/**
 * Wrapper class around a {@link io.debezium.engine.DebeziumEngine} that watches for events in an
 * OpenMRS database i.e. row inserts, updates and deletes
 */
public final class OpenmrsDebeziumEngine {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsDebeziumEngine.class);
	
	private static DebeziumEngine<ChangeEvent<SourceRecord, SourceRecord>> debeziumEngine;
	
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
	public synchronized <T extends BaseDebeziumConfig> void start(T config) {
		
		if (debeziumEngine != null) {
			log.info("OpenMRS debezium engine is already started");
			return;
		}
		
		log.info("Starting OpenMRS debezium engine...");
		
		debeziumEngine = DebeziumEngine.create(Connect.class).using(config.getProperties()).notifying(config.getConsumer())
		        .build();
		
		// Run the engine asynchronously ...
		//TODO Possibly set the thread pool size and add a global property for it configurable
		executor = Executors.newCachedThreadPool();
		executor.execute(debeziumEngine);
	}
	
	/**
	 * Stops the debezium engine
	 */
	public static synchronized void stop() {
		if (debeziumEngine != null) {
			log.info("Starting task to close the debezium engine");
			//Since this method is called from our ChangeEvent Consumer, we need to stop the engine in a separate thread
			//so that the consumer's accept method can return and the task ends otherwise the code stopping the engine 
			//hangs because it would be waiting for itself to complete which would be a deadlock
			Executors.newSingleThreadExecutor().execute(() -> {
				try {
					log.info("Closing debezium engine...");
					debeziumEngine.close();
				}
				catch (IOException e) {
					log.warn("An error occurred while closing the debezium engine", e);
				}
				finally {
					debeziumEngine = null;
				}
				
				if (executor != null) {
					try {
						executor.shutdownNow();
					}
					finally {
						executor = null;
					}
				}
			});
		}
	}
	
}
