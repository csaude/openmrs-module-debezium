package org.openmrs.module.debezium;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.debezium.engine.ChangeEvent;

/**
 * Database ChangeEvent consumer which creates a DatabaseEvent instance from the source record and
 * publishes it to all registered consumers.
 */
public class DebeziumChangeConsumer implements Consumer<ChangeEvent<SourceRecord, SourceRecord>> {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumChangeConsumer.class);
	
	private DatabaseEventListener listener;
	
	private OpenmrsDebeziumEngine engine;
	
	private Function<ChangeEvent<SourceRecord, SourceRecord>, DatabaseEvent> function = new DbChangeToEventFunction();
	
	private boolean disabled = false;
	
	public DebeziumChangeConsumer(DatabaseEventListener listener, OpenmrsDebeziumEngine engine) {
		this.listener = listener;
		this.engine = engine;
		Assert.notNull(this.engine);
	}
	
	@Override
	public void accept(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
		
		try {
			if (disabled) {
				if (log.isDebugEnabled()) {
					log.info("Deferring processing of change event for later because an error was encountered while "
					        + "processing a previous change event");
				}
				
				return;
			}
			
			DatabaseEvent dbEvent = function.apply(changeEvent);
			
			if (log.isDebugEnabled()) {
				log.debug("Notifying listener of the database event: " + dbEvent);
			}
			
			listener.onEvent(dbEvent);
		}
		catch (Throwable t) {
			//TODO Do not disable in case of a snapshot event
			disabled = true;
			CustomFileOffsetBackingStore.disable();
			if (log.isDebugEnabled()) {
				log.debug("Disabled change event consumer");
			}
			
			log.warn("Failed to process database change event, stopping debezium engine", t);
			
			//TODO Send a notification to the admin
			engine.stop();
		}
		
	}
	
}
