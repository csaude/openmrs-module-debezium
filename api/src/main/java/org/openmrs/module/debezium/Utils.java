package org.openmrs.module.debezium;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	
	/**
	 * Gets the value of the specified field on the specified object.
	 *
	 * @param object the object
	 * @param field the field object
	 * @return the property value
	 * @param <T>
	 */
	public static <T> T getFieldValue(Object object, Field field) {
		boolean isAccessible = field.canAccess(object);
		
		try {
			if (!field.canAccess(object)) {
				field.setAccessible(true);
			}
			
			return (T) field.get(object);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to get the value of the property " + field, e);
		}
		finally {
			field.setAccessible(isAccessible);
		}
	}
	
	/**
	 * Sets the value of the specified field on the specified object.
	 *
	 * @param object the object
	 * @param field the field object
	 * @param value the value to set
	 */
	public static void setFieldValue(Object object, Field field, Object value) {
		boolean isAccessible = field.canAccess(object);
		
		try {
			if (!field.canAccess(object)) {
				field.setAccessible(true);
			}
			
			field.set(object, value);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to set property " + field, e);
		}
		finally {
			field.setAccessible(isAccessible);
		}
	}
	
	/**
	 * Invokes the method represented by the specified name on the specified object with the specified
	 * arguments.
	 *
	 * @param object the object
	 * @param method the method
	 * @param args the arguments to pass to the method
	 */
	public static Object invokeMethod(Object object, Method method, Object... args) {
		boolean isAccessible = method.isBridge();
		
		try {
			if (!method.canAccess(object)) {
				method.setAccessible(true);
			}
			
			return method.invoke(object, args);
		}
		catch (Exception e) {
			final String m = "Failed to invoke method " + method.getName() + " on object of type "
			        + object.getClass().getName();
			throw new RuntimeException(m, e);
		}
		finally {
			method.setAccessible(isAccessible);
		}
	}
	
}
