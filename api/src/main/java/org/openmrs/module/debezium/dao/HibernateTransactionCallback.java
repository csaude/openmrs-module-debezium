package org.openmrs.module.debezium.dao;

import org.hibernate.Session;

import java.io.IOException;

@FunctionalInterface
public interface HibernateTransactionCallback<T> {
	
	T doInTransaction(Session status) throws IOException;
}
