package org.openmrs.module.debezium;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.openmrs.module.debezium.DatabaseEvent.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.DebeziumException;
import io.debezium.engine.ChangeEvent;

/**
 * Database ChangeEvent consumer which creates a DatabaseEvent instance from the source record and
 * publishes it to all registered consumers.
 */
public class DebeziumChangeConsumer implements Consumer<ChangeEvent<SourceRecord, SourceRecord>> {
	
	private static final Logger log = LoggerFactory.getLogger(DebeziumChangeConsumer.class);
	
	private Consumer<DatabaseEvent> consumer;
	
	private static final Map<Object, DatabaseOperation> STRING_OP_MAP;
	
	private static final Map<Object, Snapshot> STRING_SNAPSHOT_MAP;
	
	static {
		STRING_OP_MAP = new HashMap(3);
		STRING_OP_MAP.put("c", DatabaseOperation.CREATE);
		STRING_OP_MAP.put("u", DatabaseOperation.UPDATE);
		STRING_OP_MAP.put("d", DatabaseOperation.DELETE);
		
		STRING_SNAPSHOT_MAP = new HashMap(3);
		STRING_SNAPSHOT_MAP.put("true", Snapshot.TRUE);
		STRING_SNAPSHOT_MAP.put("false", Snapshot.FALSE);
		STRING_SNAPSHOT_MAP.put("last", Snapshot.LAST);
	}
	
	public DebeziumChangeConsumer(Consumer<DatabaseEvent> consumer) {
		this.consumer = consumer;
	}
	
	@Override
	public void accept(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
		SourceRecord record = changeEvent.value();
		Struct keyStruct = (Struct) record.key();
		if (keyStruct.schema().fields().isEmpty()) {
			throw new DebeziumException("Tables with no primary key column are not supported");
		}
		
		if (keyStruct.schema().fields().size() > 1) {
			throw new DebeziumException("Tables with composite primary keys are not supported");
		}
		
		Object pk = keyStruct.get(keyStruct.schema().fields().get(0));
		Struct payload = (Struct) record.value();
		DatabaseOperation dbOP = STRING_OP_MAP.get(payload.get("op"));
		if (dbOP == null) {
			throw new DebeziumException("Don't know how to handle database operation: " + payload.get("op"));
		}
		
		Struct source = (Struct) payload.get("source");
		String table = source.get("table").toString();
		Snapshot snapshot = STRING_SNAPSHOT_MAP.get(source.get("snapshot"));
		
		Map<String, Object> beforeState = null;
		if (payload.get("before") != null) {
			Map<String, Object> beforeStateTmp = new HashMap();
			Struct beforeStruct = (Struct) payload.get("before");
			beforeStruct.schema().fields().forEach(field -> beforeStateTmp.put(field.name(), beforeStruct.get(field)));
			beforeState = beforeStateTmp;
		}
		
		Map<String, Object> afterState = null;
		if (payload.get("after") != null) {
			Map<String, Object> afterStateTmp = new HashMap();
			Struct afterStruct = (Struct) payload.get("after");
			afterStruct.schema().fields().forEach(field -> afterStateTmp.put(field.name(), afterStruct.get(field)));
			afterState = afterStateTmp;
		}
		
		DatabaseEvent dbEvent = new DatabaseEvent(pk, table, dbOP, snapshot, beforeState, afterState);
		
		if (log.isDebugEnabled()) {
			log.debug("Notifying listener of the database event: " + dbEvent);
		}
		
		//TODO Only commit the offset if the listener returns with no errors
		consumer.accept(dbEvent);
	}
	
}
