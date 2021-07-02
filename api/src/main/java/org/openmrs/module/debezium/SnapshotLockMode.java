package org.openmrs.module.debezium;

/**
 * Controls whether and how long the connector holds the global read lock, which prevents any
 * updates to the database, while the connector is performing a snapshot.
 */
public interface SnapshotLockMode extends DebeziumEnumeratedPropertyValue {}
