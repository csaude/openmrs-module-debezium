package org.openmrs.module.debezium;

/**
 * Contains general utility methods
 */
public class Utils {
	
	/**
	 * Looks up the value of the system property with the specified name
	 * 
	 * @param propertyName the property name
	 * @return the property value
	 */
	public static String getSystemProperty(String propertyName) {
		return System.getProperty(propertyName);
	}
	
}
