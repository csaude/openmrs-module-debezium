package org.openmrs.module.debezium;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;

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
	
	/**
	 * Updates a global property with the specified name with the specified value
	 * 
	 * @param property the property name
	 * @param value the new value to set
	 */
	public static void updateGlobalProperty(String property, String value) {
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(property);
		gp.setPropertyValue(value);
		as.saveGlobalProperty(gp);
	}
	
}
