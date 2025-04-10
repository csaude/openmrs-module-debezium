package org.openmrs.module.debezium.dao;

import org.openmrs.module.debezium.entity.DebeziumEventQueueOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class DebeziumEventQueueOffsetDAO extends DaoBase {
	
	private static final Logger logger = LoggerFactory.getLogger(DebeziumEventQueueOffsetDAO.class);
	
	public void saveOffset(DebeziumEventQueueOffset offset) {
		executeWithTransaction(sessionFactory, session -> {
			session.save(offset);
			return null;
		});
	}
	
	public void updateOffset(DebeziumEventQueueOffset offset) {
		executeWithTransaction(sessionFactory, session -> {
			try {
				
				logger.debug("Saving offset: {} for client: {}",
				    " Start from " + offset.getFirstRead() + "to " + offset.getLastRead(), offset.getApplicationName());
				
				Query query = session.createQuery(
				    "UPDATE DebeziumEventQueueOffset SET firstRead=:firstRead, lastRead=:lastRead WHERE id = :id");
				query.setParameter("firstRead", offset.getFirstRead());
				query.setParameter("lastRead", offset.getLastRead());
				query.setParameter("id", offset.getId());
				query.executeUpdate();
				
			}
			catch (Exception e) {
				throw new RuntimeException("An error occurred saving offset : " + " Start from " + offset.getFirstRead()
				        + "to " + offset.getLastRead() + " for application: " + offset.getApplicationName(), e);
			}
			return null;
		});
	}
	
	public DebeziumEventQueueOffset getOffsetByApplicationName(String applicationName) {
		return executeWithTransaction(sessionFactory, session -> {
			
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<DebeziumEventQueueOffset> criteriaQuery = criteriaBuilder
			        .createQuery(DebeziumEventQueueOffset.class);
			Root<DebeziumEventQueueOffset> root = criteriaQuery.from(DebeziumEventQueueOffset.class);
			criteriaQuery.select(root);
			
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.equal(root.get("applicationName"), applicationName));
			criteriaQuery.where(predicates.toArray(new Predicate[0]));
			
			TypedQuery<DebeziumEventQueueOffset> query = session.createQuery(criteriaQuery);
			List<DebeziumEventQueueOffset> results = query.getResultList();
			return results.isEmpty() ? null : results.get(0);
		});
	}
}
