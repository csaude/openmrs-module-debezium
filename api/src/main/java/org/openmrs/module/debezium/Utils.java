package org.openmrs.module.debezium;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	/**
	 * Gets a formatted timestamp
	 * 
	 * @return the generated file name
	 */
	public static String getCurrentTimestamp() {
		return DATE_FORMATTER.format(new Date());
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
