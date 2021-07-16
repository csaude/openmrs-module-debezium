package org.openmrs.module.debezium;

/**
 * An instance of this class encapsulated details about a database change event
 */
public class DatabaseEvent {
	
	//Unique identifier for the entity usually a uuid or name for an entity like a privilege that has no uuid
	private Object identifier;
	
	//The primary key value of the affected row
	private Object primaryKeyId;
	
	private String tableName;
	
	private DatabaseOperation operation;
	
	private boolean snapshot;
	
	public DatabaseEvent(Object identifier, Object primaryKeyId, String tableName, DatabaseOperation operation,
	    boolean snapshot) {
		this.identifier = identifier;
		this.primaryKeyId = primaryKeyId;
		this.tableName = tableName;
		this.operation = operation;
		this.snapshot = snapshot;
	}
	
	/**
	 * Gets the identifier
	 *
	 * @return the identifier
	 */
	public Object getIdentifier() {
		return identifier;
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
	public boolean getSnapshot() {
		return snapshot;
	}
	
	@Override
	public String toString() {
		return "Event {tableName=" + tableName + ", primaryKeyId=" + primaryKeyId + ", identifier=" + identifier
		        + ", operation=" + operation + ", snapshot=" + snapshot + "}";
	}
	
}
