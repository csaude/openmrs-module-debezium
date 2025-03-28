package org.openmrs.module.debezium.service;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.debezium.dao.DebeziumEventQueueDAO;
import org.openmrs.module.debezium.entity.DebeziumEvent;

public class DebeziumEventServiceImpl extends BaseOpenmrsService implements DebeziumEventService {
	
	DebeziumEventQueueDAO eventQueueDAO;
	
	@Override
	public void createDebeziumEvent(DebeziumEvent debeziumEvent) {
		eventQueueDAO.createDebeziumEvent(debeziumEvent);
	}
	
	public DebeziumEventQueueDAO getEventQueueDAO() {
		return eventQueueDAO;
	}
	
	public void setEventQueueDAO(DebeziumEventQueueDAO eventQueueDAO) {
		this.eventQueueDAO = eventQueueDAO;
	}
}
