package org.openmrs.module.debezium;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains general utility methods
 */
public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	
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
		try {
			if (log.isDebugEnabled()) {
				log.debug("Updating Global Property: " + property);
			}
			
			Context.openSession();
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			
			AdministrationService as = Context.getAdministrationService();
			GlobalProperty gp = as.getGlobalPropertyObject(property);
			gp.setPropertyValue(value);
			as.saveGlobalProperty(gp);
			
			if (log.isDebugEnabled()) {
				log.debug("Done updating Global Property: " + property);
			}
		}
		finally {
			try {
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			}
			finally {
				Context.closeSession();
			}
		}
	}
	
}
