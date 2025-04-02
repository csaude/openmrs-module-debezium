package org.openmrs.module.debezium.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.debezium.entity.DebeziumEventQueueOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;

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
			Criteria criteria = session.createCriteria(DebeziumEventQueueOffset.class);
			criteria.add(Restrictions.eq("applicationName", applicationName));
			
			return (DebeziumEventQueueOffset) criteria.uniqueResult();
		});
	}
}
