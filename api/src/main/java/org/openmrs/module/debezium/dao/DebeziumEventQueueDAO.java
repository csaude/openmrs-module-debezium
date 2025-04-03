package org.openmrs.module.debezium.dao;

import org.openmrs.module.debezium.entity.DebeziumEventQueue;

public class DebeziumEventQueueDAO extends DaoBase {
	
	public void createDebeziumEvent(DebeziumEventQueue debeziumEvent) {
		executeWithTransaction(sessionFactory, session -> {
			session.saveOrUpdate(debeziumEvent);
			return null;
		});
	}
}
