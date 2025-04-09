package org.openmrs.module.debezium.dao;

import org.openmrs.module.debezium.entity.DebeziumEventQueue;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class DebeziumEventQueueDAO extends DaoBase {
	
	public void createDebeziumEvent(DebeziumEventQueue debeziumEvent) {
		executeWithTransaction(sessionFactory, session -> {
			session.saveOrUpdate(debeziumEvent);
			return null;
		});
	}
	
	public List<DebeziumEventQueue> fetchDebeziumEvents(Integer starterId, Integer fetchSize) {
		return executeWithTransaction(sessionFactory, session -> {
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DebeziumEventQueue> criteriaQuery = criteriaBuilder.createQuery(DebeziumEventQueue.class);
			Root<DebeziumEventQueue> root = criteriaQuery.from(DebeziumEventQueue.class);
			criteriaQuery.select(root);
			
			List<Predicate> predicates = new ArrayList<>();
			if (starterId != null) {
				predicates.add(criteriaBuilder.greaterThan(root.get("id"), starterId));
			}
			
			if (!predicates.isEmpty()) {
				criteriaQuery.where(predicates.toArray(new Predicate[0]));
			}
			
			criteriaQuery.orderBy(criteriaBuilder.asc(root.get("id")));
			TypedQuery<DebeziumEventQueue> query = session.createQuery(criteriaQuery);
			if (fetchSize != null) {
				query.setMaxResults(fetchSize);
			}
			
			return query.getResultList();
		});
	}
}
