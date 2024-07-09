package org.openmrs.module.debezium;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.ConnectSchema;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.debezium.DatabaseEvent.Snapshot;

import io.debezium.DebeziumException;
import io.debezium.engine.ChangeEvent;

public class DbChangeToEventFunctionTest {
	
	private DbChangeToEventFunction function = new DbChangeToEventFunction();
	
	@Rule
	public ExpectedException ee = ExpectedException.none();
	
	public class TestChangeEvent implements ChangeEvent<SourceRecord, SourceRecord> {
		
		private SourceRecord value;
		
		public TestChangeEvent(SourceRecord value) {
			this.value = value;
		}
		
		@Override
		public SourceRecord key() {
			return null;
		}
		
		@Override
		public SourceRecord value() {
			return value;
		}
		
		@Override
		public String destination() {
			return null;
		}
		
		@Override
		public Integer partition() {
			return null;
		}
	}
	
	private ConnectSchema createSchema(List<Field> fields) {
		return new ConnectSchema(Type.STRUCT, false, null, null, null, null, null, fields, null, null);
	}
	
	private SourceRecord createRecord(Struct pk, Struct value) {
		return new SourceRecord(null, null, null, null, pk, null, value);
	}
	
	@Test
	public void apply_FailIfNoPrimaryKeyIsFound() {
		ee.expect(DebeziumException.class);
		ee.expectMessage(Matchers.equalTo("Tables with no primary key column are not supported"));
		SourceRecord record = createRecord(new Struct(createSchema(emptyList())), null);
		
		function.apply(new TestChangeEvent(record));
	}
	
	@Test
	public void apply_FailIfACompositePrimaryKeyIsFound() {
		ee.expect(DebeziumException.class);
		ee.expectMessage(Matchers.equalTo("Tables with composite primary keys are not supported"));
		Field field1 = new Field("id", 0, new ConnectSchema(Type.INT32));
		Field field2 = new Field("id", 1, new ConnectSchema(Type.INT32));
		List<Field> fields = new ArrayList();
		fields.add(field1);
		fields.add(field2);
		SourceRecord record = createRecord(new Struct(createSchema(fields)), null);
		
		function.apply(new TestChangeEvent(record));
	}
	
	@Test
	public void apply_FailIfTheOperationIsNotKnown() {
		final String op = "a";
		ee.expect(DebeziumException.class);
		ee.expectMessage(Matchers.equalTo("Don't know how to handle database operation: " + op));
		Field field1 = new Field("id", 0, new ConnectSchema(Type.INT32));
		Struct pkStruct = new Struct(createSchema(singletonList(field1)));
		Field opField = new Field("op", 0, new ConnectSchema(Type.STRING));
		Struct payloadStruct = new Struct(createSchema(singletonList(opField)));
		payloadStruct.put(opField, op);
		SourceRecord record = createRecord(pkStruct, payloadStruct);
		
		function.apply(new TestChangeEvent(record));
	}
	
