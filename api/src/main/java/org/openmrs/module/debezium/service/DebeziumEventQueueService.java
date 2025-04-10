package org.openmrs.module.debezium.service;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.debezium.entity.DebeziumEventQueue;

import java.util.Set;

public interface DebeziumEventQueueService extends OpenmrsService {
	
	Set<DebeziumEventQueue> getApplicationEvents(String applicationName);
	
	void commitEventQueue(String applicationName);
}
