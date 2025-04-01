package org.openmrs.module.debezium.entity;

import org.openmrs.module.debezium.entity.core.Auditable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "debezium_event_queue")
public class DebeziumEventQueue extends Auditable {
	
	@Column(name = "primary_key_id", nullable = false)
	private String primaryKeyId;
	
	@Column(name = "identifier")
	private String identifier;
	
	@Column(name = "table_name")
	private String tableName;
	
	@Column(name = "snapshot", nullable = false)
	private Boolean snapshot;
	
	@Column(name = "operation", nullable = false)
	private String operation;
	
	@Column(name = "request_uuid")
	private String requestUuid;
	
	@Column(name = "event_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private EventType eventType;
	
	public String getPrimaryKeyId() {
		return primaryKeyId;
	}
	
	public void setPrimaryKeyId(String primaryKeyId) {
		this.primaryKeyId = primaryKeyId;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public Boolean getSnapshot() {
		return snapshot;
	}
	
	public void setSnapshot(Boolean snapshot) {
		this.snapshot = snapshot;
	}
	
	public String getOperation() {
		return operation;
	}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public String getRequestUuid() {
		return requestUuid;
	}
	
	public void setRequestUuid(String requestUuid) {
		this.requestUuid = requestUuid;
	}
	
	public EventType getEventType() {
		return eventType;
	}
	
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
}
