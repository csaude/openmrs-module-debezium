package org.openmrs.module.debezium.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import io.debezium.DebeziumException;
import io.debezium.engine.ChangeEvent;
import org.openmrs.module.debezium.entity.DatabaseEvent;
import org.openmrs.module.debezium.entity.DatabaseOperation;

/**
 * Utility Function that creates a {@link org.openmrs.module.debezium.entity.DatabaseEvent} from a
 * debezium {@link ChangeEvent}
 */
public class DbChangeToEventFunction implements Function<ChangeEvent<SourceRecord, SourceRecord>, DatabaseEvent> {
	
	private static final Map<Object, DatabaseOperation> STRING_OP_MAP;
	
	private static final Map<Object, DatabaseEvent.Snapshot> STRING_SNAPSHOT_MAP;
	
	static {
		STRING_OP_MAP = new HashMap(3);
		STRING_OP_MAP.put("c", DatabaseOperation.CREATE);
		STRING_OP_MAP.put("r", DatabaseOperation.READ);
		STRING_OP_MAP.put("u", DatabaseOperation.UPDATE);
		STRING_OP_MAP.put("d", DatabaseOperation.DELETE);
		
		STRING_SNAPSHOT_MAP = new HashMap(3);
		STRING_SNAPSHOT_MAP.put("true", DatabaseEvent.Snapshot.TRUE);
		STRING_SNAPSHOT_MAP.put("false", DatabaseEvent.Snapshot.FALSE);
		STRING_SNAPSHOT_MAP.put("last", DatabaseEvent.Snapshot.LAST);
	}
	
	@Override
	public DatabaseEvent apply(ChangeEvent<SourceRecord, SourceRecord> changeEvent) {
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
		DatabaseEvent.Snapshot snapshot = STRING_SNAPSHOT_MAP.get(source.get("snapshot"));
		
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
		
		return new DatabaseEvent(pk, table, dbOP, snapshot, beforeState, afterState);
	}
	
}
