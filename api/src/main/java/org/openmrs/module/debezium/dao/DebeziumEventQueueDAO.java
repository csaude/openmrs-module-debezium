package org.openmrs.module.debezium.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.debezium.entity.DebeziumEventQueue;
import org.openmrs.module.debezium.entity.DebeziumEventQueueOffset;

import java.util.List;

public class DebeziumEventQueueDAO extends DaoBase {
	
	public void createDebeziumEvent(DebeziumEventQueue debeziumEvent) {
		executeWithTransaction(sessionFactory, session -> {
			session.saveOrUpdate(debeziumEvent);
			return null;
		});
	}
	
	public List<DebeziumEventQueue> getEventsByApplicationName(DebeziumEventQueueOffset offset, Integer fetchSize) {
		return executeWithTransaction(sessionFactory, session -> {
			Criteria criteria = session.createCriteria(DebeziumEventQueue.class);
			if (fetchSize != null) {
				criteria.setFetchSize(fetchSize);
			}
			
			criteria.addOrder(Order.asc("id"));
			
			if (offset != null) {
				if (offset.getLastRead() != null) {
					if (offset.isCreated()) {
						criteria.add(Restrictions.between("id", offset.getFirstRead(), offset.getLastRead()));
					} else if (!offset.isCreated()) {
						criteria.add(Restrictions.gt("id", offset.getLastRead()));
					}
				} else {
					criteria.add(Restrictions.gt("id", offset.getFirstRead()));
				}
			}
			return criteria.list();
		});
	}
	
	public List<DebeziumEventQueue> getEventsByApplicationNameRecursive(Integer lastRead, Integer fetchSize) {
		return executeWithTransaction(sessionFactory, session -> {
			Criteria criteria = session.createCriteria(DebeziumEventQueue.class);
			criteria.setFetchSize(fetchSize);
			criteria.addOrder(Order.asc("id"));
			criteria.add(Restrictions.gt("id", lastRead));
			
			return criteria.list();
		});
	}
	
}
