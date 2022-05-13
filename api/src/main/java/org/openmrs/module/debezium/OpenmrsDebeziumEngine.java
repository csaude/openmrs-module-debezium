package org.openmrs.module.debezium;

import static io.debezium.engine.DebeziumEngine.create;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
			log.info("Debezium engine is already started");
			return;
		}
		
		log.info("Starting debezium engine...");
		
		debeziumEngine = create(Connect.class).using(config.getProperties()).notifying(config.getConsumer()).build();
		
		// Run the engine asynchronously ...
		//TODO Possibly set the thread pool size and add a global property for it to be configurable
		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		executor.execute(debeziumEngine);
	}
	
	/**
	 * Stops the debezium engine
	 * 
	 * @param wait if set to true the current thread will block until the debezium engine has been *
	 *            stopped otherwise it will not
	 */
	public synchronized void stop(boolean wait) {
		if (debeziumEngine != null) {
			log.info("Starting task to close the debezium engine");
			
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				
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
			
			if (wait) {
				if (log.isDebugEnabled()) {
					log.debug("Waiting for debezium shutdown thread to terminate");
				}
				
				try {
					future.get();
					
					if (log.isDebugEnabled()) {
						log.debug("Debezium engine shutdown thread has terminated");
					}
				}
				catch (InterruptedException e) {
					log.warn("Debezium engine shutdown thread interrupted while terminating");
				}
				catch (ExecutionException e) {
					log.warn("Debezium engine shutdown thread encountered an error while terminating");
				}
			}
		}
	}
	
}
