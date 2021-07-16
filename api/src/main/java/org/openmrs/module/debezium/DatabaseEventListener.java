package org.openmrs.module.debezium;

/**
 * An implementation of this interface is notified of database changes, note that ONLY one listener
 * is allowed.
 */
@FunctionalInterface
public interface DatabaseEventListener {
	
	/**
	 * This method is called to notify the listener of database events
	 * 
	 * @param event the database event
	 */
	void process(DatabaseEvent event);
	
}
