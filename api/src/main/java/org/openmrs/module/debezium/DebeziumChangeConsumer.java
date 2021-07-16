package org.openmrs.module.debezium;

import java.util.function.Consumer;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.DebeziumException;
import io.debezium.engine.ChangeEvent;

/**
 * Primary event listener for database changes creates DatabaseEvent and publishes it to all
 * registered listeners using the spring events API.
 */
public class DebeziumChangeConsumer implements Consumer<ChangeEvent<SourceRecord, SourceRecord>> {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumChangeConsumer.class);
	
	private DatabaseEventListener listener;
	
	public DebeziumChangeConsumer(DatabaseEventListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void accept(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
		if (log.isDebugEnabled()) {
			log.debug("Received database change -> " + changeEvent);
		}
		
		SourceRecord record = changeEvent.value();
		Struct keyStruct = (Struct) changeEvent.value().key();
		if (keyStruct.schema().fields().isEmpty()) {
			throw new DebeziumException("Tables with no primary key column are not supported");
		}
		if (keyStruct.schema().fields().size() > 1) {
			throw new DebeziumException("Tables with composite primary keys are not supproted");
		}
		Object primaryKey = keyStruct.get(keyStruct.schema().fields().get(0));
		Struct payload = (Struct) changeEvent.value().value();
		DatabaseEvent databaseEvent = new DatabaseEvent(primaryKey, null, null, null, false);
		
		if (log.isDebugEnabled()) {
			log.debug("Notifying listener of the database event: " + databaseEvent);
		}
		
		listener.process(databaseEvent);
	}
	
}
