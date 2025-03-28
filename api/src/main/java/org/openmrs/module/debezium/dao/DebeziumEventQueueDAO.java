package org.openmrs.module.debezium.dao;

import org.openmrs.module.debezium.entity.DebeziumEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebeziumEventQueueDAO extends DaoBase {
	
	private static final Logger logger = LoggerFactory.getLogger(DebeziumEventQueueDAO.class);
	
	public void createDebeziumEvent(DebeziumEvent debeziumEvent) {
		executeWithTransaction(sessionFactory, session -> {
			session.saveOrUpdate(debeziumEvent);
			return null;
		});
	}
}
