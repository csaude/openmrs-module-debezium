package org.openmrs.module.debezium.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoBase {
	
	protected SessionFactory sessionFactory;
	
	private static final Logger logger = LoggerFactory.getLogger(DaoBase.class);
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public static <T> T executeWithTransaction(SessionFactory sessionFactory, HibernateTransactionCallback<T> callback) {
		Transaction transaction = null;
		try (Session session = sessionFactory.openSession()) {
			transaction = session.beginTransaction();
			T result = callback.doInTransaction(session);
			transaction.commit();
			return result;
		}
		catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			logger.error("Transaction failed, rolled back", e);
			throw new RuntimeException(e);
		}
	}
	
}
