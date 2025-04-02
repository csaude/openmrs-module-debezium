package org.openmrs.module.debezium.entity;

import org.openmrs.module.debezium.entity.core.LifeCycle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "debezium_event_queue_offset")
public class DebeziumEventQueueOffset extends LifeCycle {
	
	@Column(name = "application_name", nullable = false)
	private String applicationName;
	
	@Column(name = "first_read", nullable = false)
	private Integer firstRead;
	
	@Column(name = "last_read", nullable = false)
	private Integer lastRead;
	
	public String getApplicationName() {
		return applicationName;
	}
	
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public Integer getFirstRead() {
		return firstRead;
	}
	
	public void setFirstRead(Integer firstRead) {
		this.firstRead = firstRead;
	}
	
	public Integer getLastRead() {
		return lastRead;
	}
	
	public void setLastRead(Integer lastRead) {
		this.lastRead = lastRead;
	}
}
