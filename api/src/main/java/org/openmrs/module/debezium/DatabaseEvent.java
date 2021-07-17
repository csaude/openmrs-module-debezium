package org.openmrs.module.debezium;

import java.util.Map;

/**
 * An instance of this class encapsulated details about a database change event
 */
public class DatabaseEvent {
	
	//The primary key value of the affected row
	private Object primaryKeyId;
	
	private String tableName;
	
	private DatabaseOperation operation;
	
	private Snapshot snapshot;
	
	private Map<String, Object> previousState;
	
	private Map<String, Object> newState;
	
	public enum Snapshot {
		TRUE,
		FALSE,
		LAST
	}
	
	public DatabaseEvent(Object primaryKeyId, String tableName, DatabaseOperation operation, Snapshot snapshot,
	    Map<String, Object> previousState, Map<String, Object> newState) {
		
		this.primaryKeyId = primaryKeyId;
		this.tableName = tableName;
		this.operation = operation;
		this.snapshot = snapshot;
		this.previousState = previousState;
		this.newState = newState;
	}
	
	/**
	 * Gets the primaryKeyId
	 *
	 * @return the primaryKeyId
	 */
	public Object getPrimaryKeyId() {
		return primaryKeyId;
	}
	
	/**
	 * Gets the tableName
	 *
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	
	/**
	 * Gets the operation
	 *
	 * @return the operation
	 */
	public DatabaseOperation getOperation() {
		return operation;
	}
	
	/**
	 * Gets the snapshot
	 *
	 * @return the snapshot
	 */
	public Snapshot getSnapshot() {
		return snapshot;
	}
	
	/**
	 * Gets the previousState
	 *
	 * @return the previousState
	 */
	public Map<String, Object> getPreviousState() {
		return previousState;
	}
	
	/**
	 * Gets the newState
	 *
	 * @return the newState
	 */
	public Map<String, Object> getNewState() {
		return newState;
	}
	
	@Override
	public String toString() {
		return "Event {tableName=" + tableName + ", primaryKeyId=" + primaryKeyId + ", operation=" + operation
		        + ", snapshot=" + snapshot + "}";
	}
	
}
