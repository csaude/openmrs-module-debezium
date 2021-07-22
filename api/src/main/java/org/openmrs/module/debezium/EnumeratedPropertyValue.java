package org.openmrs.module.debezium;

/**
 * Base interface for enumerated debezium property values
 */
public interface EnumeratedPropertyValue {
	
	/**
	 * Gets the actual property value to pass to the engine or connector
	 *
	 * @return the actual property value to pass to the engine or connector
	 */
	String getPropertyValue();
	
}
