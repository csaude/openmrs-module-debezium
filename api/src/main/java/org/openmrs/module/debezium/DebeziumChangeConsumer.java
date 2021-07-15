package org.openmrs.module.debezium;

import java.util.function.Consumer;

import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.debezium.engine.ChangeEvent;

/**
 * Primary event listener for database changes creates DatabaseEvent and publishes it to all
 * registered listeners using the spring events API.
 */
@Component
public class DebeziumChangeConsumer implements Consumer<ChangeEvent<SourceRecord, SourceRecord>> {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumChangeConsumer.class);
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Override
	public void accept(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
		if (log.isDebugEnabled()) {
			log.debug("Received database change -> " + changeEvent);
		}
		
		DatabaseEvent databaseEvent = new DatabaseEvent(this, null, null, null, null, false);
		
		if (log.isDebugEnabled()) {
			log.debug("Publishing database event: " + databaseEvent);
		}
		
		publisher.publishEvent(databaseEvent);
	}
	
}
