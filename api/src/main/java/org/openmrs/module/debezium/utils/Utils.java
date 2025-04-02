package org.openmrs.module.debezium.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.debezium.entity.DatabaseEvent;
import org.openmrs.module.debezium.entity.DebeziumEventQueue;
import org.openmrs.module.debezium.entity.EventType;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains general utility methods
 */
public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	private static final Map<String, String> DATABASE_OPERATION_MAP;
	
	public final static List<String> DEMOGRAPHIC_TABLES = Arrays.asList("person", "patient", "person_name", "person_address",
	    "patient_identifier", "person_attribute");
	
	static {
		DATABASE_OPERATION_MAP = new HashMap();
		DATABASE_OPERATION_MAP.put("CREATE", "C");
		DATABASE_OPERATION_MAP.put("READ", "R");
		DATABASE_OPERATION_MAP.put("UPDATE", "U");
		DATABASE_OPERATION_MAP.put("DELETE", "D");
	}
	
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
	
	public static String[] getConnectionDetails() {
		String jdbcUrl = Context.getRuntimeProperties().getProperty(DebeziumConstants.PROP_DB_URL);
		String host, portStr, dbName, hostPortDbNameStr;
		if (jdbcUrl.indexOf("?") > -1) {
			hostPortDbNameStr = StringUtils.substringBetween(jdbcUrl, "//", "?");
		} else {
			hostPortDbNameStr = StringUtils.substringAfter(jdbcUrl, "//");
		}
		
		String[] hostPortDbName = StringUtils.split(hostPortDbNameStr, "/");
		String[] hostAndPort = StringUtils.split(hostPortDbName[0], ":");
		host = hostAndPort[0];
		portStr = hostAndPort[1];
		dbName = hostPortDbName[1];
		
		if (log.isDebugEnabled()) {
			log.debug("Connection details -> host=" + host + ", port=" + portStr + ", DB=" + dbName);
		}
		
		return new String[] { host, portStr, dbName };
	}
	
	public static DebeziumEventQueue convertDataBaseEvent(DatabaseEvent databaseEvent) {
		DebeziumEventQueue debeziumEvent = new DebeziumEventQueue();
		boolean isDemographicEvent = DEMOGRAPHIC_TABLES.contains(databaseEvent.getTableName());
		
		if (isDemographicEvent) {
			debeziumEvent.setEventType(EventType.D);
		} else {
			debeziumEvent.setEventType(EventType.G);
		}
		
		debeziumEvent.setOperation(DATABASE_OPERATION_MAP.get(databaseEvent.getOperation().toString()));
		debeziumEvent.setTableName(databaseEvent.getTableName());
		debeziumEvent.setSnapshot(databaseEvent.getSnapshot().equals("TRUE"));
		debeziumEvent.setPrimaryKeyId(databaseEvent.getPrimaryKeyId().toString());
		debeziumEvent.setCreatedAt(new Date());
		return debeziumEvent;
	}
	
	/**
	 * Retrieves the value of a global property with the specified name
	 *
	 * @param gpName the global property name
	 * @return the global property value
	 */
	public static String getGlobalPropertyValue(String gpName) {
		
		try {
			String value = Context.getAdministrationService().getGlobalProperty(gpName);
			if (StringUtils.isBlank(value)) {
				throw new APIException("No value set for the global property named: " + gpName);
			}
			return value;
		}
		catch (APIAuthenticationException e) {
			throw new RuntimeException("An error occurred trying to get the value for the global property: " + gpName, e);
		}
	}
	
	public static String getFetchSize() {
		return Utils.getGlobalPropertyValue(DebeziumConstants.GP_FETCH_SIZE);
	}
	
}
