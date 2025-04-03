package org.openmrs.module.debezium.entity.core;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Date;

@MappedSuperclass
public abstract class Auditable extends Base {
	
	private static final long serialVersionUID = 2248918162173231925L;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
