package org.openmrs.module.debezium.service;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.debezium.entity.DebeziumEvent;
import org.springframework.transaction.annotation.Transactional;

public interface DebeziumEventService extends OpenmrsService {
	
	@Transactional
	public void createDebeziumEvent(DebeziumEvent debeziumEvent);
}
