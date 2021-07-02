package org.openmrs.module.debezium;

/**
 * Specifies the criteria for running a snapshot when the connector starts
 */
public interface SnapshotMode {
	
	/**
	 * Gets the actual value to pass to the connector
	 * 
	 * @return the actual value to pass to the connector
	 */
	String getConnectorValue();
	
}