	@Test
	public void apply_shouldCreateADatabaseEventForAnInsert() {
		final String table = "location";
		final Integer id = 1;
		final String name = "Test";
		final String op = "c";
		Field pkField = new Field("id", 0, new ConnectSchema(Type.INT32));
		Struct pkStruct = new Struct(createSchema(singletonList(pkField)));
		pkStruct.put(pkField, id);
		Field tableField = new Field("table", 0, new ConnectSchema(Type.STRING));
		Field snapshotField = new Field("snapshot", 1, new ConnectSchema(Type.STRING));
		List<Field> sourceFields = new ArrayList();
		sourceFields.add(tableField);
		sourceFields.add(snapshotField);
		Struct sourceStruct = new Struct(createSchema(sourceFields));
		sourceStruct.put(tableField, table);
		sourceStruct.put(snapshotField, "false");
		Field nameField = new Field("name", 1, new ConnectSchema(Type.STRING));
		List<Field> afterFields = new ArrayList();
		afterFields.add(pkField);
		afterFields.add(nameField);
		Field opField = new Field("op", 0, new ConnectSchema(Type.STRING));
		Field sourceField = new Field("source", 1, createSchema(sourceFields));
		Field beforeField = new Field("before", 2, createSchema(emptyList()));
		Field afterField = new Field("after", 3, createSchema(afterFields));
		Struct afterStruct = new Struct(createSchema(afterFields));
		afterStruct.put(pkField, id);
		afterStruct.put(nameField, name);
		List<Field> payloadFields = new ArrayList();
		payloadFields.add(opField);
		payloadFields.add(sourceField);
		payloadFields.add(beforeField);
		payloadFields.add(afterField);
		Struct payloadStruct = new Struct(createSchema(payloadFields));
		payloadStruct.put(opField, op);
		payloadStruct.put(sourceField, sourceStruct);
		payloadStruct.put(afterField, afterStruct);
		SourceRecord record = createRecord(pkStruct, payloadStruct);
		
		DatabaseEvent event = function.apply(new TestChangeEvent(record));
		
		assertEquals(id, event.getPrimaryKeyId());
		assertEquals(table, event.getTableName());
		assertEquals(DatabaseOperation.CREATE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		Map<String, Object> expectedNewState = new HashMap();
		expectedNewState.put("id", id);
		expectedNewState.put("name", name);
		assertEquals(expectedNewState, event.getNewState());
		assertNull(event.getPreviousState());
	}
	
	@Test
	public void apply_shouldCreateADatabaseEventForAnUpdate() {
		final String table = "location";
		final Integer id = 1;
		final String newName = "New Name";
		final String prevName = "Old Name";
		final String op = "u";
		Field pkField = new Field("id", 0, new ConnectSchema(Type.INT32));
		Struct pkStruct = new Struct(createSchema(singletonList(pkField)));
		pkStruct.put(pkField, id);
		Field tableField = new Field("table", 0, new ConnectSchema(Type.STRING));
		Field snapshotField = new Field("snapshot", 1, new ConnectSchema(Type.STRING));
		List<Field> sourceFields = new ArrayList();
		sourceFields.add(tableField);
		sourceFields.add(snapshotField);
		Struct sourceStruct = new Struct(createSchema(sourceFields));
		sourceStruct.put(tableField, table);
		sourceStruct.put(snapshotField, "false");
		Field nameField = new Field("name", 1, new ConnectSchema(Type.STRING));
		List<Field> entityFields = new ArrayList();
		entityFields.add(pkField);
		entityFields.add(nameField);
		Field opField = new Field("op", 0, new ConnectSchema(Type.STRING));
		Field sourceField = new Field("source", 1, createSchema(sourceFields));
		Field beforeField = new Field("before", 2, createSchema(entityFields));
		Field afterField = new Field("after", 3, createSchema(entityFields));
		Struct afterStruct = new Struct(createSchema(entityFields));
		afterStruct.put(pkField, id);
		afterStruct.put(nameField, newName);
		Struct beforeStruct = new Struct(createSchema(entityFields));
		beforeStruct.put(pkField, id);
		beforeStruct.put(nameField, prevName);
		List<Field> payloadFields = new ArrayList();
		payloadFields.add(opField);
		payloadFields.add(sourceField);
		payloadFields.add(beforeField);
		payloadFields.add(afterField);
		Struct payloadStruct = new Struct(createSchema(payloadFields));
		payloadStruct.put(opField, op);
		payloadStruct.put(sourceField, sourceStruct);
		payloadStruct.put(afterField, afterStruct);
		payloadStruct.put(beforeField, beforeStruct);
		SourceRecord record = createRecord(pkStruct, payloadStruct);
		
		DatabaseEvent event = function.apply(new TestChangeEvent(record));
		
		assertEquals(id, event.getPrimaryKeyId());
		assertEquals(table, event.getTableName());
		assertEquals(DatabaseOperation.UPDATE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		Map<String, Object> expectedNewState = new HashMap();
		expectedNewState.put("id", id);
		expectedNewState.put("name", newName);
		assertEquals(expectedNewState, event.getNewState());
		Map<String, Object> expectedPrevState = new HashMap();
		expectedPrevState.put("id", id);
		expectedPrevState.put("name", prevName);
		assertEquals(expectedPrevState, event.getPreviousState());
	}
	
	@Test
	public void apply_shouldCreateADatabaseEventForAnDelete() {
		final String table = "location";
		final Integer id = 1;
		final String name = "Test";
		final String op = "d";
		Field pkField = new Field("id", 0, new ConnectSchema(Type.INT32));
		Struct pkStruct = new Struct(createSchema(singletonList(pkField)));
		pkStruct.put(pkField, id);
		Field tableField = new Field("table", 0, new ConnectSchema(Type.STRING));
		Field snapshotField = new Field("snapshot", 1, new ConnectSchema(Type.STRING));
		List<Field> sourceFields = new ArrayList();
		sourceFields.add(tableField);
		sourceFields.add(snapshotField);
		Struct sourceStruct = new Struct(createSchema(sourceFields));
		sourceStruct.put(tableField, table);
		sourceStruct.put(snapshotField, "false");
		Field nameField = new Field("name", 1, new ConnectSchema(Type.STRING));
		List<Field> beforeFields = new ArrayList();
		beforeFields.add(pkField);
		beforeFields.add(nameField);
		Field opField = new Field("op", 0, new ConnectSchema(Type.STRING));
		Field sourceField = new Field("source", 1, createSchema(sourceFields));
		Field beforeField = new Field("before", 2, createSchema(beforeFields));
		Field afterField = new Field("after", 3, createSchema(emptyList()));
		Struct beforeStruct = new Struct(createSchema(beforeFields));
		beforeStruct.put(pkField, id);
		beforeStruct.put(nameField, name);
		List<Field> payloadFields = new ArrayList();
		payloadFields.add(opField);
		payloadFields.add(sourceField);
		payloadFields.add(beforeField);
		payloadFields.add(afterField);
		Struct payloadStruct = new Struct(createSchema(payloadFields));
		payloadStruct.put(opField, op);
		payloadStruct.put(sourceField, sourceStruct);
		payloadStruct.put(beforeField, beforeStruct);
		SourceRecord record = createRecord(pkStruct, payloadStruct);
		
		DatabaseEvent event = function.apply(new TestChangeEvent(record));
		
		assertEquals(id, event.getPrimaryKeyId());
		assertEquals(table, event.getTableName());
		assertEquals(DatabaseOperation.DELETE, event.getOperation());
		assertEquals(Snapshot.FALSE, event.getSnapshot());
		Map<String, Object> expectedPrevState = new HashMap();
		expectedPrevState.put("id", id);
		expectedPrevState.put("name", name);
		assertEquals(expectedPrevState, event.getPreviousState());
		assertNull(event.getNewState());
	}
	
	@Test
	public void apply_shouldCreateADatabaseEventForASnapShotRead() {
		final String table = "location";
		final Integer id = 1;
		final String name = "Test";
		final String op = "r";
		Field pkField = new Field("id", 0, new ConnectSchema(Type.INT32));
		Struct pkStruct = new Struct(createSchema(singletonList(pkField)));
		pkStruct.put(pkField, id);
		Field tableField = new Field("table", 0, new ConnectSchema(Type.STRING));
		Field snapshotField = new Field("snapshot", 1, new ConnectSchema(Type.STRING));
		List<Field> sourceFields = new ArrayList();
		sourceFields.add(tableField);
		sourceFields.add(snapshotField);
		Struct sourceStruct = new Struct(createSchema(sourceFields));
		sourceStruct.put(tableField, table);
		sourceStruct.put(snapshotField, "true");
		Field nameField = new Field("name", 1, new ConnectSchema(Type.STRING));
		List<Field> afterFields = new ArrayList();
		afterFields.add(pkField);
		afterFields.add(nameField);
		Field opField = new Field("op", 0, new ConnectSchema(Type.STRING));
		Field sourceField = new Field("source", 1, createSchema(sourceFields));
		Field beforeField = new Field("before", 2, createSchema(emptyList()));
		Field afterField = new Field("after", 3, createSchema(afterFields));
		Struct afterStruct = new Struct(createSchema(afterFields));
		afterStruct.put(pkField, id);
		afterStruct.put(nameField, name);
		List<Field> payloadFields = new ArrayList();
		payloadFields.add(opField);
		payloadFields.add(sourceField);
		payloadFields.add(beforeField);
		payloadFields.add(afterField);
		Struct payloadStruct = new Struct(createSchema(payloadFields));
		payloadStruct.put(opField, op);
		payloadStruct.put(sourceField, sourceStruct);
		payloadStruct.put(afterField, afterStruct);
		SourceRecord record = createRecord(pkStruct, payloadStruct);
		
		DatabaseEvent event = function.apply(new TestChangeEvent(record));
		
		assertEquals(id, event.getPrimaryKeyId());
		assertEquals(table, event.getTableName());
		assertEquals(DatabaseOperation.READ, event.getOperation());
		assertEquals(Snapshot.TRUE, event.getSnapshot());
		Map<String, Object> expectedNewState = new HashMap();
		expectedNewState.put("id", id);
		expectedNewState.put("name", name);
		assertEquals(expectedNewState, event.getNewState());
		assertNull(event.getPreviousState());
	}
	
	@Test
	public void apply_shouldCreateADatabaseEventForTheLastSnapShotRead() {
		final String table = "location";
		final Integer id = 1;
		final String name = "Test";
		final String op = "r";
		Field pkField = new Field("id", 0, new ConnectSchema(Type.INT32));
		Struct pkStruct = new Struct(createSchema(singletonList(pkField)));
		pkStruct.put(pkField, id);
		Field tableField = new Field("table", 0, new ConnectSchema(Type.STRING));
		Field snapshotField = new Field("snapshot", 1, new ConnectSchema(Type.STRING));
		List<Field> sourceFields = new ArrayList();
		sourceFields.add(tableField);
		sourceFields.add(snapshotField);
		Struct sourceStruct = new Struct(createSchema(sourceFields));
		sourceStruct.put(tableField, table);
		sourceStruct.put(snapshotField, "last");
		Field nameField = new Field("name", 1, new ConnectSchema(Type.STRING));
		List<Field> afterFields = new ArrayList();
		afterFields.add(pkField);
		afterFields.add(nameField);
		Field opField = new Field("op", 0, new ConnectSchema(Type.STRING));
		Field sourceField = new Field("source", 1, createSchema(sourceFields));
		Field beforeField = new Field("before", 2, createSchema(emptyList()));
		Field afterField = new Field("after", 3, createSchema(afterFields));
		Struct afterStruct = new Struct(createSchema(afterFields));
		afterStruct.put(pkField, id);
		afterStruct.put(nameField, name);
		List<Field> payloadFields = new ArrayList();
		payloadFields.add(opField);
		payloadFields.add(sourceField);
		payloadFields.add(beforeField);
		payloadFields.add(afterField);
		Struct payloadStruct = new Struct(createSchema(payloadFields));
		payloadStruct.put(opField, op);
		payloadStruct.put(sourceField, sourceStruct);
		payloadStruct.put(afterField, afterStruct);
		SourceRecord record = createRecord(pkStruct, payloadStruct);
		
		DatabaseEvent event = function.apply(new TestChangeEvent(record));
		
		assertEquals(id, event.getPrimaryKeyId());
		assertEquals(table, event.getTableName());
		assertEquals(DatabaseOperation.READ, event.getOperation());
		assertEquals(Snapshot.LAST, event.getSnapshot());
		Map<String, Object> expectedNewState = new HashMap();
		expectedNewState.put("id", id);
		expectedNewState.put("name", name);
		assertEquals(expectedNewState, event.getNewState());
		assertNull(event.getPreviousState());
	}
	
}
